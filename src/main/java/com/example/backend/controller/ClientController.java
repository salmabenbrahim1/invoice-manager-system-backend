package com.example.backend.controller;
import com.example.backend.dto.ClientCreateDTO;
import com.example.backend.model.Client;
import com.example.backend.model.CompanyAccountant;
import com.example.backend.model.User;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ClientService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

//
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;




    // Create a new client
    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody ClientCreateDTO request, Principal principal) {
        try {
            User creator = userService.getCurrentUser(principal);
            Client createdClient = clientService.createClient(creator, request.getName(), request.getEmail(), request.getPhone(), request.getAssignedAccountantId());
            return new ResponseEntity<>(createdClient, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Assign an internal accountant to an existing client
    @PutMapping("/{clientId}/assign-accountant")
    public ResponseEntity<Client> assignAccountant(
            @PathVariable String clientId,
            @RequestBody Map<String, String> request,
            Principal principal) {
        try {
            User updater = userService.getCurrentUser(principal);
            String accountantId = request.get("accountantId");
            Client updatedClient = clientService.assignAccountantToClient(updater, clientId, accountantId);
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/my-clients")
    @PreAuthorize("hasRole('INTERNAL_ACCOUNTANT')")
    public ResponseEntity<List<Client>> getClientsForInternalAccountant(Principal principal) {
        String email = principal.getName();

        // Fetch the logged-in accountant using email
        CompanyAccountant accountant = (CompanyAccountant) userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Accountant not found"));

        // Get the list of client IDs
        List<String> clientIds = accountant.getClientIds();

        // Fetch clients by their IDs
        List<Client> assignedClients = clientRepository.findAllById(clientIds);

        return ResponseEntity.ok(assignedClients);
    }



    // Get the list of clients created by the current user
    @GetMapping
    public ResponseEntity<List<Client>> getMyClients(Principal principal) {
        try {
            User user = userService.getCurrentUser(principal);
            List<Client> clients = clientService.getClientsCreatedBy(user);
            return new ResponseEntity<>(clients, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // Update a client
    @PutMapping("/{clientId}")
    public ResponseEntity<Client> updateClient(
            @PathVariable String clientId,
            @RequestBody ClientCreateDTO request,
            Principal principal) {
        try {
            User updater = userService.getCurrentUser(principal);
            Client updatedClient = clientService.updateClient(updater, clientId, request.getName(), request.getEmail(), request.getPhone(), request.getAssignedAccountantId());
            return new ResponseEntity<>(updatedClient, HttpStatus.OK);
        } catch (SecurityException e) {
            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    //  Delete a client
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(@PathVariable String clientId, Principal principal) {
        try {
            User deleter = userService.getCurrentUser(principal);
            clientService.deleteClient(deleter, clientId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (SecurityException e) {

            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
