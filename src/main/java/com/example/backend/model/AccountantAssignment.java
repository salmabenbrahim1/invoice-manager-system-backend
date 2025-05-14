package com.example.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "accountant_assignments")


public class AccountantAssignment {


    @Id
    private String id;

    @DBRef
    private CompanyAccountant accountant;

    @DBRef
    private Client client;

    private LocalDateTime assignedAt;

    private String companyName;



    public AccountantAssignment() {
        this.assignedAt = LocalDateTime.now();
    }


}
