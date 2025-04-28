package com.example.backend.repository;

import com.example.backend.model.Admin;
import com.example.backend.model.Company;
import com.example.backend.model.IndependentAccountant;
import com.example.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    List<User> findByCreatedBy_Id(String id);


}
