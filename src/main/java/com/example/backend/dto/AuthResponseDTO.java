package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
 public class AuthResponseDTO {

    private String token;
    private String role;
    private String email;
    private boolean admin;
    private boolean company;
    private boolean independentAccountant;
    private boolean companyAccountant;
   private String refreshToken;

//
   private Date expiration;



}
