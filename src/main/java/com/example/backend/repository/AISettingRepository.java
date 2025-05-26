package com.example.backend.repository;
import com.example.backend.model.AISetting;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AISettingRepository extends MongoRepository<AISetting, String> {
}
