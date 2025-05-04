package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtils;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    // Method for login
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check if the user is active
            if (!user.isActive()) {
                throw new IllegalArgumentException("User is not active");
            }
            // Check if password matches
            if (passwordEncoder.matches(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }


    // Method for registration
    public User registerUser(String email, String password, String role) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email is already in use.");
        }
        // Validate the role
        if (!isValidRole(role)) {
            throw new IllegalArgumentException("Invalid role specified");
        }

        User user;
        switch(role.toUpperCase()) {
            case "ADMIN":
                user = new Admin();
                break;
            case "COMPANY":
                user = new Company();
                break;
            case "INDEPENDENT_ACCOUNTANT":
                user = new IndependentAccountant();
                break;
            case "INTERNAL_ACCOUNTANT":
                user = new CompanyAccountant();
                break;
            default:
                throw new IllegalArgumentException("Invalid role specified");
        }
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);

        return userRepository.save(user);
    }
    private boolean isValidRole(String role) {
        return role.equalsIgnoreCase("ADMIN") ||
                role.equalsIgnoreCase("COMPANY") ||
                role.equalsIgnoreCase("INDEPENDENT_ACCOUNTANT") ||
                role.equalsIgnoreCase("INTERNAL_ACCOUNTANT");
    }


    // JWT token generation
    public String generateToken(User user) {
        return jwtUtils.generateToken(user);
    }

    public String generateRefreshToken(User user) {
        return jwtUtils.generateRefreshToken(user);
    }
    // Validate JWT token
    public boolean validateToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return false;
            }

            // Check if token is expired
            if (jwtUtils.isTokenExpired(token)) {
                return false;
            }

            // Extract user email from token
            String email = jwtUtils.extractUsername(token);
            if (email == null || email.isEmpty()) {
                return false;
            }

            // Verify user exists and is active
            Optional<User> userOpt = userRepository.findByEmail(email);
            return userOpt.isPresent() && userOpt.get().isActive();

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    // Get User from JWT token
    public User getUserFromToken(String token) {
        try {
            String email = jwtUtils.extractUsername(token);
            return userRepository.findByEmail(email).orElse(null);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
