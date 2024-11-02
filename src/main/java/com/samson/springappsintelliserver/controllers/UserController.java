package com.samson.springappsintelliserver.controllers;

import com.samson.springappsintelliserver.models.User;
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

    @GetMapping(path = "/api/users")
    public List<User> getUsers() {
        return this.userService.getUsers();
    }

    @PostMapping(path = "/api/users")
    public User addUser(@RequestBody User user) {
        return this.userService.addUser(user);
    }
}
