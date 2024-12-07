package com.samson.springappsintelliserver.controllers;

import com.samson.springappsintelliserver.models.Users;
import com.samson.springappsintelliserver.providers.PasswordProvider;
import com.samson.springappsintelliserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // endpoint for all users
    @GetMapping(path = "api/users")
    public List<Users> getUsers() {
        return this.userService.getUsers();
    }

    // endpoint for registering a user
    @PostMapping(path = "register")
    public Users register(@RequestBody Users user) {
        return this.userService.registerUser(user);
    }

    // endpoint for logging in a user
    @PostMapping(path = "login")
    public String login(@RequestBody Users user) {
        return userService.verifyUser(user);
    }

    // endpoint for updating a user
    // passwords can be updated here
    // TODO: add a check to ensure that the user is updating their own account
    @PatchMapping(path = "api/users/{id}")
    public Users updateUser(@PathVariable("id") Integer userId, @RequestBody Users user) {
        return this.userService.updateUser(userId, user);
    }

    @PatchMapping(path = "api/users/{id}/password")
    public Users updatePassword(@PathVariable("id") Integer userId, @RequestBody PasswordProvider passwordBody) {
        return this.userService.updatePassword(userId, passwordBody);
    }
    
    // endpoint for logging out a user
    @GetMapping(path = "logout")
    public ResponseEntity<?> logout() {
        return this.userService.logout();
    }

    // endpoint for deleting a user
    @DeleteMapping(path = "api/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Integer userId) {
        return this.userService.deleteUser(userId);
    }
}
