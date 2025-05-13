package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.ClientRepository;
import com.example.backend.repository.FolderRepository;
import com.example.backend.repository.InvoiceRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ClientRepository clientRepository;
    private final FolderRepository folderRepository;

    public DashboardService(UserRepository userRepository,
                            InvoiceRepository invoiceRepository,
                            ClientRepository clientRepository,
                            FolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.folderRepository = folderRepository;
    }

    // Cette méthode ne doit plus être statique
    public Map<String, Object> getUserStats(User currentUser) {
        Map<String, Object> stats = new HashMap<>();

        if (currentUser instanceof Admin) {
            List<User> nonAdminUsers = userRepository.findAll().stream()
                    .filter(u -> !(u instanceof Admin))
                    .collect(Collectors.toList());

            long activeUsers = nonAdminUsers.stream().filter(User::isActive).count();
            long inactiveUsers = nonAdminUsers.size() - activeUsers;

            stats.put("totalCompanies", nonAdminUsers.stream().filter(u -> u instanceof Company).count());
            stats.put("totalIndependentAccountants", nonAdminUsers.stream().filter(u -> u instanceof IndependentAccountant).count());
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", inactiveUsers);
            stats.put("totalInvoicesExtracted", Optional.ofNullable(invoiceRepository.count()).orElse(0L));
            stats.put("companyInvoices", Optional.ofNullable(invoiceRepository.countByUserRole("COMPANY")).orElse(0L));
            stats.put("accountantInvoices", Optional.ofNullable(invoiceRepository.countByUserRole("INDEPENDENT_ACCOUNTANT")).orElse(0L));

        } else if (currentUser instanceof IndependentAccountant) {
            IndependentAccountant accountant = (IndependentAccountant) currentUser;
            long totalClients = clientRepository.findByCreatedBy_Id(accountant.getId()).size();
            long totalFolders = folderRepository.findByCreatedById(accountant.getId()).size();

            List<Folder> folders = folderRepository.findByCreatedById(accountant.getId());

            long totalInvoices = folders.stream().mapToLong(Folder::getInvoiceCount).sum();

            Map<String, Long> invoicesByMonth = new LinkedHashMap<>();
            Map<String, Long> validatedByMonth = new LinkedHashMap<>();
            for (Month month : Month.values()) {
                String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                invoicesByMonth.put(monthName, 0L);
                validatedByMonth.put(monthName, 0L);
            }

            long pendingInvoices = 0;

            for (Folder folder : folders) {
                for (String invoiceId : folder.getInvoiceIds()) {
                    Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
                    if (invoice != null && invoice.getAddedAt() != null) {
                        String month = invoice.getAddedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                        invoicesByMonth.put(month, invoicesByMonth.get(month) + 1);

                        if ("validated".equalsIgnoreCase(invoice.getStatus())) {
                            validatedByMonth.put(month, validatedByMonth.get(month) + 1);
                        }
                        if ("pending".equalsIgnoreCase(invoice.getStatus())) {
                            pendingInvoices++;
                        }
                    }
                }
            }

            List<Map<String, Object>> invoiceData = invoicesByMonth.entrySet().stream().map(entry -> {
                Map<String, Object> point = new HashMap<>();
                point.put("name", entry.getKey());
                point.put("invoices", entry.getValue());
                return point;
            }).collect(Collectors.toList());

            List<Map<String, Object>> validatedData = validatedByMonth.entrySet().stream().map(entry -> {
                Map<String, Object> point = new HashMap<>();
                point.put("name", entry.getKey());
                point.put("validated", entry.getValue());
                return point;
            }).collect(Collectors.toList());

            stats.put("totalClients", totalClients);
            stats.put("totalFolders", totalFolders);
            stats.put("totalInvoices", totalInvoices);
            stats.put("pendingInvoices", pendingInvoices);
            stats.put("invoiceData", invoiceData);
            stats.put("validatedInvoices", validatedData);
        } else if (currentUser instanceof Company) {
            Company company = (Company) currentUser;

            long totalInternalAccountants = userRepository.countByRoleAndIdIn("INTERNAL_ACCOUNTANT", company.getAccountantIds());
            List<Client> clients = clientRepository.findByCreatedBy_Id(company.getId());
            long totalClients = clients.size();

            long uploadedInvoices = 0;
            for (String accountantId : company.getAccountantIds()) {
                // 3.a. Récupère tous les dossiers créés par ce comptable
                List<Folder> folders = folderRepository.findByCreatedById(accountantId);
                for (Folder folder : folders) {
                    // 3.b. Compte les factures pour chaque dossier
                    uploadedInvoices += invoiceRepository.countByFolderId(folder.getId());
                }
            }


                // Ajouter les statistiques à la carte
                stats.put("totalInternalAccountants", totalInternalAccountants);
                stats.put("totalClients", totalClients);
                stats.put("totalInvoices", uploadedInvoices);


            }


            return stats;
        }
    }

