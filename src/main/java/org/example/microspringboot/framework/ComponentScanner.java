package org.example.microspringboot.framework;

import org.example.microspringboot.annotations.RestController;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans the classpath for classes annotated with @RestController.
 * Works both when running from .class files (mvn exec / -cp) AND from a fat JAR.
 */
public class ComponentScanner {

    public static List<Class<?>> findRestControllers() throws Exception {
        List<Class<?>> controllers = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL selfUrl = ComponentScanner.class.getProtectionDomain()
                .getCodeSource().getLocation();

        System.out.println("[ComponentScanner] Running from: " + selfUrl);

        String urlStr = selfUrl.toString();

        // Normalize: strip "jar:" wrapper if present  e.g.  jar:file:/path/app.jar!/
        if (urlStr.startsWith("jar:")) {
            urlStr = urlStr.substring(4);
        }
        if (urlStr.contains("!")) {
            urlStr = urlStr.substring(0, urlStr.indexOf('!'));
        }

        boolean isJar = urlStr.toLowerCase().endsWith(".jar");

        if (isJar) {
            File jarFile = urlToFile(new URL(urlStr));
            System.out.println("[ComponentScanner] Scanning JAR: " + jarFile.getAbsolutePath());
            scanJar(jarFile, classLoader, controllers);
        } else {
            System.out.println("[ComponentScanner] Scanning class directories...");
            Enumeration<URL> roots = classLoader.getResources("");
            while (roots.hasMoreElements()) {
                URL root = roots.nextElement();
                if ("file".equals(root.getProtocol())) {
                    File dir = urlToFile(root);
                    System.out.println("[ComponentScanner] Dir: " + dir);
                    scanDirectory(dir, dir, classLoader, controllers);
                }
            }
        }

        System.out.println("[ComponentScanner] Total @RestController found: " + controllers.size());
        return controllers;
    }

    private static void scanJar(File jarFile, ClassLoader classLoader, List<Class<?>> result) {
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.endsWith(".class") || name.contains("$")) continue;
                String className = name.replace('/', '.').replace(".class", "");
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (clazz.isAnnotationPresent(RestController.class)) {
                        result.add(clazz);
                        System.out.println("[ComponentScanner] Found @RestController: " + className);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[ComponentScanner] ERROR reading JAR: " + e.getMessage());
        }
    }

    private static void scanDirectory(File rootDir, File current,
                                      ClassLoader classLoader, List<Class<?>> result) {
        File[] files = current.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                scanDirectory(rootDir, f, classLoader, result);
            } else if (f.getName().endsWith(".class") && !f.getName().contains("$")) {
                String className = toClassName(rootDir, f);
                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (clazz.isAnnotationPresent(RestController.class)) {
                        result.add(clazz);
                        System.out.println("[ComponentScanner] Found @RestController: " + className);
                    }
                } catch (Throwable ignored) {}
            }
        }
    }

    /**
     * Converts a URL to File, handling Windows paths like file:/D:/Users/... correctly.
     */
    private static File urlToFile(URL url) {
        try {
            return new File(url.toURI());
        } catch (Exception e) {
            String path = url.getPath();
            try { path = java.net.URLDecoder.decode(path, "UTF-8"); } catch (Exception ignored) {}
            if (path.matches("/[A-Za-z]:/.*")) path = path.substring(1);
            return new File(path);
        }
    }

    private static String toClassName(File rootDir, File classFile) {
        String root = rootDir.getAbsolutePath();
        String file = classFile.getAbsolutePath();
        String relative = file.substring(root.length() + 1);
        return relative.replace(File.separatorChar, '.').replace(".class", "");
    }
}
