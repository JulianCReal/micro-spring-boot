package org.example.microspringboot.framework;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Minimal HTTP/1.1 server.
 *
 * Static files are loaded from the CLASSPATH (inside the JAR when packaged,
 * or from target/classes/static when running with -cp). This means placing
 * files under src/main/resources/static/ is enough — Maven copies them into
 * the JAR automatically, and the ClassLoader can always find them.
 */
public class HttpServer {

    private final int port;
    private final DispatcherHandler dispatcher;

    // Classpath prefix for static resources (maps to src/main/resources/static/)
    private static final String STATIC_CLASSPATH = "static";

    // Optional filesystem fallback (useful during development with -cp)
    private static final String STATIC_DEV_DIR = "src/main/resources/static";

    public HttpServer(int port, DispatcherHandler dispatcher) {
        this.port = port;
        this.dispatcher = dispatcher;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("======================================================");
            System.out.println("  MicroSpringBoot server started on port " + port);
            System.out.println("  Open: http://localhost:" + port + "/");
            System.out.println("======================================================");

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleRequest(clientSocket);
                } catch (Exception e) {
                    System.err.println("[Server] Error handling request: " + e.getMessage());
                }
            }
        }
    }

    private void handleRequest(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream out = socket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) return;

        System.out.println("[Server] " + requestLine);

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) return;

        String method = parts[0];
        String requestPath = parts[1];

        if (!"GET".equalsIgnoreCase(method)) {
            sendText(out, 405, "text/plain", "Method Not Allowed");
            return;
        }

        // 1. REST controller routes
        if (dispatcher.hasRoute(requestPath)) {
            try {
                String body = dispatcher.dispatch(requestPath);
                sendText(out, 200, "text/html; charset=utf-8", body);
            } catch (Exception e) {
                sendText(out, 500, "text/plain", "Internal Server Error: " + e.getMessage());
            }
            return;
        }

        // 2. Static files
        serveStaticFile(out, requestPath);
    }

    /**
     * Serves a static file.
     * Strategy:
     *   1. Try ClassLoader (works inside JAR and with -cp)
     *   2. Try filesystem fallback (useful when running directly in dev)
     */
    private void serveStaticFile(OutputStream out, String requestPath) throws IOException {
        if (requestPath.contains("..")) {
            sendText(out, 403, "text/plain", "Forbidden");
            return;
        }

        String filePath = requestPath.equals("/") ? "/index.html" : requestPath;
        // Remove leading slash for ClassLoader lookup
        String resourcePath = STATIC_CLASSPATH + filePath.replace("\\", "/");

        // --- Strategy 1: ClassLoader (JAR-compatible) ---
        InputStream resourceStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);

        if (resourceStream != null) {
            byte[] content = toBytes(resourceStream);
            sendBinary(out, 200, detectContentType(filePath), content);
            return;
        }

        // --- Strategy 2: Filesystem fallback (development) ---
        Path fsPath = Paths.get(STATIC_DEV_DIR + filePath);
        if (Files.exists(fsPath) && !Files.isDirectory(fsPath)) {
            byte[] content = Files.readAllBytes(fsPath);
            sendBinary(out, 200, detectContentType(filePath), content);
            return;
        }

        // Not found
        sendText(out, 404, "text/html",
                "<html><body><h1>404 - Not Found</h1><p>" + filePath + "</p></body></html>");
    }

    private byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int n;
        while ((n = is.read(chunk)) != -1) buffer.write(chunk, 0, n);
        return buffer.toByteArray();
    }

    private void sendText(OutputStream out, int code, String contentType, String body) throws IOException {
        sendBinary(out, code, contentType, body.getBytes("UTF-8"));
    }

    private void sendBinary(OutputStream out, int code, String contentType, byte[] body) throws IOException {
        String header = "HTTP/1.1 " + code + " " + statusText(code) + "\r\n"
                + "Content-Type: " + contentType + "\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";
        out.write(header.getBytes("UTF-8"));
        out.write(body);
        out.flush();
    }

    private String statusText(int code) {
        switch (code) {
            case 200: return "OK";
            case 403: return "Forbidden";
            case 404: return "Not Found";
            case 405: return "Method Not Allowed";
            case 500: return "Internal Server Error";
            default:  return "Unknown";
        }
    }

    private String detectContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html; charset=utf-8";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif"))  return "image/gif";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        if (path.endsWith(".ico"))  return "image/x-icon";
        if (path.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
