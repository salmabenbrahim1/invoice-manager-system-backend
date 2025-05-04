package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//
//Data Transfer Object(DTO) used to capture user registration details.
public class RegisterDTO {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String role;
}
