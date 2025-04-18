package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.service.UserService;
import com.example.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing password");
        }

        Optional<User> existingUser = userService.findByEmail(user.getEmail());

        if (existingUser.isPresent()) {
            User foundUser = existingUser.get();

            // Checks if the user is active
            if (!foundUser.isActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your account is deactivated.");
            }

            // Check the password
            if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
                String jwtToken = jwtUtils.generateToken(foundUser);

                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("token", jwtToken);
                responseBody.put("role", foundUser.getRole());
                responseBody.put("email", foundUser.getEmail());

                return ResponseEntity.ok(responseBody);
            } else {
                return ResponseEntity.badRequest().body("Incorrect email or password");
            }
        } else {
            return ResponseEntity.badRequest().body("Incorrect email or password");
        }
    }



    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Vérifier si le mot de passe est vide
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Missing password");
        }

        // Hasher password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userService.saveUser(user);

        return ResponseEntity.ok(savedUser);
    }
}
