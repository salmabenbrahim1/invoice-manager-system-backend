package com.example.backend.repository;
//
import com.example.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    List<User> findByCreatedById(String id);
    // Add this new method
    @Query(value = "{ 'email' : { $regex : ?0, $options: 'i' } }", exists = true)
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRoleAndIdIn(String role, List<String> companyAccountantIds);

    List<User> findByRole(String role);

    @Query("{ 'role': ?0, 'createdBy.$id': ObjectId(?1) }")
    List<User> findByRoleAndCreatedBy(String role, String companyId);


}
