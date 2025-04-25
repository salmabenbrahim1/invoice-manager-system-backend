package com.example.backend.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "folders")
@Getter
@Setter
public class Folder {
    @Id
    private String id;
    private String folderName;
    private String description;
    private String clientId;

    // List of invoice IDs
    private List<String> invoiceIds =  new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @Transient
    private Client client;


    public Folder(String folderName, String description, String clientId) {
        this.folderName = folderName;
        this.description = description;
        this.clientId = clientId;
        this.createdAt = LocalDateTime.now();
    }
}