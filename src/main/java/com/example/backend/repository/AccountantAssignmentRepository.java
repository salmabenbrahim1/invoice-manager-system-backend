package com.example.backend.repository;

import com.example.backend.model.AccountantAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AccountantAssignmentRepository extends MongoRepository<AccountantAssignment, String> {
    List<AccountantAssignment> findByAccountant_Id(String accountantId);
    List<AccountantAssignment> findByClient_Id(String clientId);
}
