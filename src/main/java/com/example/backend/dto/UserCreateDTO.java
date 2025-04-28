package com.example.backend.dto;


import lombok.Data;

@Data
public class UserCreateDTO {
    private String email;
    private String phone;
    private String role;

    private String createdById;

    // For individual accountants
    private String firstName;
    private String lastName;
    private String cin;
    private String gender;

    // For companies
    private String companyName;


}
