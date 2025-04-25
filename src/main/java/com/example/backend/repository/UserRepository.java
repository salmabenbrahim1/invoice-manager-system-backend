package com.example.backend.repository;

import com.example.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    // Nouvelle méthode pour les comptables internes
    List<User> findByRoleAndCompanyId(String role, String companyId);
}
