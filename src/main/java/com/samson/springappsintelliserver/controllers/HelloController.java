package com.samson.springappsintelliserver.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping(path = "/default")
    public String hello() {
        return "Hello, World!";
    }

}
