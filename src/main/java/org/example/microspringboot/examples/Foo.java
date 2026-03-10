package org.example.microspringboot.examples;

/**
 * Clase de prueba que demuestra el uso de la anotación @Tests.
 * RunTests usará reflexión para detectar cuáles métodos tienen @Tests
 * y ejecutarlos, reportando cuántos pasan y cuántos fallan.
 *
 * Métodos con @Tests: m1, m3, m5, m7
 * Métodos sin @Tests: m2, m4, m6, m8  (serán ignorados por RunTests)
 * Métodos que lanzan excepción: m3 ("Boom"), m7 ("Crash") → fallarán
 */
public class Foo {
    @Tests
    public static void m1() { }
    public static void m2() { }

    @Tests
    public static void m3() {
        throw new RuntimeException("Boom");
    }
    public static void m4() { }

    @Tests
    public static void m5() { }
    public static void m6() { }

    @Tests
    public static void m7() {
        throw new RuntimeException("Crash");
    }
    public static void m8() { }
}
