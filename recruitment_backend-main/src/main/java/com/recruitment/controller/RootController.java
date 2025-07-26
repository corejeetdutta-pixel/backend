package com.recruitment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://1c.atract.in"}, allowCredentials = "true")

public class RootController {

    @GetMapping("/")
    public String forward() {
        return "forward:/index.html";
    }

}

