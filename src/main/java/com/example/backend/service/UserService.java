package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(generatedPassword));

        User savedUser = userRepository.save(user);

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

        long totalCompanies = users.stream()
                .filter(user -> "COMPANY".equals(user.getRole()))
                .count();

        long totalAccountIndependents = users.stream()
                .filter(user -> "INDEPENDENT ACCOUNTANT".equals(user.getRole()))
                .count();

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalCompanies", totalCompanies);
        stats.put("totalAccountIndependents", totalAccountIndependents);

        return stats;
    }

    // Spring Security
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Return the UserDetails object
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())  // Use the email as the username
                .password(user.getPassword())   // Add the encrypted password
                .roles(user.getRole())          // Add the user's role
                .build();
    }

}
