package com.example.backend.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "folders")
public class Folder {
    @Id
    private String id;

    private String folderName;
    private String description;

    private String clientId;         // Reference to the client
    private String createdById;      // ID of the accountant who created the folder
    private Role createdByRole;    // "INDEPENDENT" or "COMPANY"

    private List<String> invoiceIds = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;



    @Transient
    private Client client;

    public Folder(String folderName, String description, String clientId, String createdById, Role createdByRole) {
        this.folderName = folderName;
        this.description = description;
        this.clientId = clientId;
        this.createdById = createdById;
        this.createdByRole = createdByRole;
        this.createdAt = LocalDateTime.now();
    }
}
