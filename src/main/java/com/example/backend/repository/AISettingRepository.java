package com.example.backend.repository;
import com.example.backend.model.AISetting;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AISettingRepository extends MongoRepository<AISetting, String> {
    Optional<AISetting> findTopByOrderByIdDesc();
}
