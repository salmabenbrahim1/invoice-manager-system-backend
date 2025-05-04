package com.example.backend.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data

public class InvoiceCreateDTO {
    private String img;
    private String invoiceName;
    private String status;
    private String folderId;
    private MultipartFile file;
}
