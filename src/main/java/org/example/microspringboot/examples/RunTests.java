package org.example.microspringboot.examples;

import java.lang.reflect.Method;

/**
 * Mini framework de testing basado en reflexión.
 * Inspiración directa del framework de pruebas del proyecto original.
 *
 * Uso:
 *   java -cp target/classes org.example.microspringboot.examples.RunTests \
 *        org.example.microspringboot.examples.Foo
 *
 * Salida esperada:
 *   Test m3() failed: java.lang.RuntimeException: Boom
 *   Test m7() failed: java.lang.RuntimeException: Crash
 *   Passed: 2, Failed 2
 *
 * El mismo principio de reflexión que usa MicroSpringBoot para detectar
 * @RestController y @GetMapping — aquí aplicado para detectar @Tests.
 */
public class RunTests {
    public static void main(String[] args) throws Exception {
        int passed = 0, failed = 0;

        // Reflexión: cargar la clase por nombre desde args
        for (Method m : Class.forName(args[0]).getMethods()) {

            // Reflexión: verificar si el método tiene la anotación @Tests
            if (m.isAnnotationPresent(Tests.class)) {
                try {
                    // Reflexión: invocar el método dinámicamente
                    m.invoke(null);
                    passed++;
                } catch (Throwable ex) {
                    System.out.printf("Test %s failed: %s %n", m, ex.getCause());
                    failed++;
                }
            }
        }
        System.out.printf("Passed: %d, Failed %d%n", passed, failed);
    }
}
