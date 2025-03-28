package com.samson.springappsintelliserver.controllers;

import com.samson.springappsintelliserver.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    
    private final UserService userService;
    
    @Autowired
    public HomeController(UserService userService) {
        this.userService = userService;
    }

    // this is just houses test endpoints

    @GetMapping(path = "/home-id")
    public String helloId(HttpServletRequest request) {
        return "Intelli Server is up and running! " + request.getSession().getId();
    }

    @GetMapping(path = "/home")
    public String hello() {
        return "Intelli Server is up and running!";
    }

    // the csrf token endpoint will not work due to it being off in the security configuration
    @GetMapping(path = "/csrf-token")
    public CsrfToken getCsrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }
    
    @GetMapping(path = "/de-token")
    public String getUsername(@RequestBody String token) {
        // returns the username from the provided token
        return this.userService.getUsernameFromToken(token);
    }
}
