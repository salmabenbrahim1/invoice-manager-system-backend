package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;  //  PasswordEncoder

    // Search user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Register a user with an encrypted password
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Create a new user
    public User createUser(User user) {
        // Check if the email already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Generate a random password and encrypt the password
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(generatedPassword));  // Crypter le mot de passe généré

        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Send the welcome email
        String emailText = "Hello "  + ",\n\n" +
                "Welcome to Invoice Management! Your account has been successfully created.\n\n" +
                "Email: " + user.getEmail() + "\n" +
                "Password: " + generatedPassword + "\n\n" +
                "Please login to start managing your invoices efficiently. Don't forget to verify your account for full access.\n\n" +
                "If you have any questions, feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "The Invoice Management Team";
        emailService.sendEmail(user.getEmail(),  "Welcome to Invoice Management!", emailText);

        return savedUser;
    }

    // Retrieve all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // Update a user
    public User updateUser(String id, User user) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        existingUser.setEmail(user.getEmail());
        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setPhone(user.getPhone());
        existingUser.setRole(user.getRole());
        existingUser.setCompanyName(user.getCompanyName());
        existingUser.setGender(user.getGender());
        existingUser.setCin(user.getCin());
        return userRepository.save(existingUser);
    }

    // Delete a user
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    // Get dashboard stats
    public Map<String, Long> getDashboardStats() {
        List<User> users = userRepository.findAll();

        // Count the number of 'COMPANY' users
        long totalCompanies = users.stream()
                .filter(user -> "COMPANY".equals(user.getRole()))
                .count();

        // Count the number of 'INDEPENDENT ACCOUNTANT' users
        long totalAccountIndependents = users.stream()
                .filter(user -> "INDEPENDENT ACCOUNTANT".equals(user.getRole()))
                .count();

        // Return stats in a map
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCompanies", totalCompanies);
        stats.put("totalAccountIndependents", totalAccountIndependents);

        return stats;
    }

}
