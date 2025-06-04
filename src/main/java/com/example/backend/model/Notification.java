package com.example.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    @DBRef
    private CompanyAccountant accountant;

    @DBRef
    private Client client; // ✅ ajout du client

    private String message;

    private LocalDateTime createdAt;

    private boolean read;

    public Notification() {
        this.createdAt = LocalDateTime.now();
        this.read = false;
    }
}
