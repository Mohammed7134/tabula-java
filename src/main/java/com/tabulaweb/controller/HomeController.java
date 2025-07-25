package com.tabulaweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller         // not @RestController because we want to return HTML
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";        // looks for src/main/resources/templates/index.html
    }
}
