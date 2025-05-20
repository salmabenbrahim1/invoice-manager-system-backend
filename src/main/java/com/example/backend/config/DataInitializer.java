package com.example.backend.config;

import com.example.backend.model.Admin;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
//
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Check if the admin user already exists
        Optional<User> adminUserOpt = userRepository.findByEmail("invox2025@gmail.com");

        if (adminUserOpt.isEmpty()) {
            User adminUser = new Admin();
            adminUser.setEmail("invox2025@gmail.com");
            adminUser.setPassword(passwordEncoder.encode("admin/@8450$q2025"));
            adminUser.setRole("ADMIN");

            userRepository.save(adminUser);
            System.out.println("Admin user created!");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}
