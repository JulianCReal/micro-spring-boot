package org.example.microspringboot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps HTTP GET requests to handler methods.
 * Used within @RestController classes to define URI endpoints.
 *
 * Example:
 *   @GetMapping("/hello")
 *   public String hello() { return "Hello!"; }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GetMapping {
    String value();
}
