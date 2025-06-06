package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FolderDTO {
//
    private String folderName;
    private String description;
    private String clientId;  // For existing client
    private boolean archived;
    private boolean favorite;

    // New client details, used only when clientId is not provided
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String assignedAccountantId; //  for companies
}
