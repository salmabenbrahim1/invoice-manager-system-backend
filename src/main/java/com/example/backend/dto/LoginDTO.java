package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//Data Transfer Object(DTO) used to capture user login details.
public class LoginDTO {
    private String email;
    private String password;
}

