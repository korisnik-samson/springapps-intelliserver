package com.samson.springappsintelliserver.services;

import com.samson.springappsintelliserver.models.Users;
import com.samson.springappsintelliserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JWTService jwtService;
    AuthenticationManager authenticationManager;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Autowired
    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, JWTService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public List<Users> getUsers() {
        return this.userRepository.findAll();
    }

    public Users registerUser(@NonNull Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return this.userRepository.save(user);
    }

    public String verifyUser(@NonNull Users user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
        );

        if (authentication.isAuthenticated())
            return jwtService.generateToken(user.getUsername());

        else return "Invalid credentials";
    }

    public Users updateUser(Integer id, @NonNull Users updatedUser) {
        Users currentUser = this.userRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("User not found"));

        if (updatedUser.getFirstName() != null)
            currentUser.setFirstName(updatedUser.getFirstName());

        if (updatedUser.getLastName() != null)
            currentUser.setLastName(updatedUser.getLastName());

        if (updatedUser.getEmail() != null)
            currentUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getUsername() != null)
            currentUser.setUsername(updatedUser.getUsername());

        if (updatedUser.getPassword() != null)
            currentUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));

        // Roles should not be changed as it is only done at registration

        return this.userRepository.save(currentUser);
    }

    // delete user - this simply invalidates the user via UserPrincipal
}
