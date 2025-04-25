package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

//Data Transfer Object(DTO) used to capture user registration details.
public class RegisterDTO {
    private String email;
    private String password;
    private String role;
}
