package com.samson.springappsintelliserver.services;

import com.samson.springappsintelliserver.models.Users;
import com.samson.springappsintelliserver.providers.PasswordProvider;
import com.samson.springappsintelliserver.repositories.UserRepository;
import com.samson.springappsintelliserver.types.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

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
    
    private boolean isDuplicateUser(@NonNull Users user) {
        List<Users> usersList = this.userRepository.findAll();
        
        for (Users candidate : usersList) 
            if (candidate.getUsername().equals(user.getUsername())) return true;
        
        return false;
    }

    public Users registerUser(@NonNull Users user) {
        // there should be a check to ensure that the user does not already exist
        // this should be done by checking the username
        
        if (isDuplicateUser(user))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        
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
        // this is done by simply removing the token from the client side
        // there is no need to invalidate the token on the server side
        // as the token is stateless

        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String token = request.getHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                HttpSession session = request.getSession(false);

                if (session != null) session.invalidate();
            }
            
            return ResponseEntity.ok().body(Map.of("message", "Logout successful"));
            
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Logout failed"));
        }
    }

    public Users updatePassword(@NonNull Integer userId, @NonNull PasswordProvider provider) {
        // extract user from the token
        String username = extractUsername();

        Users currentUser = this.userRepository.findById(userId).orElseThrow(
                () -> new IllegalStateException("User not found")
        );

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

    public ResponseEntity<HttpStatusCode> deleteUser(Integer userId) {
        // fetch the user by id
        Users user = this.userRepository.findById(userId).orElseThrow(
                () -> new IllegalStateException("User not found"));

        // get the username from the token
        String username = extractUsername();

        // verify that the user is deleting their own account
        if (!user.getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this account");

        // delete the user
        this.userRepository.delete(user);

        return ResponseEntity.ok().build();
    }

    // delete user - this simply invalidates the user via UserPrincipal
    
    public String getUsernameFromToken(@NonNull Token token) {
        // extract the username from the token
        return jwtService.extractUsername(token.getToken());
    }
}
