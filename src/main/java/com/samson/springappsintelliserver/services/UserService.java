package com.samson.springappsintelliserver.services;

import com.samson.springappsintelliserver.models.Users;
import com.samson.springappsintelliserver.providers.PasswordProvider;
import com.samson.springappsintelliserver.repositories.UserRepository;
import com.samson.springappsintelliserver.types.UserType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

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

        // TODO: passwords should not be updated here
        if (updatedUser.getPassword() != null)
            // currentUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            currentUser.setPassword(currentUser.getPassword());

        // Roles should not be changed as it is only done at registration

        return this.userRepository.save(currentUser);
    }

    public ResponseEntity<?> logout() {
        // invalidate the token

        return ResponseEntity.ok().build();
    }

    public Users updatePassword(@NonNull Integer userId, @NonNull PasswordProvider provider) {
        // extract user from the token
        String username = extractUsername();

        Users currentUser = this.userRepository.findById(userId).orElseThrow(
                () -> new IllegalStateException("User not found"));

        // verify that the user is updating their own account
        if (!currentUser.getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this account");

        // compare the current password with the one in the database
        if (!passwordEncoder.matches(provider.getCurrentPassword(), currentUser.getPassword()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid current password");

        // update the password
        currentUser.setPassword(passwordEncoder.encode(provider.getNewPassword()));

        return this.userRepository.save(currentUser);

        // might be a flaw in how the user is fetched -> by id (sql injection) or by username (userdetails)
    }

    private String extractUsername() {
        // extract username from the token via servlet request
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization").substring(7);

        return jwtService.extractUsername(token);
    }

    // delete user - this simply invalidates the user via UserPrincipal
}
