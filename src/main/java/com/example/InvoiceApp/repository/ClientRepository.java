package com.example.InvoiceApp.repository;

import com.example.InvoiceApp.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ClientRepository extends MongoRepository<Client,String> {
}
