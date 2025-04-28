package com.example.backend.controller;
import com.example.backend.model.*;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.RegisterDTO;

import com.example.backend.dto.AuthResponseDTO;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // Login endpoint
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        if (loginDTO.getEmail() == null || loginDTO.getEmail().isEmpty() ||
                loginDTO.getPassword() == null || loginDTO.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and password are required.");
        }

        Optional<User> userOpt = authService.authenticateUser(loginDTO.getEmail(), loginDTO.getPassword());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Incorrect email or password.");
        }

        User user = userOpt.get();



        // Generate JWT token
        String token = authService.generateToken(user);

        // Prepare response
        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(token);
        response.setRole(user.getRole());
        response.setEmail(user.getEmail());

        response.setAdmin(user instanceof Admin);
        response.setCompany(user instanceof Company);
        response.setIndependentAccountant(user instanceof IndependentAccountant);

        return ResponseEntity.ok(response);
    }

    // Token validation endpoint
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String token = authHeader.substring(7);
            if (!authService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = authService.getUserFromToken(token);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Return minimal user data needed by frontend
            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("role", user.getRole());
            response.put("isAdmin", user instanceof Admin);
            response.put("isCompany", user instanceof Company);
            response.put("isAccountant", user instanceof IndependentAccountant);
            response.put("isCompanyAccountant", user instanceof CompanyAccountant);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // Register endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register( @Valid @RequestBody RegisterDTO registerDTO) {
        if (registerDTO.getEmail() == null || registerDTO.getEmail().isEmpty() ||
                registerDTO.getPassword() == null || registerDTO.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Email and password are required.");
        }

        try {
            User user = authService.registerUser(
                    registerDTO.getEmail(),
                    registerDTO.getPassword(),
                    registerDTO.getRole()
            );

            // Generate JWT token
            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(authService.generateToken(user));
            response.setRole(user.getRole());
            response.setEmail(user.getEmail());
            response.setAdmin(user instanceof Admin);
            response.setCompany(user instanceof Company);
            response.setIndependentAccountant(user instanceof IndependentAccountant);
            response.setCompanyAccountant(user instanceof CompanyAccountant);


            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}