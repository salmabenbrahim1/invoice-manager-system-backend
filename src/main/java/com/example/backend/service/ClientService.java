package com.example.backend.service;
import com.example.backend.model.*;
import com.example.backend.repository.AccountantAssignmentRepository;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;


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
    private final NotificationService notificationService;


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

        //  Delete all existing assignments for this client
        assignmentRepository.deleteByClientId(new ObjectId(clientId));

        // Create new assignment
        AccountantAssignment assignment = new AccountantAssignment();
        assignment.setAccountant(accountant);
        assignment.setClient(client);
        assignment.setCompanyName(((Company) updater).getCompanyName());
        assignmentRepository.save(assignment);
        String message = "The accounting firm " + assignment.getCompanyName()
                + " has assigned you a new client: " + client.getName() + " (" + client.getEmail() + ")";

        notificationService.createNotification(accountant.getId(), client.getId(), message);

        // Legacy updates
        client.setAssignedTo(accountant);
        clientRepository.save(client);

        accountant.getClientIds().add(client.getId());
        userRepository.save(accountant);

        return client;
    }



    public List<Client> getClientsCreatedBy(User user) {
        return clientRepository.findByCreatedById(user.getId());
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



}