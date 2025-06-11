package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
//
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection ="invoices")
public class Invoice {

   @Id
    private String id;
    private String img;
    private String invoiceName;
    private String status;

    //the date of the invoice when added to the folder
    private LocalDateTime addedAt;

    private String folderId;
    private boolean archived = false;

    private String invoiceNumber;
   private String invoiceDate;
   private String dueDate;
   private String currency;

   private String sellerName;
   private String sellerAddress;
   private String sellerPhone;
   private String sellerEmail;


   private String customerName;
   private String customerAddress;
   private String customerPhone;
    private String customerEmail;


    private String tvaNumber;
   private String tvaRate;
   private String tva;
   private String ht;
   private String ttc;
   private String discount;

}
