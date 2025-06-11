package com.example.backend.dto;


import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
//
public class InvoiceDTO {
    //Uploaded data
    private String img;
    private String invoiceName;
    private String status;
    private String folderId;
    private MultipartFile file;
    private boolean archived;


    //Extracted fields from invoice
    private String invoiceNumber;
    private String invoiceDate;
    private String dueDate;
    private String currency;

    // The entity issuing the invoice
    private String sellerName;
    private String sellerAddress;
    private String sellerPhone;
    private String sellerEmail;

    // The customer for whom the invoice is issued
    private String customerName;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;


    //Amounts
    private String tvaNumber;
    private String tvaRate;
    private String tva;
    private String ht;
    private String ttc;
    private String discount;


}
