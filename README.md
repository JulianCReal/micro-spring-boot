# MicroSpringBoot 🌱

Servidor web HTTP mínimo con framework IoC (Inversion of Control) construido en Java puro, sin dependencias externas. Demuestra las capacidades de reflexión de Java para descubrir y registrar componentes en tiempo de ejecución.

## Arquitectura

```
MicroSpringBoot (main)
    │
    ├── ComponentScanner      ← Escanea classpath buscando @RestController
    ├── DispatcherHandler     ← Registra rutas y las invoca via reflexión
    └── HttpServer            ← Servidor TCP que habla HTTP/1.1
```

### Anotaciones soportadas

| Anotación | Objetivo | Descripción |
|---|---|---|
| `@RestController` | Clase | Marca la clase como componente REST |
| `@GetMapping("/path")` | Método | Registra el método como handler de GET |
| `@RequestParam(value="x", defaultValue="y")` | Parámetro | Inyecta query params |

## Cómo ejecutar

### Prerrequisitos
- Java 11+
- Maven 3.6+

### Compilar
```bash
mvn clean package
```

### Modo 1: Auto-scan (recomendado)
El framework escanea automáticamente el classpath buscando `@RestController`:
```bash
java -jar target/micro-spring-boot-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Modo 2: Especificar clase explícitamente (como el framework de TEST)
```bash
java -cp target/classes org.example.microspringboot.MicroSpringBoot \
     org.example.microspringboot.controllers.HelloController
```

### Puerto personalizado
```bash
java -jar target/micro-spring-boot-1.0-SNAPSHOT-jar-with-dependencies.jar 9090
```

## Endpoints de ejemplo

| Endpoint | Descripción |
|---|---|
| `GET /` | Página principal |
| `GET /hello` | Hello World |
| `GET /pi` | Valor de Pi |
| `GET /greeting` | Saludo (defaultValue="World") |
| `GET /greeting?name=Ana` | Saludo con @RequestParam |
| `GET /farewell?name=Carlos` | Despedida con @RequestParam |
| `GET /time` | Hora del servidor |
| `GET /about.html` | Página HTML estática |

## Ejemplo de controlador personalizado

```java
@RestController
public class MiController {

    @GetMapping("/suma")
    public String suma(@RequestParam(value = "a", defaultValue = "0") String a,
                       @RequestParam(value = "b", defaultValue = "0") String b) {
        int resultado = Integer.parseInt(a) + Integer.parseInt(b);
        return "Resultado: " + resultado;
    }
}
```

## Despliegue en AWS EC2

1. **Crear instancia EC2** (Amazon Linux 2, t2.micro)
2. **Abrir puerto 8080** en Security Groups (inbound rule TCP 8080)
3. **Instalar Java**:
   ```bash
   sudo yum install java-11-amazon-corretto -y
   ```
4. **Subir el JAR**:
   ```bash
   scp -i key.pem target/micro-spring-boot-1.0-SNAPSHOT-jar-with-dependencies.jar ec2-user@<IP>:~
   ```
5. **Ejecutar**:
   ```bash
   java -jar micro-spring-boot-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```
6. **Acceder**: `http://<IP_PUBLICA>:8080/`

## Tests

```bash
mvn test
```

## Cómo funciona la reflexión (IoC explicado)

```java
// 1. Escanear classpath
Class<?> clazz = classLoader.loadClass(className);

// 2. Detectar @RestController via reflexión
if (clazz.isAnnotationPresent(RestController.class)) { ... }

// 3. Registrar métodos @GetMapping
for (Method m : clazz.getDeclaredMethods()) {
    if (m.isAnnotationPresent(GetMapping.class)) {
        routes.put(m.getAnnotation(GetMapping.class).value(), m);
    }
}

// 4. Resolver @RequestParam en tiempo de llamada
for (Parameter p : method.getParameters()) {
    RequestParam rp = p.getAnnotation(RequestParam.class);
    args[i] = queryParams.getOrDefault(rp.value(), rp.defaultValue());
}

// 5. Invocar el método dinámicamente
Object result = method.invoke(instance, args);
```
