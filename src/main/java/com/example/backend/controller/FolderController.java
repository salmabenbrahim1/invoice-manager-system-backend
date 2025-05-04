package com.example.backend.controller;

import com.example.backend.dto.FolderCreateDTO;
import com.example.backend.model.*;
import com.example.backend.service.ClientService;
import com.example.backend.service.FolderService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final ClientService clientService;
    private final UserService userService;

    // Create a new folder, and add existing or new client

    @PostMapping
    public ResponseEntity<Folder> createFolder(@RequestBody FolderCreateDTO folderCreateDTO, Principal principal) {
        try {
            // Get the authenticated user (creator - accountant or company)
            User creator = userService.getCurrentUser(principal);

            // Check if the client exists or create a new client
            Client client;
            if (folderCreateDTO.getClientId() != null && !folderCreateDTO.getClientId().isEmpty()) {
                // If clientId is provided, fetch the existing client
                client = clientService.getClientById(folderCreateDTO.getClientId());
                if (client == null) {
                    // If client not found, return an error
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
            } else {
                // If clientId is not provided, create a new client
                client = clientService.createClient(creator, folderCreateDTO.getClientName(),
                        folderCreateDTO.getClientEmail(), folderCreateDTO.getClientPhone(),
                        folderCreateDTO.getAssignedAccountantId());
            }

            // Determine the creator's role
            Role creatorRole = null;
            if (creator instanceof Company) {
                creatorRole = Role.COMPANY; // Company role
            } else if (creator instanceof IndependentAccountant) {
                creatorRole = Role.INDEPENDENT_ACCOUNTANT; // Independent Accountant role
            } else if (creator instanceof Admin) {
                creatorRole = Role.ADMIN; // Admin role
            }

            // Create the folder linked to the client and authenticated user
            Folder folder = new Folder(
                    folderCreateDTO.getFolderName(),
                    folderCreateDTO.getDescription(),
                    client.getId(), // Use client ID (existing or newly created)
                    creator.getId(),
                    creatorRole // Pass the Role directly
            );

            // Create the folder
            Folder createdFolder = folderService.createFolder(folder);

            return new ResponseEntity<>(createdFolder, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Fetch folders created by the authenticated user
    @GetMapping("/my-folders")
    public ResponseEntity<List<Folder>> getFoldersByCreator(Principal principal) {
        try {
            // Get the authenticated user (creator - accountant or company)
            User creator = userService.getCurrentUser(principal);

            // Fetch folders created by this user
            List<Folder> folders = folderService.getFoldersByCreatorId(creator.getId());

            // For each folder, fetch the associated client and set the client details
            for (Folder folder : folders) {
                // Fetch the client for the folder
                Client client = clientService.getClientById(folder.getClientId());
                if (client != null) {
                    // Set the client information to the folder
                    folder.setClient(client); // This will populate the client field dynamically
                }
            }

            return new ResponseEntity<>(folders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    // Update a folder
    @PutMapping("/{folderId}")
    public ResponseEntity<Folder> updateFolder(@PathVariable String folderId, @RequestBody Folder updatedFolder) {
        try {
            Folder folder = folderService.updateFolder(folderId, updatedFolder);
            return new ResponseEntity<>(folder, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    // Delete a folder
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String folderId) {
        try {
            folderService.deleteFolder(folderId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}


