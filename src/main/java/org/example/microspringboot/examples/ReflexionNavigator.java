package org.example.microspringboot.examples;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static java.lang.System.out;

/**
 * Explorador de miembros de una clase usando la API de Reflexión de Java.
 * Imprime constructores, campos y métodos de cualquier objeto.
 *
 * Uso:
 *   java -cp target/classes org.example.microspringboot.examples.ReflexionNavigator
 *
 * Este explorador ilustra las mismas capacidades que usa ComponentScanner
 * para inspeccionar clases en tiempo de ejecución:
 *   - getDeclaredConstructors()
 *   - getDeclaredFields()
 *   - getDeclaredMethods()
 */
public class ReflexionNavigator {

    public static void main(String[] args) {
        // Explorar la clase String usando reflexión
        Class c = "hola".getClass();

        System.out.println("=== Explorando clase: " + c.getName() + " ===");
        printMembers(c.getDeclaredConstructors(), "Constructores");
        printMembers(c.getDeclaredFields(),       "Campos");
        printMembers(c.getDeclaredMethods(),      "Métodos");
    }

    private static void printMembers(Member[] mbrs, String tipo) {
        out.format("%n-- %s --%n", tipo);
        for (Member mbr : mbrs) {
            if (mbr instanceof Field)
                out.format("  CAMPO:       %s%n", ((Field) mbr).toGenericString());
            else if (mbr instanceof Constructor)
                out.format("  CONSTRUCTOR: %s%n", ((Constructor) mbr).toGenericString());
            else if (mbr instanceof Method)
                out.format("  MÉTODO:      %s%n", ((Method) mbr).toGenericString());
        }
        if (mbrs.length == 0)
            out.format("  (ninguno)%n");
    }
}
