package com.example.backend.dto;

import com.example.backend.model.Client;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AssignedClientDTO {
    private Client client;
    private LocalDateTime assignedAt;
    private String companyName;

}
