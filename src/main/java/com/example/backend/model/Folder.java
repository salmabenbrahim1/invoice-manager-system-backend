package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection ="folders")
public class Folder {
    @Id
    private String _id;
    private String folderName;
    private String description;
    private String clientId;

    @CreatedDate
    private LocalDateTime createdAt;


    @Transient
    private Client client;


public Folder (String folderName,String description, String clientId){
    this.folderName = folderName;
    this.description = description;
    this.clientId = clientId;
    this.createdAt = LocalDateTime.now();
}


}
