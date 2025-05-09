package com.example.backend.controller;

import com.example.backend.model.*;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import com.example.backend.dto.LoginDTO;
import com.example.backend.dto.RegisterDTO;
import com.example.backend.dto.AuthResponseDTO;
import com.example.backend.dto.RefreshTokenRequest;
import com.example.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
//
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
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        Optional<User> userOpt = authService.authenticateUser(loginDTO.getEmail(), loginDTO.getPassword());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Incorrect email or password."));
        }

        User user = userOpt.get();



        AuthResponseDTO response = new AuthResponseDTO();
        response.setToken(authService.generateToken(user));
        response.setRefreshToken(authService.generateRefreshToken(user));
        response.setRole(user.getRole());
        response.setEmail(user.getEmail());
        response.setAdmin(user instanceof Admin);
        response.setCompany(user instanceof Company);
        response.setIndependentAccountant(user instanceof IndependentAccountant);
        response.setCompanyAccountant(user instanceof CompanyAccountant);

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
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO registerDTO) {
        try {
            User user = authService.registerUser(
                    registerDTO.getEmail(),
                    registerDTO.getPassword(),
                    registerDTO.getRole()
            );

            AuthResponseDTO response = new AuthResponseDTO();
            response.setToken(authService.generateToken(user));
            response.setRefreshToken(authService.generateRefreshToken(user));
            response.setRole(user.getRole());
            response.setEmail(user.getEmail());
            response.setAdmin(user instanceof Admin);
            response.setCompany(user instanceof Company);
            response.setIndependentAccountant(user instanceof IndependentAccountant);
            response.setCompanyAccountant(user instanceof CompanyAccountant);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token is missing or invalid"));
        }

        String refreshToken = authHeader.substring(7);

        // Validate the refresh token
        if (!authService.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
        }

        User user = authService.getUserFromToken(refreshToken);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid refresh token"));
        }

        // Generate a new access token using the user
        String newAccessToken = authService.generateToken(user);

        // Return the new access token
        Map<String, String> response = new HashMap<>();
        response.put("token", newAccessToken);

        return ResponseEntity.ok(response);
    }

    // Global handler for validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

}
