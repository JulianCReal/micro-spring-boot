package org.example.microspringboot.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a method parameter to a query string parameter.
 * Supports a defaultValue if the parameter is not present in the request.
 *
 * Example:
 *   @GetMapping("/greeting")
 *   public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
 *       return "Hello, " + name + "!";
 *   }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequestParam {
    String value();
    String defaultValue() default "";
}
