package com.example.backend.repository;

import com.example.backend.model.Client;
import com.example.backend.model.Company;
import com.example.backend.model.IndependentAccountant;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
//
@Repository
public interface ClientRepository extends MongoRepository<Client, String> {
    List<Client> findByCreatedBy_Id(String id);

    Optional<Client> findById(String id);

    // Query by the assignedToId field (direct string match)
    List<Client> findByAssignedToId(String accountantId);

    // Alternative query using @DBRef
    @Query("{ 'assignedTo.$id': ?0 }")
    List<Client> findByAssignedTo_Id(ObjectId accountantId);







}