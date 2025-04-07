package com.example.backend.repository;

import com.example.backend.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface InvoiceRepository extends MongoRepository<Invoice,String> {
    List<Invoice> findByFolderId(String folderId);
    void deleteByFolderId(String folderId);

}

