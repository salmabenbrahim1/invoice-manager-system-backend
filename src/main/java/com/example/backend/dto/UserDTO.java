package com.example.backend.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

//
@Data
public class UserDTO {
    private String email;
    private String phone;
    private String role;

    private String createdById;
    private transient MultipartFile image;

    // For individual accountants
    private String firstName;
    private String lastName;
    private String cin;
    private String gender;

    // For companies
    private String fiscalNumber;
    private String companyName;


}
