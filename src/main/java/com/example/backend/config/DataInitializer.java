package com.example.backend.config;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Injecter PasswordEncoder

    @Override
    public void run(String... args) {
        // Vérifier si l'utilisateur admin existe déjà
        Optional<User> adminUserOpt = userRepository.findByEmail("admin@gmail.com");

        if (adminUserOpt.isEmpty()) {
            User adminUser = new User();
            adminUser.setEmail("admin@gmail.com");
            adminUser.setPassword(passwordEncoder.encode("admin123")); // Hachage du mot de passe avant de l'enregistrer
            adminUser.setRole("ADMIN");

            userRepository.save(adminUser);
            System.out.println("Admin user created!");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
