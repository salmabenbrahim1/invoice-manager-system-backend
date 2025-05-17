package com.example.backend.service;

import com.example.backend.model.Client;
import com.example.backend.model.Folder;
import com.example.backend.model.Invoice;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.FolderRepository;
import com.example.backend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//
@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;
    @Autowired
    private ClientRepository clientRepository;
    private final InvoiceRepository invoiceRepository;


    public FolderService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }


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

    public Folder updateFolder(String folderId, Folder updatedFolder) {
        Optional<Folder> existingFolderOpt = folderRepository.findById(folderId);
        if (existingFolderOpt.isEmpty()) {
            throw new RuntimeException("Folder not found with id: " + folderId);
        }

        Folder existingFolder = existingFolderOpt.get();
        existingFolder.setFolderName(updatedFolder.getFolderName());
        existingFolder.setDescription(updatedFolder.getDescription());
        return folderRepository.save(existingFolder);


    }

    public void deleteFolder(String folderId) {
        if (!folderRepository.existsById(folderId)) {
            throw new RuntimeException("Folder not found with id: " + folderId);
        }
        folderRepository.deleteById(folderId);
    }

    public void archiveFolder(String folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        folder.setArchived(true);
        folder.setArchivedAt(LocalDateTime.now());

        folderRepository.save(folder);

        // Archiver toutes les factures du dossier
        List<Invoice> invoices = invoiceRepository.findByFolderId(folderId);
        for (Invoice invoice : invoices) {
            invoice.setArchived(true);

        }
        invoiceRepository.saveAll(invoices);
    }

    public void unarchiveFolder(String folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        folder.setArchived(false);
        folder.setArchivedAt(null);
        folderRepository.save(folder);

        // Désarchiver toutes les factures du dossier
        List<Invoice> invoices = invoiceRepository.findByFolderId(folderId);
        for (Invoice invoice : invoices) {
            invoice.setArchived(false);
        }
        invoiceRepository.saveAll(invoices);
    }


    public void markAsFavorite(String id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        folder.setFavorite(true);
        folderRepository.save(folder);
    }

    public void unmarkAsFavorite(String id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        folder.setFavorite(false);
        folderRepository.save(folder);
    }



}
