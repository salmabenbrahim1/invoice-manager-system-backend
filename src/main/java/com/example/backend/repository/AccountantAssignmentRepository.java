package com.example.backend.repository;

import com.example.backend.model.AccountantAssignment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AccountantAssignmentRepository extends MongoRepository<AccountantAssignment, String> {
    List<AccountantAssignment> findByAccountant_Id(String accountantId);
    List<AccountantAssignment> findByClient_Id(String clientId);
    @Query(value = "{ 'client.$id' : ?0 }", delete = true)
    void deleteByClientId(ObjectId clientId);    void deleteByClientId(String clientId);
    @Query("{ 'client.$id' : ?0 }")
    List<AccountantAssignment> findByClientId(String clientId);


}
