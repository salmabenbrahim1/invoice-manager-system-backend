package com.example.backend.controller;
//
import com.example.backend.dto.FolderDTO;
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
    public ResponseEntity<Folder> createFolder(@RequestBody FolderDTO folderDto, Principal principal) {
        try {
            // Get the authenticated user (creator - accountant or company)
            User creator = userService.getCurrentUser(principal);

            // Check if the client exists or create a new client
            Client client;
            if (folderDto.getClientId() != null && !folderDto.getClientId().isEmpty()) {
                // If clientId is provided, fetch the existing client
                client = clientService.getClientById(folderDto.getClientId());
                if (client == null) {
                    // If client not found, return an error
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
            } else {
                // If clientId is not provided, create a new client
                client = clientService.createClient(creator, folderDto.getClientName(),
                        folderDto.getClientEmail(), folderDto.getClientPhone(),
                        folderDto.getAssignedAccountantId());
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
                    folderDto.getFolderName(),
                    folderDto.getDescription(),
                    client.getId(), // Use client ID (existing or newly created)
                    creator.getId(),
                    creatorRole
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


    @GetMapping("/by-internal-accountant/{accountantId}")
    public ResponseEntity<List<Folder>> getFoldersByInternalAccountant(
            @PathVariable String accountantId, Principal principal) {

        try {
            // Récupérer l'utilisateur connecté (doit être une entreprise)
            User currentUser = userService.getCurrentUser(principal);

            if (!(currentUser instanceof Company)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            // Vérifier que l'entreprise a bien créé ce comptable interne
            User accountant = userService.getUserById(accountantId, currentUser);
            if (!(accountant instanceof CompanyAccountant)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            if (!accountant.getCreatedBy().getId().equals(currentUser.getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            // Récupérer les dossiers créés par ce comptable interne
            List<Folder> folders = folderService.getFoldersByCreatorId(accountantId);

            // Ajouter les infos client pour chaque dossier
            for (Folder folder : folders) {
                Client client = clientService.getClientById(folder.getClientId());
                if (client != null) {
                    folder.setClient(client);
                }
            }

            return new ResponseEntity<>(folders, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

}


