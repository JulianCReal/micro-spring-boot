package org.example.microspringboot;

import org.example.microspringboot.annotations.GetMapping;
import org.example.microspringboot.annotations.RequestParam;
import org.example.microspringboot.annotations.RestController;
import org.example.microspringboot.framework.DispatcherHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for the IoC framework core components.
 */
public class MicroSpringBootTest {

    private DispatcherHandler dispatcher;

    // Inner test controller used only in tests
    @RestController
    public static class TestController {
        @GetMapping("/test")
        public String testEndpoint() {
            return "test-response";
        }

        @GetMapping("/greet")
        public String greet(@RequestParam(value = "name", defaultValue = "Tester") String name) {
            return "Hello, " + name + "!";
        }
    }

    @Before
    public void setUp() throws Exception {
        dispatcher = new DispatcherHandler();
        List<Class<?>> controllers = Arrays.asList(TestController.class);
        dispatcher.registerControllers(controllers);
    }

    @Test
    public void testBasicRouteDispatch() throws Exception {
        String result = dispatcher.dispatch("/test");
        Assert.assertEquals("test-response", result);
    }

    @Test
    public void testRequestParamDefault() throws Exception {
        String result = dispatcher.dispatch("/greet");
        Assert.assertEquals("Hello, Tester!", result);
    }

    @Test
    public void testRequestParamProvided() throws Exception {
        String result = dispatcher.dispatch("/greet?name=Ana");
        Assert.assertEquals("Hello, Ana!", result);
    }

    @Test
    public void testUnknownRouteReturnsNull() throws Exception {
        String result = dispatcher.dispatch("/unknown");
        Assert.assertNull(result);
    }

    @Test
    public void testHasRoute() {
        Assert.assertTrue(dispatcher.hasRoute("/test"));
        Assert.assertFalse(dispatcher.hasRoute("/nonexistent"));
    }

    @Test
    public void testAnnotationPresence() {
        Assert.assertTrue(TestController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    public void testGetMappingAnnotation() throws NoSuchMethodException {
        java.lang.reflect.Method m = TestController.class.getMethod("testEndpoint");
        Assert.assertTrue(m.isAnnotationPresent(GetMapping.class));
        Assert.assertEquals("/test", m.getAnnotation(GetMapping.class).value());
    }
}
