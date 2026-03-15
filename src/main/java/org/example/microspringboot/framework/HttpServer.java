package org.example.microspringboot.framework;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Concurrent HTTP/1.1 server backed by a fixed thread pool.
 *
 * Concurrency: each accepted connection is handed off to a worker thread from
 * a pool of {@value #THREAD_POOL_SIZE} threads, so multiple clients are served
 * simultaneously without blocking the accept loop.
 *
 * Graceful shutdown: a JVM shutdown hook (registered in its own thread, as
 * required by Runtime.addShutdownHook) sets the stop flag, closes the
 * ServerSocket so the blocking accept() unblocks, and then waits up to 30 s
 * for in-flight requests to finish before forcing a shutdown.
 */
public class HttpServer {

    private static final int THREAD_POOL_SIZE = 10;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    private final int port;
    private final DispatcherHandler dispatcher;

    /** Shared flag — volatile so all threads see the update immediately. */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /** Thread pool that processes incoming connections concurrently. */
    private final ExecutorService threadPool =
            Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    /** Kept as a field so the shutdown hook can close it. */
    private ServerSocket serverSocket;

    private static final String STATIC_CLASSPATH = "static";
    private static final String STATIC_DEV_DIR   = "src/main/resources/static";

    public HttpServer(int port, DispatcherHandler dispatcher) {
        this.port       = port;
        this.dispatcher = dispatcher;
    }

    // -------------------------------------------------------------------------
    // Startup
    // -------------------------------------------------------------------------

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);

        registerShutdownHook();

        System.out.println("======================================================");
        System.out.println("  MicroSpringBoot server started on port " + port);
        System.out.println("  Thread pool size: " + THREAD_POOL_SIZE);
        System.out.println("  Open: http://localhost:" + port + "/");
        System.out.println("  Stop: Ctrl+C (graceful shutdown enabled)");
        System.out.println("======================================================");

        while (running.get()) {
            try {
                Socket clientSocket = serverSocket.accept();   // blocks until a client connects
                // Hand off to the thread pool — accept loop is never blocked by slow clients
                threadPool.submit(() -> {
                    try {
                        handleRequest(clientSocket);
                    } catch (Exception e) {
                        System.err.println("[Server] Error handling request: " + e.getMessage());
                    } finally {
                        try { clientSocket.close(); } catch (IOException ignored) {}
                    }
                });
            } catch (SocketException e) {
                if (!running.get()) {
                    // ServerSocket was closed intentionally by the shutdown hook — exit cleanly
                    break;
                }
                System.err.println("[Server] Socket error: " + e.getMessage());
            }
        }

        System.out.println("[Server] Accept loop exited.");
    }

    // -------------------------------------------------------------------------
    // Graceful shutdown hook
    // -------------------------------------------------------------------------

    /**
     * Registers a JVM shutdown hook that:
     *   1. Sets the {@code running} flag to false.
     *   2. Closes the ServerSocket so the blocking {@code accept()} throws and unblocks.
     *   3. Shuts down the thread pool, waiting up to {@value #SHUTDOWN_TIMEOUT_SECONDS} s
     *      for in-flight requests to complete before forcing termination.
     *
     * The hook itself runs in a dedicated thread, as required by the JVM contract
     * (see https://www.baeldung.com/jvm-shutdown-hooks).
     */
    private void registerShutdownHook() {
        Thread hookThread = new Thread(() -> {
            System.out.println("[Server] Shutdown signal received — stopping gracefully...");

            // 1. Stop accepting new connections
            running.set(false);

            // 2. Unblock serverSocket.accept()
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("[Server] Error closing server socket: " + e.getMessage());
            }

            // 3. Allow in-flight requests to finish, then force-stop
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.out.println("[Server] Forcing shutdown after timeout...");
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }

            System.out.println("[Server] Graceful shutdown complete.");
        }, "shutdown-hook");

        Runtime.getRuntime().addShutdownHook(hookThread);
        System.out.println("[Server] Graceful shutdown hook registered.");
    }

    // -------------------------------------------------------------------------
    // Request handling (unchanged logic, now runs in worker threads)
    // -------------------------------------------------------------------------

    private void handleRequest(Socket socket) throws IOException {
        BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStream   out = socket.getOutputStream();

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) return;

        System.out.println("[Server][" + Thread.currentThread().getName() + "] " + requestLine);

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) return;

        String method      = parts[0];
        String requestPath = parts[1];

        if (!"GET".equalsIgnoreCase(method)) {
            sendText(out, 405, "text/plain", "Method Not Allowed");
            return;
        }

        // 1. Dynamic REST routes
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

    private void serveStaticFile(OutputStream out, String requestPath) throws IOException {
        if (requestPath.contains("..")) {
            sendText(out, 403, "text/plain", "Forbidden");
            return;
        }

        String filePath    = requestPath.equals("/") ? "/index.html" : requestPath;
        String resourcePath = STATIC_CLASSPATH + filePath.replace("\\", "/");

        InputStream resourceStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath);

        if (resourceStream != null) {
            byte[] content = toBytes(resourceStream);
            sendBinary(out, 200, detectContentType(filePath), content);
            return;
        }

        Path fsPath = Paths.get(STATIC_DEV_DIR + filePath);
        if (Files.exists(fsPath) && !Files.isDirectory(fsPath)) {
            byte[] content = Files.readAllBytes(fsPath);
            sendBinary(out, 200, detectContentType(filePath), content);
            return;
        }

        sendText(out, 404, "text/html",
                "<html><body><h1>404 - Not Found</h1><p>" + filePath + "</p></body></html>");
    }

    // -------------------------------------------------------------------------
    // HTTP response helpers
    // -------------------------------------------------------------------------

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