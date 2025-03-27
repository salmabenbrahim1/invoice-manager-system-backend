package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Ajouter PasswordEncoder

    // Recherche de l'utilisateur par email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Enregistrer un utilisateur avec un mot de passe crypté
    public User saveUser(User user) {
        // Crypter le mot de passe avant de l'enregistrer
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Créer un nouvel utilisateur
    public User createUser(User user) {
        // Vérification si l'email existe déjà
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Générer un mot de passe aléatoire et crypter le mot de passe
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(generatedPassword));  // Crypter le mot de passe généré

        // Enregistrer l'utilisateur dans la base de données
        User savedUser = userRepository.save(user);

        // Envoyer l'email de bienvenue
        String emailText = "Hello "  +
                "Your account has been created successfully!\n\n" +
                "Email: " + user.getEmail() + "\n" +
                "Password: " + generatedPassword + "\n\n" +
                "Please log in and verify your account.\n\n" +
                "Thank you!";
        emailService.sendEmail(user.getEmail(), "Welcome to our platform!", emailText);

        return savedUser;
    }

    // Récupérer tous les utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Mettre à jour un utilisateur
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

    // Supprimer un utilisateur
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
