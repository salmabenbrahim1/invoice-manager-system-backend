package com.example.backend.service;

import com.example.backend.dto.InvoiceCreateDTO;
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
import java.time.LocalDateTime;
import java.util.List;


@Service
public class InvoiceService {

    @Autowired
    private  InvoiceRepository invoiceRepository;

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FolderService folderService;



    // Fetch all invoices for a given folder
    public List<Invoice> getInvoicesByFolder(String folderId) {
        if (folderId == null || folderId.isEmpty()) {
            throw new IllegalArgumentException("Folder ID cannot be null or empty");
        }

        // Check if folder exists
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with ID: " + folderId));

        // Fetch invoices associated with the folder
        return invoiceRepository.findByFolderId(folderId); // Ne pas jeter d’exception ici
    }


    // Save the uploaded file
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


    // 
    public Invoice saveInvoice(MultipartFile file, InvoiceCreateDTO dto) throws IOException {
        // 1. Save the image file
        String savedImagePath = saveFile(file);

        // 2. Create the Invoice entity from the DTO
        Invoice invoice = new Invoice();
        invoice.setInvoiceName(dto.getInvoiceName());
        invoice.setStatus(dto.getStatus());
        invoice.setFolderId(dto.getFolderId());
        invoice.setImg(savedImagePath);
        invoice.setAddedAt(LocalDateTime.now());

        // 3. Save the invoice
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 4. Add invoice ID to folder
        Folder folder = folderRepository.findById(dto.getFolderId())
                .orElseThrow(() -> new RuntimeException("Folder not found with ID: " + dto.getFolderId()));

        folder.getInvoiceIds().add(savedInvoice.getId());
        folderRepository.save(folder);
        updateInvoiceCount(dto.getFolderId());


        return savedInvoice;
    }

    //
    public Invoice updateExtractedData(String invoiceId, InvoiceCreateDTO extractedData) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        invoice.setInvoiceNumber(extractedData.getInvoiceNumber());
        invoice.setInvoiceDate(extractedData.getInvoiceDate());
        invoice.setDueDate(extractedData.getDueDate());
        invoice.setCurrency(extractedData.getCurrency());


        invoice.setSellerName(extractedData.getSellerName());
        invoice.setSellerAddress(extractedData.getSellerAddress());
        invoice.setSellerPhone(extractedData.getSellerPhone());
        invoice.setSellerSiretNumber(extractedData.getSellerSiretNumber());

        invoice.setCustomerName(extractedData.getCustomerName());
        invoice.setCustomerAddress(extractedData.getCustomerAddress());
        invoice.setCustomerPhone(extractedData.getCustomerPhone());

        invoice.setTvaNumber(extractedData.getTvaNumber());
        invoice.setTvaRate(extractedData.getTvaRate());
        invoice.setTva(extractedData.getTva());
        invoice.setHt(extractedData.getHt());
        invoice.setTtc(extractedData.getTtc());
        invoice.setDiscount(extractedData.getDiscount());

        invoice.setStatus("VALIDATED");


        return invoiceRepository.save(invoice);
    }

    // Delete invoice method
    public void deleteInvoice(String invoiceId) {
        if (invoiceId == null || invoiceId.isEmpty()) {
            throw new IllegalArgumentException("Invoice ID cannot be null or empty");
        }

        // Find and delete the invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        // Remove invoice from the folder's invoice list
        Folder folder = folderRepository.findById(invoice.getFolderId())
                .orElseThrow(() -> new RuntimeException("Folder not found with ID: " + invoice.getFolderId()));

        folder.getInvoiceIds().remove(invoice.getId());  // Remove the invoice ID from the folder's invoice list
        folderRepository.save(folder);  // Save updated folder


        // Delete the invoice
        invoiceRepository.delete(invoice);
        updateInvoiceCount(folder.getId());

    }

    // Update invoice method
    public Invoice updateInvoice(String invoiceId, InvoiceCreateDTO dto, MultipartFile file) throws IOException {
        if (invoiceId == null || invoiceId.isEmpty()) {
            throw new IllegalArgumentException("Invoice ID cannot be null or empty");
        }

        // Find the existing invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found with ID: " + invoiceId));

        // If a new file is uploaded, save the file and update the image path
        if (file != null && !file.isEmpty()) {
            String newImagePath = saveFile(file);  // Save new file and get the path
            invoice.setImg(newImagePath);  // Update the invoice's image path
        }

        // Update other fields
        invoice.setInvoiceName(dto.getInvoiceName());
        invoice.setStatus(dto.getStatus());
        invoice.setFolderId(dto.getFolderId());

        // Save and return the updated invoice
        return invoiceRepository.save(invoice);
    }


    //
    public void updateInvoiceCount(String folderId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found with ID: " + folderId));

        int count = invoiceRepository.findByFolderId(folderId).size();
        folder.setInvoiceCount(count);
        folderRepository.save(folder);
    }

}




