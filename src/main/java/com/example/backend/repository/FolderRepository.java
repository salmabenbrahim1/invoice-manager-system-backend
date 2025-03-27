package com.example.backend.repository;

import com.example.backend.model.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FolderRepository extends MongoRepository<Folder, String> {
}
