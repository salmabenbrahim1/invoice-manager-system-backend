package com.example.backend.service;

import com.example.backend.model.Client;
import com.example.backend.model.Folder;
import com.example.backend.repository.FolderRepository;
import com.example.backend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {
    @Autowired
    private  FolderRepository folderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

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
            folder.setClientId(savedClient.getId()); //set the new client's ID in the folders collection
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
        invoiceRepository.deleteByFolderId(id);

        folderRepository.deleteById(id);
    }
    public void removeInvoiceFromFolder(String invoiceId, String folderId) {
        if (invoiceId == null || invoiceId.isEmpty()) {
            throw new IllegalArgumentException("Invoice ID cannot be null or empty");
        }

        Folder folder = folderRepository.findById(folderId).orElse(null);

        if (folder == null) {
            throw new RuntimeException("Folder not found with ID: " + folderId);
        }

        boolean removed = folder.getInvoiceIds().removeIf(id -> id.equals(invoiceId));

        if (removed) {
            folderRepository.save(folder);
        } else {
            System.out.println("Invoice ID not found in folder");
        }
    }


    public Folder updateFolder(String id, Folder updatedFolder) {
        Folder existingFolder = folderRepository.findById(id).orElse(null);
        if (existingFolder == null) {
            throw new RuntimeException("Folder not found with id: " + id);
        }
        // Update folder fields
        existingFolder.setFolderName(updatedFolder.getFolderName());
        existingFolder.setDescription(updatedFolder.getDescription());

        return folderRepository.save(existingFolder);
    }

}