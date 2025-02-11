package com.teamcubation.springsecurity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";  // Vista de la página de login (login.html)
    }

    @GetMapping("/home")
    public String home() {
        return "home";   // Vista protegida, solo accesible si el usuario está autenticado
    }

    @GetMapping("/inicio")
    public String inicio() {
        return "inicio";// Vista protegida, solo accesible si el admin está autenticado
    }

}

