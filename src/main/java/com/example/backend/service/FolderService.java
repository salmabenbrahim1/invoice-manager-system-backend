package com.example.backend.service;

import com.example.backend.model.Client;
import com.example.backend.model.Folder;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//
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


}
