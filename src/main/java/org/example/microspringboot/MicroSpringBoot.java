package org.example.microspringboot;

import org.example.microspringboot.framework.ComponentScanner;
import org.example.microspringboot.framework.DispatcherHandler;
import org.example.microspringboot.framework.HttpServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MicroSpringBoot {

    private static final int DEFAULT_PORT = 8080;

    /**
     * Lista fija de controladores registrados en el framework.
     * En modo auto-scan esto se descubre dinámicamente;
     * si el scan falla (fat JAR en Windows), se usa esta lista como fallback.
     */
    private static final List<String> KNOWN_CONTROLLERS = Arrays.asList(
        "org.example.microspringboot.controllers.HelloController",
        "org.example.microspringboot.controllers.GreetingController"
    );

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        List<Class<?>> explicitControllers = new ArrayList<>();

        for (String arg : args) {
            try {
                port = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                try {
                    explicitControllers.add(Class.forName(arg));
                    System.out.println("[Startup] Loaded from args: " + arg);
                } catch (ClassNotFoundException ex) {
                    System.err.println("[Startup] Class not found: " + arg);
                }
            }
        }

        List<Class<?>> controllers;

        if (!explicitControllers.isEmpty()) {
            // Modo 2: clases pasadas por argumento
            System.out.println("[Startup] Mode: explicit class loading");
            controllers = explicitControllers;

        } else {
            // Modo 1: auto-scan del classpath
            System.out.println("[Startup] Mode: auto-scanning classpath...");
            controllers = ComponentScanner.findRestControllers();

            // Fallback: si el scan no encontró nada (fat JAR en Windows),
            // cargar la lista conocida de controladores
            if (controllers.isEmpty()) {
                System.out.println("[Startup] Scan found 0 — using built-in controller list");
                for (String className : KNOWN_CONTROLLERS) {
                    try {
                        controllers.add(Class.forName(className));
                        System.out.println("[Startup] Loaded: " + className);
                    } catch (ClassNotFoundException e) {
                        System.err.println("[Startup] Could not load: " + className);
                    }
                }
            }
        }

        System.out.println("[Startup] Registering " + controllers.size() + " controller(s)...");

        DispatcherHandler dispatcher = new DispatcherHandler();
        dispatcher.registerControllers(controllers);

        HttpServer server = new HttpServer(port, dispatcher);
        server.start();
    }
}
