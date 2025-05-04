package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Getter
@Setter
@Document(collection = "clients")
public class Client {

    @Id
    private String id;

    private String name;
    private String email;
    private String phone;

    private String password;

    private boolean isActive = true;

    @DBRef
    private User createdBy; // IndependentAccountant or Company

    @DBRef
    private CompanyAccountant assignedTo; // only if assigned

}
