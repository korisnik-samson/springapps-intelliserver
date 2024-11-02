package com.samson.springappsintelliserver.controllers;

import com.samson.springappsintelliserver.models.Users;
import com.samson.springappsintelliserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "api/users")
    public List<Users> getUsers() {
        return this.userService.getUsers();
    }

    @PostMapping(path = "register")
    public Users register(@RequestBody Users user) {
        return this.userService.registerUser(user);
    }

    @PostMapping(path = "login")
    public String login(@RequestBody Users user) {
        return userService.verifyUser(user);
    }
}
