package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//
//Data Transfer Object(DTO) used to capture user login details.
public class LoginDTO {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}

