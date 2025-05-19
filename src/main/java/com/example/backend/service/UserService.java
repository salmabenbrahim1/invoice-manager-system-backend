package com.example.backend.service;

import com.example.backend.dto.UserDTO;
import com.example.backend.model.*;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.FolderRepository;
import com.example.backend.repository.InvoiceRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.security.SecureRandom;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
//

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ClientRepository clientRepository;
    private final FolderRepository folderRepository;


    private final InvoiceRepository invoiceRepository;


    private <T extends User> T populateCommonFields(T user, UserDTO dto, User creator) {
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setCreatedBy(creator);
        user.setActive(true);  // Ensure the user is active by default
        return user;
    }

    private void checkAdminPrivileges(User user) {
        if (!(user instanceof Admin)) {
            throw new SecurityException("Only Admin can perform this action");
        }
    }

    private void checkCompanyPrivileges(User user) {
        if (!(user instanceof Company)) {
            throw new SecurityException("Only Company can perform this action");
        }
    }

    public Map<String, Object> createUser(User currentUser, UserDTO dto) {
        if (currentUser == null) {
            throw new IllegalArgumentException("Current user cannot be null");
        }

        User newUser;
        String generatedPassword = generateBase64Password(); // for generating passwords

        switch (dto.getRole()) {
            case "COMPANY":
                checkAdminPrivileges(currentUser);
                newUser = populateCommonFields(new Company(), dto, currentUser);
                Company company = (Company) newUser;
                company.setCompanyName(dto.getCompanyName());
                break;

            case "INDEPENDENT_ACCOUNTANT":
                checkAdminPrivileges(currentUser);
                newUser = populateCommonFields(new IndependentAccountant(), dto, currentUser);
                IndependentAccountant independent = (IndependentAccountant) newUser;
                independent.setFirstName(dto.getFirstName());
                independent.setLastName(dto.getLastName());
                independent.setGender(dto.getGender());
                independent.setCin(dto.getCin());
                break;

            case "INTERNAL_ACCOUNTANT":
                checkCompanyPrivileges(currentUser);
                newUser = populateCommonFields(new CompanyAccountant(), dto, currentUser);
                CompanyAccountant internal = (CompanyAccountant) newUser;
                internal.setFirstName(dto.getFirstName());
                internal.setLastName(dto.getLastName());
                internal.setGender(dto.getGender());
                internal.setCin(dto.getCin());
                break;

            default:
                throw new IllegalArgumentException("Unsupported role: " + dto.getRole());
        }

        // Set the generated password
        newUser.setPassword(passwordEncoder.encode(generatedPassword));

        // Check the generated password being sent to the user

        // Save the created user
        User savedUser = userRepository.save(newUser);

        System.out.println("Generated password: " + generatedPassword);


        // Add the accountant ID to the company's list if applicable
        if (savedUser instanceof CompanyAccountant && currentUser instanceof Company company) {
            company.getAccountantIds().add(savedUser.getId());
            userRepository.save(company);
        }

        // Sending email to the user
        String subject = "Your Account Has Been Created Successfully";

        // Creating an email body
        String body;
        if ("COMPANY".equals(savedUser.getRole())) {
            body = String.format(
                    "<html>" +
                            "<body>" +
                            "<p>Dear %s,</p>" +
                            "<p>We are pleased to inform you that your account has been successfully created on Invox.</p>" +
                            "<p>Below are your account credentials:</p>" +
                            "<p><b>Username</b>: %s</p>" +
                            "<p><b>Temporary Password</b>: %s</p>" +
                            "<p>Please log in to the system and change your password as soon as possible for security reasons.</p>" +
                            "To access your account, please visit the following link: http://localhost:3000/login <br><br>" +

                            "<p>If you have any questions or need assistance, feel free to contact our support team.</p>" +
                            "<p>Best regards,<br>Invox Team</p>" +
                            "</body>" +
                            "</html>",
                    dto.getCompanyName(), dto.getEmail(), generatedPassword);
        } else {
            body = String.format(
                    "<html>" +
                            "<body>" +
                            "<p>Dear %s,</p>" +
                            "<p>We are pleased to inform you that your account has been successfully created on Invox.</p>" +
                            "<p>Below are your account credentials:</p>" +
                            "<p><b>Username</b>: %s</p>" +
                            "<p><b>Temporary Password</b>: %s</p>" +
                            "<p>Please log in to the system and change your password as soon as possible for security reasons.</p>" +
                            "To access your account, please visit the following link: http://localhost:3000/login <br><br>" +

                            "<p>If you have any questions or need assistance, feel free to contact our support team.</p>" +
                            "<p>Best regards,<br>Invox Team</p>" +
                            "</body>" +
                            "</html>",
                    dto.getFirstName(), dto.getEmail(), generatedPassword);
        }

        // Send the email
        boolean emailSent = emailService.sendEmail(dto.getEmail(), subject, body);



        Map<String, Object> response = new HashMap<>();
        response.put("user", savedUser);
        response.put("emailSent", emailSent);
        response.put("subject",subject);
        response.put("body",body);
        return response;


    }

    public List<?> getAllUsers(User currentUser) {
        if (currentUser instanceof Admin) {
            return userRepository.findByCreatedBy_Id(currentUser.getId());
        }
        if (currentUser instanceof Company) {
            return userRepository.findByCreatedBy_Id(currentUser.getId());
        }
        return List.of(currentUser);
    }

    public User getUserById(String id, User currentUser) {
        User user = userRepository.findById(id).orElseThrow();
        if (!canView(currentUser, user)) {
            throw new SecurityException("Not authorized to view this user");
        }
        return user;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    public User updateUser(String id, User updated, User currentUser) {
        // Input validation
        if (updated == null) {
            throw new IllegalArgumentException("Updated user cannot be null");
        }

        User existing = getUserById(id, currentUser);

        // Authorization checks
        if (!canManage(currentUser)) {
            throw new SecurityException("Not authorized to update users");
        }
        if (!canView(currentUser, existing)) {
            throw new SecurityException("Not authorized to update this user");
        }

        // Immutable properties check
        if (!existing.getId().equals(updated.getId())) {
            throw new IllegalArgumentException("Cannot change user ID");
        }
        if (!existing.getClass().equals(updated.getClass())) {
            throw new IllegalArgumentException("Cannot change user role type");
        }

        // Update common fields
        updateCommonFields(existing, updated);

        // Role-specific updates
        updateRoleSpecificFields(existing, updated);

        return userRepository.save(existing);
    }

    private void updateCommonFields(User existing, User updated) {
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setActive(updated.isActive());
    }

    private void updateRoleSpecificFields(User existing, User updated) {
        switch (existing.getClass().getSimpleName()) {
            case "IndependentAccountant":
                IndependentAccountant accountant = (IndependentAccountant) existing;
                IndependentAccountant updatedAccountant = (IndependentAccountant) updated;
                accountant.setFirstName(updatedAccountant.getFirstName());
                accountant.setLastName(updatedAccountant.getLastName());
                accountant.setGender(updatedAccountant.getGender());
                accountant.setCin(updatedAccountant.getCin());
                break;

            case "CompanyAccountant":
                CompanyAccountant companyAccountant = (CompanyAccountant) existing;
                CompanyAccountant updatedCompanyAccountant = (CompanyAccountant) updated;
                companyAccountant.setFirstName(updatedCompanyAccountant.getFirstName());
                companyAccountant.setLastName(updatedCompanyAccountant.getLastName());
                companyAccountant.setGender(updatedCompanyAccountant.getGender());
                companyAccountant.setCin(updatedCompanyAccountant.getCin());
                break;

            case "Company":
                Company company = (Company) existing;
                Company updatedCompany = (Company) updated;
                company.setCompanyName(updatedCompany.getCompanyName());
                break;

            default:
                throw new IllegalArgumentException("Unsupported user type: " + existing.getClass().getSimpleName());
        }
    }

    public void deleteUser(String id, User currentUser) {
        User existing = getUserById(id, currentUser);
        if (!canManage(currentUser)) {
            throw new SecurityException("Not authorized to delete this user");
        }
        if (!canView(currentUser, existing)) {
            throw new SecurityException("Not authorized to update/delete this user");
        }
        userRepository.deleteById(id);
    }

    public User toggleUserActivation(String id, User currentUser) {
        User user = getUserById(id, currentUser);
        if (!canView(currentUser, user)) {
            throw new SecurityException("Not authorized to modify this user");
        }
        if (!(currentUser instanceof Admin || currentUser instanceof Company)) {
            throw new SecurityException("Only Admin or Company can toggle activation");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);

        return user;  // Return updated user
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Fetched user: " + user);
        return user;
    }



    public String generateBase64Password() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12]; // 12 bytes will produce 16-character Base64 string
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean canManage(User currentUser) {
        return currentUser instanceof Admin || currentUser instanceof Company;
    }


    private boolean canView(User currentUser, User targetUser) {
        if (currentUser instanceof Admin || currentUser instanceof Company ) return true;
        return targetUser.getCreatedBy() != null && targetUser.getCreatedBy().getId().equals(currentUser.getId());
    }


    public User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new SecurityException("No user is currently authenticated");
        }

        String email = principal.getName();  // Assuming the principal contains the email as the username
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    public List<CompanyAccountant> getCompanyAccountants(User currentUser) {
        if (!(currentUser instanceof Company)) {
            throw new SecurityException("Only a Company can access its internal accountants.");
        }

        return userRepository.findByCreatedBy_Id(currentUser.getId()).stream()
                .filter(user -> user instanceof CompanyAccountant)
                .map(user -> (CompanyAccountant) user)
                .collect(Collectors.toList());
    }

    public User updateUserProfile(User currentUser, User updatedProfile) {
        if (updatedProfile == null) {
            throw new IllegalArgumentException("Updated profile cannot be null");
        }

        currentUser.setPhone(updatedProfile.getPhone());

        if (currentUser instanceof Company company && updatedProfile instanceof Company updatedCompany) {
            company.setCompanyName(updatedCompany.getCompanyName());
        } else if (currentUser instanceof IndependentAccountant independent && updatedProfile instanceof IndependentAccountant updatedIndependent) {
            independent.setFirstName(updatedIndependent.getFirstName());
            independent.setLastName(updatedIndependent.getLastName());
            independent.setCin(updatedIndependent.getCin());
            independent.setGender(updatedIndependent.getGender());
        } else if (currentUser instanceof CompanyAccountant internal && updatedProfile instanceof CompanyAccountant updatedInternal) {
            internal.setFirstName(updatedInternal.getFirstName());
            internal.setLastName(updatedInternal.getLastName());
            internal.setCin(updatedInternal.getCin());
            internal.setGender(updatedInternal.getGender());
        }

        if (updatedProfile.getPassword() != null && !updatedProfile.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(updatedProfile.getPassword()));
        }

        return userRepository.save(currentUser);
    }


}
