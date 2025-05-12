package com.example.backend.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
//
public class InvoiceCreateDTO {
    //Uploaded data
    private String img;
    private String invoiceName;
    private String status;
    private String folderId;
    private MultipartFile file;


    //Extracted fields from invoice
    private String invoiceNumber;
    private String invoiceDate;
    private String dueDate;
    private String currency;

    // The entity issuing the invoice
    private String sellerName;
    private String sellerAddress;
    private String sellerPhone;
    private String sellerSiretNumber;

    // The customer for whom the invoice is issued
    private String customerName;
    private String customerAddress;
    private String customerPhone;

    //Amounts
    private String tvaNumber;
    private String tvaRate;
    private Double tva;
    private Double ht;
    private Double ttc;
    private Double discount;


}
