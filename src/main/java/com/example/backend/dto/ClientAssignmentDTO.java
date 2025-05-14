package com.example.backend.dto;

import com.example.backend.model.Client;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientAssignmentDTO {
    private String clientName;
    private String clientEmail;
    private LocalDateTime assignedAt;

    public ClientAssignmentDTO(Client client, LocalDateTime assignedAt) {
        this.clientName = client.getName();
        this.clientEmail = client.getEmail();
        this.assignedAt = assignedAt;
    }
}

