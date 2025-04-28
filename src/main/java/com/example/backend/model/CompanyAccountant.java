package com.example.backend.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CompanyAccountant extends User {

    private String firstName;
    private String lastName;
    private String gender;
    private String cin;
    private List<String> folderIds = new ArrayList<>();

    public CompanyAccountant() {
        this.setRole("INTERNAL ACCOUNTANT");
    }
}

