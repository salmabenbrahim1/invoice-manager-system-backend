package com.example.backend.controller;
import com.example.backend.dto.AssignedClientDTO;
import com.example.backend.dto.ClientDTO;
import com.example.backend.model.*;
import com.example.backend.repository.AccountantAssignmentRepository;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ClientService;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final AccountantAssignmentRepository assignmentRepository;





    // Create a new client
    @PostMapping
    public ResponseEntity<?> createClient(@RequestBody ClientDTO request, Principal principal) {
        try {
            User creator = userService.getCurrentUser(principal);

            // Create client
            clientService.createClient(
                    creator,
                    request.getName(),
                    request.getEmail(),
                    request.getPhone(),
                    request.getAssignedAccountantId()
            );

            // Check if creator is a company
            if (creator instanceof Company) {
                return ResponseEntity.status(HttpStatus.CREATED).body("Client has been created by a company.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Client has been created.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error while creating client.");
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
    public ResponseEntity<List<AssignedClientDTO>> getClientsForInternalAccountant(Principal principal) {
        String email = principal.getName();
        CompanyAccountant accountant = (CompanyAccountant) userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Accountant not found"));

        List<AccountantAssignment> assignments = assignmentRepository.findByAccountantId(accountant.getId());

        List<AssignedClientDTO> clientsWithAssignmentDate = assignments.stream()
                .map(a -> new AssignedClientDTO(a.getClient(), a.getAssignedAt(), a.getCompanyName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(clientsWithAssignmentDate);
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
            @RequestBody ClientDTO request,
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
