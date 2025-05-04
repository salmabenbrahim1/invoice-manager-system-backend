package com.example.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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
 // Ajout des données extraites de la facture
 private String clientName;
 private String siretNumber;
 private String invoiceNumber;
 private String invoiceDate;
 private String tvaNumber;
 private Double tva;
 private Double ht;
 private Double ttc;
 private String currency;
}
