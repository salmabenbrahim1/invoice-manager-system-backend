package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Getter
@Setter
public class User {
    @Id
    private String _id;

    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String password;
    private String role;
    private String companyName;
    private String gender;
    private String cin;

}
