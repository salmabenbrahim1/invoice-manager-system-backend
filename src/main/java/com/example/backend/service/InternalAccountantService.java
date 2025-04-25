package com.example.backend.service;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalAccountantService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User add(User user, String companyId) {
        if (user.getEmail() == null || user.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        user.setId(UUID.randomUUID().toString());
        user.setRole("INTERNAL_ACCOUNTANT");
        user.setCompanyId(companyId);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);
        return userRepository.save(user);
    }

    public List<User> findAllByCompany(String companyId) {
        return userRepository.findByRoleAndCompanyId("INTERNAL_ACCOUNTANT", companyId);
    }

    public User update(User updatedUser) {
        User existing = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        existing.setFirstName(updatedUser.getFirstName());
        existing.setLastName(updatedUser.getLastName());
        existing.setEmail(updatedUser.getEmail());
        existing.setActive(updatedUser.isActive());
        // Ne pas changer le mot de passe ici sauf s'il y a une option dédiée
        return userRepository.save(existing);
    }


    public void delete(String id) {
        userRepository.deleteById(id);
    }
}
