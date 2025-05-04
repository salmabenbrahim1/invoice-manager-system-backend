package com.example.backend.dto;


import lombok.Data;

@Data
public class UserProfileUpdateDTO {
    private String email;
    private String phone;
    private String password; // nouveau mot de passe (optionnel)

    // Pour les individus
    private String firstName;
    private String lastName;
    private String cin;
    private String gender;

    // Pour les entreprises
    private String companyName;
}
