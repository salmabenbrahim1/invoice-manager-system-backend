package com.example.backend.service;

import com.example.backend.model.*;
import com.example.backend.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final AccountantAssignmentRepository accountantAssignmentRepository;


    public DashboardService(UserRepository userRepository,
                            InvoiceRepository invoiceRepository,
                            ClientRepository clientRepository,
                            FolderRepository folderRepository,AccountantAssignmentRepository accountantAssignmentRepository) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.clientRepository = clientRepository;
        this.folderRepository = folderRepository;
        this.accountantAssignmentRepository = accountantAssignmentRepository;

    }

    public Map<String, Object> getUserStats(User currentUser) {
        Map<String, Object> stats = new HashMap<>();

        if (currentUser instanceof Admin) {
            List<User> nonAdminUsers = userRepository.findAll().stream()
                    .filter(u -> !(u instanceof Admin))
                    .collect(Collectors.toList());

            // Map folders to users by email
            Map<String, List<Folder>> foldersByUser = new HashMap<>();



            for (User user : nonAdminUsers) {
                List<Folder> userFolders = folderRepository.findByCreatedById(user.getId());
                foldersByUser.put(user.getEmail(), userFolders);
            }
            // Sort users by number of folders (descending) and limit to top 10
            List<Map.Entry<String, List<Folder>>> sortedEntries = foldersByUser.entrySet()
                    .stream()
                    .sorted((entry1, entry2) -> Integer.compare(entry2.getValue().size(), entry1.getValue().size()))
                    .collect(Collectors.toList());

            // Build the top 10 folder stats
            sortedEntries = sortedEntries.stream().limit(10).collect(Collectors.toList());


            Map<String, List<Folder>> top10FoldersByUser = sortedEntries.stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            stats.put("foldersByUser", top10FoldersByUser);



            long activeUsers = nonAdminUsers.stream().filter(User::isActive).count();
            long inactiveUsers = nonAdminUsers.size() - activeUsers;



            stats.put("totalCompanies", nonAdminUsers.stream().filter(u -> u instanceof Company).count());
            stats.put("totalIndependentAccountants", nonAdminUsers.stream().filter(u -> u instanceof IndependentAccountant).count());
            stats.put("activeUsers", activeUsers);
            stats.put("inactiveUsers", inactiveUsers);
            stats.put("totalInvoicesExtracted", Optional.ofNullable(invoiceRepository.count()).orElse(0L));

            List<User> independentAccountants = userRepository.findByRole("INDEPENDENT_ACCOUNTANT");
            List<User> companies = userRepository.findByRole("COMPANY");

            Map<String, Long> independentAccountantInvoices = new HashMap<>();
            for (User accountant : independentAccountants) {
                List<Folder> folders = folderRepository.findByCreatedById(accountant.getId());
                long invoiceCount = folders.stream()
                        .mapToLong(folder -> folder.getInvoiceIds() != null ? folder.getInvoiceIds().size() : 0)
                        .sum();
                independentAccountantInvoices.put(accountant.getEmail(), invoiceCount);
            }

            Map<String, Long> companyInvoices = new HashMap<>();
            for (User company : companies) {
                List<Client> clients = clientRepository.findByCreatedById(company.getId());
                List<String> clientIds = clients.stream().map(Client::getId).collect(Collectors.toList());
                List<Folder> folders = folderRepository.findByClientIdIn(clientIds);
                long invoiceCount = folders.stream()
                        .mapToLong(folder -> folder.getInvoiceIds() != null ? folder.getInvoiceIds().size() : 0)
                        .sum();
                companyInvoices.put(company.getEmail(), invoiceCount);
            }


            // Top 3 independent accountants (with > 1 invoice)
            List<Map<String, Object>> top3IndependentAccountants = independentAccountantInvoices.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(entry -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("name", entry.getKey());
                        point.put("invoiceCount", entry.getValue());
                        return point;
                    })
                    .collect(Collectors.toList());





            // Top 3 companies (with > 1 invoice)
             List<Map<String, Object>> top3Companies = companyInvoices.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(entry -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("name", entry.getKey());
                        point.put("invoiceCount", entry.getValue());
                        return point;
                    })
                    .collect(Collectors.toList());

            stats.put("topIndependentAccountants", top3IndependentAccountants);
            stats.put("topCompanies", top3Companies);





        }
        else if (currentUser instanceof IndependentAccountant) {
            IndependentAccountant accountant = (IndependentAccountant) currentUser;
            long totalClients = clientRepository.findByCreatedById(accountant.getId()).size();
            long totalFolders = folderRepository.findByCreatedById(accountant.getId()).size();

            List<Folder> folders = folderRepository.findByCreatedById(accountant.getId());

            long totalInvoices = folders.stream().mapToLong(Folder::getInvoiceCount).sum();

            long failedInvoices = 0;

            // Initialize month maps
            Map<String, Long> invoicesByMonth = new LinkedHashMap<>();
            Map<String, Long> validatedByMonth = new LinkedHashMap<>();
            for (Month month : Month.values()) {
                String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                invoicesByMonth.put(monthName, 0L);
                validatedByMonth.put(monthName, 0L);
            }

            long pendingInvoices = 0;

            // Count invoices and statuses per month
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
                        if ("failed".equalsIgnoreCase(invoice.getStatus())) {
                            failedInvoices++;
                        }
                    }
                }
            }

            // Calculate total validated invoices count after populating the map
            long validatedInvoicesCount = validatedByMonth.values().stream().mapToLong(Long::longValue).sum();

            // Prepare chart data
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

            long archivedFolders = folders.stream()
                    .filter(Folder::isArchived)
                    .count();

            stats.put("archivedFiles", archivedFolders);
            LocalDateTime now = LocalDateTime.now();

            LocalDateTime startOfYear = now.withDayOfYear(1).with(LocalTime.MIN);
            LocalDateTime startOfMonth = now.withDayOfMonth(1).with(LocalTime.MIN);

            long archivedThisYear = folders.stream()
                    .filter(Folder::isArchived)
                    .filter(folder -> folder.getCreatedAt().isAfter(startOfYear))
                    .count();
            stats.put("archivedThisYear", archivedThisYear);

            long archivedThisMonth = folders.stream()
                    .filter(Folder::isArchived)
                    .filter(folder -> folder.getCreatedAt().isAfter(startOfMonth))
                    .count();
            stats.put("recentArchives", archivedThisMonth);

            long favoriteFolders = folders.stream()
                    .filter(Folder::isFavorite)
                    .filter(folder -> !folder.isArchived())
                    .count();
            stats.put("favoriteFolders", favoriteFolders);

            stats.put("totalClients", totalClients);
            stats.put("totalFolders", totalFolders);
            stats.put("totalInvoices", totalInvoices);
            stats.put("pendingInvoices", pendingInvoices);
            stats.put("invoiceData", invoiceData);
            stats.put("validatedInvoices", validatedData);
            stats.put("failedInvoices", failedInvoices);
            stats.put("validatedInvoicesCount", validatedInvoicesCount);
        }
        else if (currentUser instanceof Company) {
            Company company = (Company) currentUser;


             List<User> internalAccountants = userRepository.findByRoleAndIdIn("INTERNAL_ACCOUNTANT", company.getAccountantIds());

            long totalInternalAccountants = internalAccountants.size();
            long activeInternalAccountants = internalAccountants.stream().filter(User::isActive).count();
            long inactiveInternalAccountants = totalInternalAccountants - activeInternalAccountants;
            List<Client> clients = clientRepository.findByCreatedById(company.getId());
            long totalClients = clients.size();

            List<String> clientIds = clients.stream().map(Client::getId).collect(Collectors.toList());
            List<Folder> folders = folderRepository.findByClientIdIn(clientIds);

            // Initialize monthly counters
            Map<String, Long> invoicesByMonth = new LinkedHashMap<>();
            Map<String, Long> validatedByMonth = new LinkedHashMap<>();
            Map<String, Long> pendingInvoicesByMonth = new LinkedHashMap<>();
            for (Month month : Month.values()) {
                String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                invoicesByMonth.put(monthName, 0L);
                pendingInvoicesByMonth.put(monthName, 0L);
                validatedByMonth.put(monthName, 0L);


            }

            long uploadedInvoices = folders.stream()
                    .mapToLong(folder -> folder.getInvoiceIds() != null ? folder.getInvoiceIds().size() : 0)
                    .sum();

            long pendingInvoices = 0;
            long failedInvoices = 0;

            for (Folder folder : folders) {
                if (folder.getInvoiceIds() != null) {
                    for (String invoiceId : folder.getInvoiceIds()) {
                        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
                        if (invoice != null && invoice.getAddedAt() != null) {
                            Month monthEnum = invoice.getAddedAt().getMonth();  // e.g., Month.JANUARY
                            String monthName = monthEnum.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);  // e.g., "Jan"

                            invoicesByMonth.merge(monthName, 1L, Long::sum);

                            String status = invoice.getStatus();
                            if ("validated".equalsIgnoreCase(status)) {
                                validatedByMonth.merge(monthName, 1L, Long::sum);
                            } else if ("pending".equalsIgnoreCase(status)) {
                                pendingInvoices++;
                                pendingInvoicesByMonth.merge(monthName, 1L, Long::sum);  // This now works!
                            } else if ("failed".equalsIgnoreCase(status)) {
                                failedInvoices++;
                            }
                        }
                    }
                }
            }

            long validatedInvoicesCount = validatedByMonth.values().stream().mapToLong(Long::longValue).sum();



            // Prepare chart data
            List<Map<String, Object>> invoiceData = invoicesByMonth.entrySet().stream().map(entry -> {
                Map<String, Object> point = new HashMap<>();
                point.put("name", entry.getKey());
                point.put("invoices", entry.getValue());
                return point;
            }).collect(Collectors.toList());
            List<Map<String, Object>> pendingData = pendingInvoicesByMonth.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> point = new HashMap<>();
                        point.put("date", entry.getKey());  // e.g., "Jan"
                        point.put("pending", entry.getValue());
                        return point;
                    }).collect(Collectors.toList());


            List<AccountantAssignment> assignments = accountantAssignmentRepository.findAll().stream()
                    .filter(a -> a.getClient() != null && clientIds.contains(a.getClient().getId()))
                    .collect(Collectors.toList());

            Set<String> assignedClientIds = assignments.stream()
                    .map(a -> a.getClient().getId())
                    .collect(Collectors.toSet());

            long assignedClients = assignedClientIds.size();

            long unassignedClients = totalClients - assignedClients;


            stats.put("totalInternalAccountants", totalInternalAccountants);
            stats.put("totalClients", totalClients);
            stats.put("uploadedInvoices", uploadedInvoices);
            stats.put("pendingInvoices", pendingInvoices);
            stats.put("pendingInvoicesByMonth", pendingData);
            stats.put("validatedInvoicesCount", validatedInvoicesCount);
            stats.put("failedInvoices", failedInvoices);
            stats.put("assignedClients", assignedClients);
            stats.put("monthlyInvoices", invoiceData);
            stats.put("unassignedClients", unassignedClients);
            stats.put("activeInternalAccountants", activeInternalAccountants);
            stats.put("disabledInternalAccountants", inactiveInternalAccountants);

        }

        else if (currentUser instanceof CompanyAccountant) {
            User internalAccountant = currentUser;

            List<AccountantAssignment> assignments = accountantAssignmentRepository.findByAccountantId(internalAccountant.getId());
            Set<String> clientIds = assignments.stream()
                    .map(a -> a.getClient().getId())
                    .collect(Collectors.toSet());

            List<Folder> folders = folderRepository.findByClientIdIn(new ArrayList<>(clientIds));

            long totalClients = clientIds.size();
            long totalFolders = folders.size();
            long totalInvoices = folders.stream()
                    .mapToLong(folder -> folder.getInvoiceIds() != null ? folder.getInvoiceIds().size() : 0)
                    .sum();

            Map<String, Long> invoicesByMonth = new LinkedHashMap<>();
            Map<String, Long> validatedByMonth = new LinkedHashMap<>();
            long pendingInvoices = 0;
            long failedInvoices = 0;

            for (Month month : Month.values()) {
                String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                validatedByMonth.put(monthName, 0L);
            }

            for (Folder folder : folders) {
                if (folder.getInvoiceIds() != null) {
                    for (String invoiceId : folder.getInvoiceIds()) {
                        Invoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
                        if (invoice != null && invoice.getAddedAt() != null) {
                            String month = invoice.getAddedAt().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
                            invoicesByMonth.merge(month, 1L, Long::sum);

                            String status = invoice.getStatus();
                            if ("validated".equalsIgnoreCase(status)) {
                                validatedByMonth.merge(month, 1L, Long::sum);
                            } else if ("pending".equalsIgnoreCase(status)) {
                                pendingInvoices++;

                            } else if ("failed".equalsIgnoreCase(status)) {
                                failedInvoices++;
                            }
                        }
                    }
                }
            }

            long validatedInvoicesCount = validatedByMonth.values().stream().mapToLong(Long::longValue).sum();

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

            long archivedFolders = folders.stream().filter(Folder::isArchived).count();
            LocalDateTime now = LocalDateTime.now();
            long archivedThisYear = folders.stream()
                    .filter(Folder::isArchived)
                    .filter(folder -> folder.getCreatedAt().isAfter(now.withDayOfYear(1).with(LocalTime.MIN)))
                    .count();

            long archivedThisMonth = folders.stream()
                    .filter(Folder::isArchived)
                    .filter(folder -> folder.getCreatedAt().isAfter(now.withDayOfMonth(1).with(LocalTime.MIN)))
                    .count();

            long favoriteFolders = folders.stream()
                    .filter(Folder::isFavorite)
                    .filter(folder -> !folder.isArchived())
                    .count();

            stats.put("totalClients", totalClients);
            stats.put("totalFolders", totalFolders);
            stats.put("totalInvoices", totalInvoices);
            stats.put("pendingInvoices", pendingInvoices);
            stats.put("failedInvoices", failedInvoices);
            stats.put("validatedInvoicesCount", validatedInvoicesCount);
            stats.put("invoiceData", invoiceData);
            stats.put("validatedInvoices", validatedData);
            stats.put("archivedFiles", archivedFolders);
            stats.put("archivedThisYear", archivedThisYear);
            stats.put("recentArchives", archivedThisMonth);
            stats.put("favoriteFolders", favoriteFolders);
        }



        return stats;
        }


    }

