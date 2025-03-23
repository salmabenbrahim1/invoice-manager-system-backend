package com.example.InvoiceApp.service;

import com.example.InvoiceApp.model.Client;
import com.example.InvoiceApp.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    //fetch the list of clients from DB
    public List<Client> getAllClients(){
        return clientRepository.findAll();
    }
    //get a client by his id
    public Client getClientById(String id){
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }
        return clientRepository.findById(id).orElse(null);
    }
    //add a client to db
    public Client addClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Client cannot be null");
        }
        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Client name cannot be empty");
        }
        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Client email cannot be empty");
        }
        if (client.getPhoneNumber() == null || client.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Client phone number cannot be empty");
        }

        return clientRepository.save(client);
    }
    public void deleteClient(String id) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Client ID cannot be null or empty");
        }
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client not found with ID: " + id);
        }
        clientRepository.deleteById(id);
    }



}
