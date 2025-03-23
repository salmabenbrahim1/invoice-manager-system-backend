package com.example.InvoiceApp.repository;

import com.example.InvoiceApp.model.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FolderRepository extends MongoRepository<Folder, String> {
}
