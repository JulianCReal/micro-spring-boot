package org.example.microspringboot.framework;

import org.example.microspringboot.annotations.GetMapping;
import org.example.microspringboot.annotations.RequestParam;
import org.example.microspringboot.annotations.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central dispatcher: registers all @GetMapping endpoints from @RestController beans
 * and dispatches incoming HTTP requests to the correct handler method using reflection.
 *
 * This is the heart of the IoC framework — it decouples HTTP routing from business logic.
 */
public class DispatcherHandler {

    // Maps URI path -> handler method (e.g., "/hello" -> HelloController.hello())
    private final Map<String, Method> routes = new HashMap<>();

    // Maps URI path -> controller instance (needed to call instance methods)
    private final Map<String, Object> instances = new HashMap<>();

    /**
     * Scans a list of controller classes and registers all @GetMapping methods.
     * Uses Java Reflection to inspect methods at runtime.
     */
    public void registerControllers(List<Class<?>> controllerClasses) throws Exception {
        for (Class<?> clazz : controllerClasses) {
            if (!clazz.isAnnotationPresent(RestController.class)) continue;

            // Create a single instance of the controller (IoC container manages lifecycle)
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    GetMapping mapping = method.getAnnotation(GetMapping.class);
                    String path = mapping.value();
                    routes.put(path, method);
                    instances.put(path, instance);
                    System.out.println("[Dispatcher] Registered: GET " + path
                            + " -> " + clazz.getSimpleName() + "." + method.getName() + "()");
                }
            }
        }
    }

    /**
     * Dispatches an incoming request path (with optional query string) to the matching
     * controller method, resolves @RequestParam arguments, and returns the String response.
     *
     * @param requestPath  full request path, e.g. "/greeting?name=Ana"
     * @return             the String returned by the controller method, or null if not found
     */
    public String dispatch(String requestPath) throws Exception {
        // Split path and query string: "/greeting?name=Ana" -> ["/greeting", "name=Ana"]
        String path;
        Map<String, String> queryParams = new HashMap<>();

        if (requestPath.contains("?")) {
            String[] parts = requestPath.split("\\?", 2);
            path = parts[0];
            parseQueryString(parts[1], queryParams);
        } else {
            path = requestPath;
        }

        Method method = routes.get(path);
        if (method == null) return null;

        Object instance = instances.get(path);
        Object[] args = resolveArguments(method, queryParams);

        // Core reflection: invoke the method dynamically
        Object result = method.invoke(instance, args);
        return result != null ? result.toString() : "";
    }

    /**
     * Resolves method parameters annotated with @RequestParam from query string values.
     * Falls back to defaultValue if the parameter is missing.
     */
    private Object[] resolveArguments(Method method, Map<String, String> queryParams) {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = params[i].getAnnotation(RequestParam.class);
                String value = queryParams.getOrDefault(rp.value(), rp.defaultValue());
                args[i] = value;
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    /**
     * Parses "key=value&key2=value2" into a Map.
     */
    private void parseQueryString(String query, Map<String, String> map) {
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(decode(kv[0]), decode(kv[1]));
            } else if (kv.length == 1) {
                map.put(decode(kv[0]), "");
            }
        }
    }

    private String decode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    public boolean hasRoute(String path) {
        String cleanPath = path.contains("?") ? path.split("\\?")[0] : path;
        return routes.containsKey(cleanPath);
    }
}
