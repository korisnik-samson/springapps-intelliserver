package com.samson.springappsintelliserver.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    // this is just houses test endpoints

    @GetMapping(path = "/home")
    public String hello(HttpServletRequest request) {
        return "Intelli Server is up and running! " + request.getSession().getId();
    }

    // the csrf token endpoint will not work due to it being off in the security configuration
    @GetMapping(path = "/csrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }
}
