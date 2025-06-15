package com.example.backend.controller;

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
            User creator = userService.getCurrentUser(principal);

            Client client;
            if (folderDto.getClientId() != null && !folderDto.getClientId().isEmpty()) {
                client = clientService.getClientById(folderDto.getClientId());
                if (client == null) {
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
            } else {
                client = clientService.createClient(creator, folderDto.getClientName(),
                        folderDto.getClientEmail(), folderDto.getClientPhone(),
                        folderDto.getAssignedAccountantId());
            }

            Role creatorRole = null;
            if (creator instanceof Company) {
                creatorRole = Role.COMPANY;
            } else if (creator instanceof IndependentAccountant) {
                creatorRole = Role.INDEPENDENT_ACCOUNTANT;
            } else if (creator instanceof Admin) {
                creatorRole = Role.ADMIN;
            }

            Folder folder = new Folder(
                    folderDto.getFolderName(),
                    folderDto.getDescription(),
                    client.getId(),
                    creator.getId(),
                    creatorRole
            );

            Folder createdFolder = folderService.createFolder(folder);


            return ResponseEntity.status(HttpStatus.CREATED).body(createdFolder);

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
            User currentUser = userService.getCurrentUser(principal);

            // Allow admin or the company that created the accountant
            boolean isAdmin = currentUser instanceof Admin;
            boolean isCompany = currentUser instanceof Company;

            if (!isAdmin && !isCompany) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            User accountant = userService.getUserById(accountantId, currentUser);

            if (!(accountant instanceof CompanyAccountant)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // If it's a company, it can only see the accountants it created
            if (isCompany && !accountant.getCreatedBy().getId().equals(currentUser.getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            List<Folder> folders = folderService.getFoldersByCreatorId(accountantId);

            for (Folder folder : folders) {
                String clientId = folder.getClientId();
                if (clientId != null) {
                    try {
                        Client client = clientService.getClientById(clientId);
                        if (client != null) {
                            folder.setClient(client);
                        }
                    } catch (Exception ex) {
                        System.out.println("Failed to retrieve client ID:" + clientId);
                    }
                }
            }

            return new ResponseEntity<>(folders, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/by-independent-accountant/{accountantId}")
    public ResponseEntity<List<Folder>> getFoldersByIndependentAccountant(
            @PathVariable String accountantId, Principal principal) {

        try {
            User currentUser = userService.getCurrentUser(principal);

            if (!(currentUser instanceof Admin)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            User accountant = userService.getUserById(accountantId, currentUser);
            if (!(accountant instanceof IndependentAccountant)) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            List<Folder> folders = folderService.getFoldersByCreatorId(accountantId);

            for (Folder folder : folders) {
                String clientId = folder.getClientId();
                if (clientId != null) {
                    try {
                        Client client = clientService.getClientById(clientId);
                        if (client != null) {
                            folder.setClient(client);
                        }
                    } catch (Exception ex) {
                        System.out.println(" Failed to retrieve client ID:" + clientId);
                    }
                }
            }

            return new ResponseEntity<>(folders, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @PutMapping("/{folderId}/archive")
    public ResponseEntity<String> archiveFolder(@PathVariable String folderId) {
        folderService.archiveFolder(folderId);
        return ResponseEntity.ok("Folder archived successfully");
    }

    @PutMapping("/{folderId}/unarchive")
    public ResponseEntity<String> unarchiveFolder(@PathVariable String folderId) {
        folderService.unarchiveFolder(folderId);
        return ResponseEntity.ok("Folder unarchived successfully");
    }


    @PutMapping("/{id}/favorite")
    public ResponseEntity<Void> markAsFavorite(@PathVariable("id") String folderId) {
        folderService.markAsFavorite(folderId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/unfavorite")
    public ResponseEntity<Void> unmarkAsFavorite(@PathVariable("id") String folderId) {
        folderService.unmarkAsFavorite(folderId);
        return ResponseEntity.ok().build();
    }



}


