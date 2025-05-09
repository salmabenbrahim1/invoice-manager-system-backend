package com.example.backend.repository;

import com.example.backend.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
//
public interface InvoiceRepository extends MongoRepository<Invoice,String> {
    List<Invoice> findByFolderId(String folderId);
    void deleteByFolderId(String folderId);

    // Count all invoices
    long count();

    // Count invoices by user type
    @Query(value = "{'userType': ?0}", count = true)
    long countByUserType(String userType);

    //  count by specific user roles
    @Query(value = "{'user.role': ?0}", count = true)
    long countByUserRole(String role);

    long countByStatus(String status);

}

