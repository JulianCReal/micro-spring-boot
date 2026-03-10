package org.example.microspringboot.controllers;

import org.example.microspringboot.annotations.GetMapping;
import org.example.microspringboot.annotations.RequestParam;
import org.example.microspringboot.annotations.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    private static final String STYLE = "<style>*{box-sizing:border-box;margin:0;padding:0}"
        + "body{font-family:'Segoe UI',Arial,sans-serif;background:#f0f4f8;padding:40px 20px}"
        + ".card{max-width:600px;margin:0 auto;background:#fff;border-radius:10px;"
        + "box-shadow:0 4px 16px rgba(0,0,0,.1);padding:40px}"
        + "h1{color:#276749;margin-bottom:16px}p{color:#4a5568;line-height:1.6;margin-bottom:12px}"
        + ".counter{background:#edf2f7;padding:6px 14px;border-radius:20px;display:inline-block;"
        + "font-size:.9rem;color:#718096;margin-bottom:16px}"
        + "a{color:#276749;font-weight:bold}</style>";

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        long count = counter.incrementAndGet();
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" + STYLE + "</head>"
            + "<body><div class='card'>"
            + "<span class='counter'>Petición #" + count + "</span>"
            + "<h1>👋 Hola, " + name + "!</h1>"
            + "<p>" + String.format(template, name) + "</p>"
            + "<p><a href='/greeting?name=Ana'>Probar con Ana</a> &nbsp;|&nbsp; <a href='/'>← Volver</a></p>"
            + "</div></body></html>";
    }

    @GetMapping("/farewell")
    public String farewell(@RequestParam(value = "name", defaultValue = "amigo") String name) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" + STYLE + "</head>"
            + "<body><div class='card'><h1>👋 Goodbye, " + name + "!</h1>"
            + "<p><a href='/'>← Volver</a></p></div></body></html>";
    }
}
