package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

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

    @Transient
    private Client client;

public Folder (String folderName,String description, String clientId){
    this.folderName = folderName;
    this.description = description;
    this.clientId = clientId;
}


}
