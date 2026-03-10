package org.example.microspringboot.controllers;

import org.example.microspringboot.annotations.GetMapping;
import org.example.microspringboot.annotations.RestController;

@RestController
public class HelloController {

    private static final String STYLE = "<style>*{box-sizing:border-box;margin:0;padding:0}"
        + "body{font-family:'Segoe UI',Arial,sans-serif;background:#f0f4f8;padding:40px 20px}"
        + ".card{max-width:600px;margin:0 auto;background:#fff;border-radius:10px;"
        + "box-shadow:0 4px 16px rgba(0,0,0,.1);padding:40px}"
        + "h1{color:#276749;margin-bottom:16px}p{color:#4a5568;line-height:1.6;margin-bottom:12px}"
        + "a{color:#276749;font-weight:bold}pre{background:#edf2f7;padding:14px;"
        + "border-radius:6px;font-size:.9rem}</style>";

    @GetMapping("/")
    public String index() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>MicroSpringBoot</title>" + STYLE + "</head>"
            + "<body><div class='card'><h1>🌱 MicroSpringBoot</h1>"
            + "<p>Framework IoC mínimo — reflexión en Java.</p>"
            + "<p><a href='/hello'>/hello</a> &nbsp;|&nbsp; <a href='/pi'>/pi</a> &nbsp;|&nbsp; "
            + "<a href='/greeting'>/greeting</a> &nbsp;|&nbsp; <a href='/greeting?name=Ana'>/greeting?name=Ana</a>"
            + " &nbsp;|&nbsp; <a href='/time'>/time</a></p>"
            + "</div></body></html>";
    }

    @GetMapping("/hello")
    public String helloWorld() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" + STYLE + "</head>"
            + "<body><div class='card'><h1>👋 Hello, World!</h1>"
            + "<p><a href='/'>← Volver</a></p></div></body></html>";
    }

    @GetMapping("/pi")
    public String getPi() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" + STYLE + "</head>"
            + "<body><div class='card'><h1>π = " + Math.PI + "</h1>"
            + "<p><a href='/'>← Volver</a></p></div></body></html>";
    }

    @GetMapping("/time")
    public String getTime() {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>" + STYLE + "</head>"
            + "<body><div class='card'><h1>🕐 Hora del servidor</h1>"
            + "<p>" + new java.util.Date() + "</p>"
            + "<p><a href='/'>← Volver</a></p></div></body></html>";
    }
}
