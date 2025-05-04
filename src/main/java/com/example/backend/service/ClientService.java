package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

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

        // Assign to internal accountant (only for companies)
        if (creator instanceof Company && assignedAccountantId != null) {
            CompanyAccountant accountant = (CompanyAccountant) userRepository.findById(assignedAccountantId).orElseThrow();
            client.setAssignedTo(accountant);
        }

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
            CompanyAccountant accountant = (CompanyAccountant) userRepository.findById(assignedAccountantId)
                    .orElseThrow(() -> new IllegalArgumentException("Accountant not found"));
            client.setAssignedTo(accountant);
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

        clientRepository.delete(client);
    }

    // Retrieve client by ID
    public Client getClientById(String clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }

}
