package com.example.backend.repository;

import com.example.backend.model.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
//
import java.util.List;

public interface FolderRepository extends MongoRepository<Folder, String> {
    List<Folder> findByCreatedById(String createdById);

}
