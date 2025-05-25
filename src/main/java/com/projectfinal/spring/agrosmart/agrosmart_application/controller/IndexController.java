package com.projectfinal.spring.agrosmart.agrosmart_application.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping({"/", "/index"})
    public String showWelcomePage() {
        return "index"; 
    }
}