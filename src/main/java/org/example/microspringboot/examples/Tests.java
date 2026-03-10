package org.example.microspringboot.examples;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada @Tests para marcar métodos como casos de prueba.
 * Demuestra la creación de anotaciones con retención en tiempo de ejecución (RUNTIME),
 * lo que permite que el framework de testing las detecte via reflexión.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Tests {
}
