package com.example.backend.service;
import com.example.backend.dto.ClientAssignmentDTO;
import com.example.backend.model.*;
import com.example.backend.repository.AccountantAssignmentRepository;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Autowired
    private AccountantAssignmentRepository assignmentRepository;

    public Client createClient(User creator, String name, String email, String phone, String assignedAccountantId) {
        if (!(creator instanceof IndependentAccountant || creator instanceof Company)) {
            throw new SecurityException("Unauthorized to create clients");
        }

        //client details:
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        Client client = new Client();
        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);
        client.setPassword(passwordEncoder.encode(generatedPassword));
        client.setCreatedBy(creator);

        Client savedClient = clientRepository.save(client);

        // Send email to the new client
        String subject = "Welcome to Invox - Your Client Account";
        String body = String.format("""
                <html><body>
                <p>Dear %s,</p>
                <p>Your client account has been created.</p>
                <p><b>Email:</b> %s<br><b>Temporary Password:</b> %s</p>
                <p>Please login and update your password: <a href="http://localhost:3000/login">Login</a></p>
                </body></html>
                """, name, email, generatedPassword);
        emailService.sendEmail(email, subject, body);

        return savedClient;
    }


    public Client assignAccountantToClient(User updater, String clientId, String accountantId) {
        if (!(updater instanceof Company)) {
            throw new SecurityException("Only companies can assign accountants to clients.");
        }

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (!client.getCreatedBy().getId().equals(updater.getId())) {
            throw new SecurityException("Unauthorized to assign an accountant to this client");
        }

        CompanyAccountant accountant = (CompanyAccountant) userRepository.findById(accountantId)
                .orElseThrow(() -> new IllegalArgumentException("Accountant not found"));

        // Save the assignment as a new document
        AccountantAssignment assignment = new AccountantAssignment();
        assignment.setAccountant(accountant);
        assignment.setClient(client);
        assignmentRepository.save(assignment);

        // Optionally update references in the user and client if still needed
        client.setAssignedTo(accountant); // optional legacy reference
        clientRepository.save(client);

        // Add client ID to accountant's list (optional legacy)
        accountant.getClientIds().add(client.getId());
        userRepository.save(accountant);

        return client;
    }



    public List<Client> getClientsCreatedBy(User user) {
        return clientRepository.findByCreatedBy_Id(user.getId());
    }


    public Client updateClient(User updater, String clientId, String name, String email, String phone, String assignedAccountantId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // Ensure only the creator can update the client
        if (!client.getCreatedBy().getId().equals(updater.getId())) {
            throw new SecurityException("Unauthorized to update this client");
        }

        client.setName(name);
        client.setEmail(email);
        client.setPhone(phone);

        // Reassign accountant (only for companies)
        if (updater instanceof Company && assignedAccountantId != null) {
            // Remove client from previous accountant if changed
            if (client.getAssignedTo() != null && !client.getAssignedTo().getId().equals(assignedAccountantId)) {
                CompanyAccountant oldAccountant = (CompanyAccountant) client.getAssignedTo();
                oldAccountant.removeClientId(client.getId());
                userRepository.save(oldAccountant);
            }

            CompanyAccountant newAccountant = (CompanyAccountant) userRepository.findById(assignedAccountantId)
                    .orElseThrow(() -> new IllegalArgumentException("Accountant not found"));
            client.setAssignedTo(newAccountant);
            newAccountant.addClientId(client.getId());

        }

        return clientRepository.save(client);
    }

    public void deleteClient(User deleter, String clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // Only the creator can delete the client
        if (!client.getCreatedBy().getId().equals(deleter.getId())) {
            throw new SecurityException("Unauthorized to delete this client");
        }

        if (client.getAssignedTo() != null) {
            CompanyAccountant accountant = client.getAssignedTo();
            accountant.removeClientId(client.getId());
            userRepository.save(accountant);
        }


        clientRepository.delete(client);
    }

    // Retrieve client by ID
    public Client getClientById(String clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }

    public ClientAssignmentDTO getClientAssignmentDetails(String clientId) {
        // Fetch the client
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // Fetch the accountant assignment related to this client
        AccountantAssignment assignment = assignmentRepository.findByClient_Id(clientId).stream()
                .findFirst() // Assuming one assignment per client (if this is a one-to-one assignment)
                .orElseThrow(() -> new IllegalArgumentException("No assignment found for this client"));

        // Create and return the DTO with client and assignment details
        return new ClientAssignmentDTO(client, assignment.getAssignedAt());
    }


}
