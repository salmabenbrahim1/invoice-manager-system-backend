package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;
//
@Getter
@Setter
public class Company extends User {


    private String companyName;
    //eventually adding serial number

    // List of Internal accountants IDs
    private List<String> accountantIds = new ArrayList<>();

    // List of the company clients IDs
    private List<String> clientIds = new ArrayList<>();

    public Company() {
        this.setRole("COMPANY");
    }

}


