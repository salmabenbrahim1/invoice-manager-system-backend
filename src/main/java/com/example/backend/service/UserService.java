package com.example.backend.service;

import com.example.backend.dto.UserCreateDTO;
import com.example.backend.model.*;
import com.example.backend.repository.InvoiceRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private final InvoiceRepository invoiceRepository;


    public User createUser(User currentUser, UserCreateDTO dto) {
        if (!canManage(currentUser)) {
            throw new SecurityException("Not authorized to create user with role: " + dto.getRole());
        }

        User newUser;
        String generatedPassword = generateBase64Password(); // for generating passwords

        switch (dto.getRole()) {
            case "COMPANY":
                if (!(currentUser instanceof Admin)) {
                    throw new SecurityException("Only Admin can create companies");
                }
                Company company = new Company();
                company.setCompanyName(dto.getCompanyName());
                company.setCreatedBy(currentUser);
                newUser = company;
                break;

            case "INDEPENDENT ACCOUNTANT":
                if (!(currentUser instanceof Admin)) {
                    throw new SecurityException("Only Admin can create independent accountants");
                }
                IndependentAccountant independent = new IndependentAccountant();
                independent.setFirstName(dto.getFirstName());
                independent.setLastName(dto.getLastName());
                independent.setGender(dto.getGender());
                independent.setCin(dto.getCin());
                independent.setEmail(dto.getEmail());
                independent.setPhone(dto.getPhone());
                independent.setCreatedBy(currentUser);
                newUser = independent;
                break;
            case "INTERNAL ACCOUNTANT":
                if (!(currentUser instanceof Company)) {
                    throw new SecurityException("Only Company can create internal accountants");
                }
                CompanyAccountant internal = new CompanyAccountant();
                internal.setFirstName(dto.getFirstName());
                internal.setLastName(dto.getLastName());
                internal.setEmail(dto.getEmail());
                internal.setPhone(dto.getPhone());
                internal.setGender(dto.getGender());
                internal.setCin(dto.getCin());
                internal.setCreatedBy(currentUser);
                newUser = internal;
                // Save the accountant
                User savedAccountant = userRepository.save(newUser);

                // Add the accountant ID to the company's list
                 company = (Company) currentUser;
                company.getAccountantIds().add(savedAccountant.getId());
                userRepository.save(company); // Update the company

                return savedAccountant;


            default:
                throw new IllegalArgumentException("Unsupported role: " + dto.getRole());
        }

        newUser.setEmail(dto.getEmail());
        newUser.setPhone(dto.getPhone());
        newUser.setPassword(passwordEncoder.encode(generatedPassword));

        User savedUser = userRepository.save(newUser);


        // Send email
        String subject = "Your Account has been created";
        String body;

        if (savedUser.getRole().equals("COMPANY")) {
            body = String.format("Hello %s,\n\nYour account has been created successfully. Your temporary password is: %s\n\nPlease change your password after logging in.", dto.getCompanyName(), generatedPassword);
        } else {
            body = String.format("Hello %s,\n\nYour account has been created successfully. Your temporary password is: %s\n\nPlease change your password after logging in.", dto.getFirstName(), generatedPassword);
        }

        emailService.sendEmail(dto.getEmail(), subject, body);

        return savedUser;

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
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setActive(updated.isActive()); // If applicable

        // Role-specific updates
        switch (existing) {
            case IndependentAccountant accountant -> {
                IndependentAccountant updatedAccountant = (IndependentAccountant) updated;
                accountant.setFirstName(updatedAccountant.getFirstName());
                accountant.setLastName(updatedAccountant.getLastName());
                accountant.setGender(updatedAccountant.getGender());
                accountant.setCin(updatedAccountant.getCin());
            }
            case CompanyAccountant accountant -> {
                CompanyAccountant updatedAccountant = (CompanyAccountant) updated;
                accountant.setFirstName(updatedAccountant.getFirstName());
                accountant.setLastName(updatedAccountant.getLastName());
                accountant.setGender(updatedAccountant.getGender());
                accountant.setCin(updatedAccountant.getCin());
            }
            case Company company -> {
                Company updatedCompany = (Company) updated;
                company.setCompanyName(updatedCompany.getCompanyName());
            }
            default -> throw new IllegalArgumentException("Unsupported user type: " + existing.getClass().getSimpleName());
        }


        return userRepository.save(existing);
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

    public void toggleUserActivation(String id, User currentUser) {
        User user = getUserById(id, currentUser);
        if (!canView(currentUser, user)) {
            throw new SecurityException("Not authorized to modify this user");
        }        if (!(currentUser instanceof Admin || currentUser instanceof Company)) {
            throw new SecurityException("Only Admin or Company can toggle activation");
        }

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("Fetched user: " + user);
        return user;
    }


    public Map<String, Long> getUserStats(User currentUser) {
        Map<String, Long> stats = new HashMap<>();
        if (currentUser instanceof Admin) {
            // Get all non-admin users
            List<User> nonAdminUsers = userRepository.findAll().stream()
                    .filter(u -> !(u instanceof Admin))
                    .collect(Collectors.toList());

            // User statistics (excluding admin)
            long activeUsers = nonAdminUsers.stream().filter(User::isActive).count();
            long inactiveUsers = nonAdminUsers.size() - activeUsers;

            stats.put("totalCompanies", nonAdminUsers.stream()
                    .filter(u -> u instanceof Company)
                    .count());

            stats.put("totalIndependentAccountants", nonAdminUsers.stream()
                    .filter(u -> u instanceof IndependentAccountant)
                    .count());

            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", inactiveUsers);

            // Invoice statistics (unchanged)
            Long totalInvoices = invoiceRepository.count();
            stats.put("totalInvoicesExtracted", totalInvoices != null ? totalInvoices : 0L);

            // Invoices count by user type (unchanged)
            Long companyInvoices = invoiceRepository.countByUserRole("COMPANY");
            Long accountantInvoices = invoiceRepository.countByUserRole("ACCOUNTANT");
            stats.put("companyInvoices", companyInvoices != null ? companyInvoices : 0L);
            stats.put("accountantInvoices", accountantInvoices != null ? accountantInvoices : 0L);
        }
        return stats;
    }
    private String generateBase64Password() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12]; // 12 bytes will produce 16-character Base64 string
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean canManage(User currentUser) {
        return currentUser instanceof Admin || currentUser instanceof Company;
    }


    private boolean canView(User currentUser, User targetUser) {
        if (currentUser instanceof Admin) return true;
        return targetUser.getCreatedBy() != null && targetUser.getCreatedBy().getId().equals(currentUser.getId());
    }

}
