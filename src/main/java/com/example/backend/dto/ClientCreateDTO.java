package com.example.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientCreateDTO {
    private String name;
    private String email;
    private String phone;

    private String assignedAccountantId; // optional


}
