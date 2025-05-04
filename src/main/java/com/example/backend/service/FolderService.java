package com.example.backend.service;

import com.example.backend.model.Client;
import com.example.backend.model.Folder;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private ClientRepository clientRepository;

    // Create folder method
    public Folder createFolder(Folder folder) {
        return folderRepository.save(folder);
    }


    // Get all folders created by a specific user (company or accountant)
    public List<Folder> getFoldersByCreatorId(String creatorId) {
        List<Folder> folders = folderRepository.findByCreatedById(creatorId);
        for (Folder folder : folders) {
            // Fetch client details based on clientId and add it to folder
            Optional<Client> clientOpt;
            clientOpt = clientRepository.findById(folder.getClientId());
            clientOpt.ifPresent(folder::setClient);  // Set the client details on the folder
        }

        return folders;
    }



    public Folder getFolderById(String folderId) {
        return folderRepository.findById(folderId).orElse(null);
    }

    public Folder addFolder(Folder folder, Client newClient) {
        if (folder == null) {
            throw new IllegalArgumentException("Folder cannot be null");
    public Folder updateFolder(String folderId, Folder updatedFolder) {
        Optional<Folder> existingFolderOpt = folderRepository.findById(folderId);
        if (existingFolderOpt.isEmpty()) {
            throw new RuntimeException("Folder not found with id: " + folderId);
        }

            // Verify client exists
            Client existingClient = clientService.getClientById(clientId);
            if (existingClient == null) {
                throw new RuntimeException("Client not found with ID: " + clientId);
            }
        }
        folder.setInvoiceCount(0);
        return folderRepository.save(folder);
    }
        Folder existingFolder = existingFolderOpt.get();
        existingFolder.setFolderName(updatedFolder.getFolderName());
        existingFolder.setDescription(updatedFolder.getDescription());
        return folderRepository.save(existingFolder);


    }
    public void removeInvoiceFromFolder(String invoiceId, String folderId) {
        if (invoiceId == null || invoiceId.isEmpty()) {
            throw new IllegalArgumentException("Invoice ID cannot be null or empty");
        }

        // Find the folder by its ID
        Folder folder = folderRepository.findById(folderId).orElse(null);

        if (folder == null) {
            throw new RuntimeException("Folder not found with ID: " + folderId);
        }

        // Delete the invoice from the list of invoices in the file
        boolean removed = folder.getInvoiceIds().removeIf(id -> id.equals(invoiceId));

        if (removed) {
            folder.setInvoiceCount(folder.getInvoiceIds().size()); // updates the count
            folderRepository.save(folder); // save with the new account
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
    public void deleteFolder(String folderId) {
        if (!folderRepository.existsById(folderId)) {
            throw new RuntimeException("Folder not found with id: " + folderId);
        }
        folderRepository.deleteById(folderId);
    }


}
