package com.example.backend.controller;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PasswordResetController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService; // Injection de UserService

    @Autowired
    public PasswordResetController(UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder, UserService userService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService; // Injecter UserService
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        // Vérifier si l'utilisateur existe
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("L'utilisateur n'existe pas");
        }

        // Utilisation de la méthode generateBase64Password() du UserService
        String newPassword = userService.generateBase64Password();

        // Encoder le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Envoyer un e-mail avec le nouveau mot de passe
        String subject = "Your Password Has Been Reset Successfully";

        // Creating an email body
        String body;
        body = String.format(
                "<html>" +
                        "<body>" +
                        "<p>Dear %s,</p>" +
                        "<p>We are pleased to inform you that your password has been successfully reset.</p>" +
                        "<p>Below are your new account credentials:</p>" +
                        "<p><b>Email</b>: %s</p>" +
                        "<p><b>New Password</b>: %s</p>" +
                        "<p>If you have any questions or need assistance, feel free to contact our support team.</p>" +
                        "<p>Best regards,<br>Invox Team</p>" +
                        "</body>" +
                        "</html>",
                user.getEmail(), user.getEmail(), newPassword);


        boolean emailSent = emailService.sendEmail(user.getEmail(), subject, body);

        if (emailSent) {
            return ResponseEntity.ok("Un nouveau mot de passe a été envoyé par e-mail.");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'envoi de l'e-mail.");
        }
    }
}
