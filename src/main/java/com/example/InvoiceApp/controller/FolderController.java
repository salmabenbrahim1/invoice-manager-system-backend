package com.example.InvoiceApp.controller;

import com.example.InvoiceApp.model.Client;
import com.example.InvoiceApp.model.Folder;
import com.example.InvoiceApp.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/folders")
public class FolderController {
    @Autowired
    private  FolderService folderService;

    @PostMapping
    public ResponseEntity<Folder> createFolder(@RequestBody Map<String, Object> requestBody) {
     try{
         String folderName =(String)requestBody.get("name");
         String folderDescription = (String) requestBody.get("description");
         Map<String, Object> clientData = (Map<String, Object>) requestBody.get("clientId");

     // Validate folder data
        if (folderName == null || folderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be empty");
        }
         // Create Folder object
         Folder folder = new Folder();
         folder.setFolderName(folderName);
         folder.setDescription(folderDescription);

         //Create a new Client
         Client newClient = null;
         if (clientData != null && clientData.get("id") == null) {
             // Scenario 1: New client provided
             newClient = new Client();
             newClient.setName((String) clientData.get("name"));
             newClient.setEmail((String) clientData.get("email"));
             newClient.setPhoneNumber((String) clientData.get("phoneNumber"));
         } else if (clientData != null) {
             // Scenario 2: Existing client
             folder.setClientId((String) clientData.get("id"));
         }
         // Save the folder (and create a new client if needed)
         Folder savedFolder = folderService.addFolder(folder, newClient);
         return new ResponseEntity<>(savedFolder, HttpStatus.CREATED);
     } catch (Exception e) {
         return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
     }
    }





    @GetMapping("/{id}")
    public ResponseEntity<?> getFolderById(@PathVariable String id) {
        Folder folder = folderService.getFolderById(id);
        if (folder == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Folder not found with id: " + id));
        }
        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable String id) {
            folderService.deleteFolder(id);
            return ResponseEntity.noContent().build();

    }

    @GetMapping
    public ResponseEntity<List<Folder>> getAllFolders() {
        List<Folder> folders = folderService.getAllFolders();
        return ResponseEntity.ok(folders);
    }
}