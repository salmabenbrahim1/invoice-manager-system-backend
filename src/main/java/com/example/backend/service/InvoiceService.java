package com.example.backend.service;

import com.example.backend.model.Folder;
import com.example.backend.model.Invoice;
import com.example.backend.repository.FolderRepository;
import com.example.backend.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final FolderRepository folderRepository;
    @Autowired
    private FolderService folderService;

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository, FolderRepository folderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.folderRepository = folderRepository;
    }

    // Fetch all invoices for a given folder
    public List<Invoice> getInvoicesByFolder(String folderId) {
        if (folderId == null || folderId.isEmpty()) {
            throw new IllegalArgumentException("Folder ID cannot be null or empty");
        }
        return invoiceRepository.findByFolderId(folderId);
    }

    // saving the uploaded file
    public String saveFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads/invoices";
        File directory = new File(uploadDir);

        // Check if the directory exists, if not, try to create it
        if (!directory.exists()) {
            boolean dirsCreated = directory.mkdirs();  // Try to create the directory
            if (!dirsCreated) {
                throw new IOException("Failed to create directories: " + uploadDir);
            }
            System.out.println("Directories created: " + uploadDir);  // Log the successful directory creation
        }

        // Save the file
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), path);  // Save the file to the path
        return "/uploads/invoices/" + fileName;  // Return the relative path of the saved file
    }
    // Save the invoice and associate it with the folder
    public Invoice saveInvoice(Invoice invoice) {
        // Save the invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Fetch the folder and update its invoice list
        Folder folder = folderRepository.findById(invoice.getFolderId())
                .orElseThrow(() -> new RuntimeException("Folder not found with ID: " + invoice.getFolderId()));

        folder.getInvoiceIds().add(savedInvoice.get_id());
        folderRepository.save(folder);

        return savedInvoice;
    }

    // Delete an invoice
    public void deleteInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.isEmpty()) {
            throw new IllegalArgumentException("Invoice ID cannot be null or empty");
        }

        // Fetch the invoice to get the folder ID
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        String folderId = invoice.getFolderId();

        if (folderId != null) {
            // Find the folder containing this invoice by invoiceId
            Folder folder = folderRepository.findById(folderId).orElse(null);

            if (folder != null) {
                // Remove the invoiceId from the folder's invoiceIds list
                folderService.removeInvoiceFromFolder(invoiceId, folderId);

            }
        }
        // Delete the invoice itself
        invoiceRepository.deleteById(invoiceId);
    }

    // Update an invoice
    public Invoice updateInvoice(String id, Invoice updatedInvoice) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + id));

        if (updatedInvoice.getInvoiceName() != null) {
            existingInvoice.setInvoiceName(updatedInvoice.getInvoiceName());
        }
        if (updatedInvoice.getImg() != null) {
            existingInvoice.setImg(updatedInvoice.getImg());
        }
        if (updatedInvoice.getStatus() != null) {
            existingInvoice.setStatus(updatedInvoice.getStatus());
        }

        return invoiceRepository.save(existingInvoice);
    }


}
