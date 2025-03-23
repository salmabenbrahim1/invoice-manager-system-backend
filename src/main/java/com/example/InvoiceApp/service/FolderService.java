package com.example.InvoiceApp.service;

import com.example.InvoiceApp.model.Client;
import com.example.InvoiceApp.model.Folder;
import com.example.InvoiceApp.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FolderService {
    @Autowired
    private  FolderRepository folderRepository;

    @Autowired
    private ClientService clientService;



    public List<Folder> getAllFolders() {
        List<Folder> folders = folderRepository.findAll();
        for (Folder folder : folders) {
            Client client = clientService.getClientById(folder.getClientId());
            if (client != null) {
                folder.setClient(client);
            }
        }
        return folders;
    }


    public Folder getFolderById(String folderId) {
        return folderRepository.findById(folderId).orElse(null);
    }

    public Folder addFolder(Folder folder, Client newClient) {
        if (folder == null) {
            throw new IllegalArgumentException("Folder cannot be null");
        }
        // Scenario 1: New client provided:
        if(newClient!= null){
            //Create a new Client and associate with folder
            Client savedClient = clientService.addClient(newClient);
            folder.setClientId(savedClient.get_id()); //set the new client's ID in the folders collection
        }
        // Scenario 2: Existing client
        else {
            //validate clientId
            String clientId = folder.getClientId();
            if (clientId == null || clientId.isEmpty()) {
                throw new IllegalArgumentException("Client Id must be provided for existing clients");
            }
            // Verify client exists
            Client existingClient = clientService.getClientById(clientId);
            if (existingClient == null) {
                throw new RuntimeException("Client not found with ID: " + clientId);
            }
        }
            return folderRepository.save(folder);
    }

    public void deleteFolder(String id) {
        if (!folderRepository.existsById(id)) {
            throw new RuntimeException("Folder not found with id: " + id);
        }
        folderRepository.deleteById(id);
    }
}