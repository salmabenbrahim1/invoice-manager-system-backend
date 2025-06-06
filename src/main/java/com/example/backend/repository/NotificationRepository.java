package com.example.backend.repository;

import com.example.backend.model.CompanyAccountant;
import com.example.backend.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {

    List<Notification> findByAccountantOrderByCreatedAtDesc(CompanyAccountant accountant);

    Long countByAccountantAndReadFalse(CompanyAccountant accountant);

    List<Notification> findByAccountantAndReadFalse(CompanyAccountant accountant);
}
