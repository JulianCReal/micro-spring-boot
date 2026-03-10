package org.example.microspringboot.examples;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Invoca el método main() de cualquier clase usando reflexión.
 * Demuestra cómo cargar y ejecutar código dinámicamente en tiempo de ejecución.
 *
 * Uso:
 *   java -cp target/classes org.example.microspringboot.examples.InvokeMain \
 *        org.example.microspringboot.examples.RunTests \
 *        org.example.microspringboot.examples.Foo
 *
 * Este mismo patrón es el núcleo de MicroSpringBoot:
 * cargar clases por nombre y ejecutar métodos sin conocerlos en tiempo de compilación.
 */
public class InvokeMain {
    public static void main(String... args) throws IllegalAccessException, InvocationTargetException {
        try {
            // Reflexión: cargar clase por nombre
            Class<?> c = Class.forName(args[0]);

            // Reflexión: obtener el método main(String[])
            Class[] argTypes = new Class[]{String[].class};
            Method main = c.getDeclaredMethod("main", argTypes);

            // Preparar argumentos (todo lo que viene después del nombre de clase)
            String[] mainArgs = Arrays.copyOfRange(args, 1, args.length);

            System.out.format("Invocando %s.main()%n", c.getName());

            // Reflexión: invocar main dinámicamente
            main.invoke(null, (Object) mainArgs);

        } catch (ClassNotFoundException x) {
            x.printStackTrace();
        } catch (NoSuchMethodException x) {
            x.printStackTrace();
        }
    }
}
