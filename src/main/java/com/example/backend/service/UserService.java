package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, EmailService emailService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User createUser(User user) {
        userRepository.findByEmail(user.getEmail())
                .ifPresent(u -> { throw new IllegalArgumentException("Email already exists"); });

        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(generatedPassword));

        User savedUser = userRepository.save(user);
        sendWelcomeEmail(user, generatedPassword);

        return savedUser;
    }

    private void sendWelcomeEmail(User user, String password) {
        String emailText = String.format(
                "Hello,\n\nWelcome to Invoice Management! Your account has been created.\n\n" +
                        "Email: %s\nPassword: %s\n\n" +
                        "Please login and verify your account.\n\nBest regards,\nThe Team",
                user.getEmail(), password);

        emailService.sendEmail(user.getEmail(), "Welcome!", emailText);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(String id, User user) {
        User existingUser = getUserById(id);
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

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public Map<String, Long> getDashboardStats() {
        List<User> users = getAllUsers();

        return Map.of(
                "totalCompanies", countUsersByRole(users, "COMPANY"),
                "totalAccountIndependents", countUsersByRole(users, "INDEPENDENT ACCOUNTANT")
        );
    }

    private long countUsersByRole(List<User> users, String role) {
        return users.stream().filter(u -> role.equals(u.getRole())).count();
    }

    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    public User updateProfile(String userId, User updatedData) {
        User user = getUserById(userId);

        if ("COMPANY".equals(user.getRole())) {
            user.setCompanyName(updatedData.getCompanyName());
            user.setPhone(updatedData.getPhone());
        } else {
            user.setFirstName(updatedData.getFirstName());
            user.setLastName(updatedData.getLastName());
            user.setCin(updatedData.getCin());
            user.setGender(updatedData.getGender());
            user.setPhone(updatedData.getPhone());
        }

        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }

        return userRepository.save(user);
    }




    // Activied compte
    public void toggleUserActivation(String id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActive(!user.isActive());
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }



}