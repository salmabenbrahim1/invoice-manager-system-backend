package com.example.backend.controller;

import com.example.backend.model.Invoice;
import com.example.backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/folders")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    // Get all invoices in a folder
    @GetMapping("/{folderId}/invoices")
    public ResponseEntity<List<Invoice>> getInvoicesByFolder(@PathVariable String folderId) {
        List<Invoice> invoices = invoiceService.getInvoicesByFolder(folderId);

        if (invoices.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(invoices);
    }

    // Upload an invoice file and save it
    @PostMapping("/{folderId}/invoices")
    public ResponseEntity<Invoice> uploadInvoiceFile(@PathVariable String folderId,
                                                     @RequestParam("file") MultipartFile file,
                                                     @RequestParam("invoiceName") String invoiceName,
                                                     @RequestParam("status") String status
    ) {
        try {
            // Save the file and get the URL
            String fileUrl = invoiceService.saveFile(file);

            // Create a new invoice
            Invoice invoice = new Invoice();
            invoice.setInvoiceName(invoiceName);
            invoice.setImg(fileUrl);// Set the file URL
            invoice.setStatus(status);
            invoice.setAddedAt(LocalDateTime.now());
            invoice.setFolderId(folderId);  // Assign to the correct folder

            // Save the invoice and return it
            Invoice savedInvoice = invoiceService.saveInvoice(invoice);
            System.out.println("File received: " + file.getOriginalFilename());
            System.out.println("Invoice Name: " + invoiceName);

            return ResponseEntity.ok(savedInvoice);

        } catch (IOException e) {
            // Handle any issues during file upload
            return ResponseEntity.status(500).build();
        }
    }

    // Delete an invoice from a folder
    @DeleteMapping("/{folderId}/invoices/{invoiceId}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable String invoiceId) {
        invoiceService.deleteInvoice(invoiceId);
        return ResponseEntity.noContent().build();
    }

    // Update an invoice
    @PutMapping("/{folderId}/invoices/{invoiceId}")
    public ResponseEntity<Invoice> updateInvoice(@PathVariable String folderId, @PathVariable String invoiceId,
                                                 @RequestBody Invoice updatedInvoice) {
        Invoice updated = invoiceService.updateInvoice(invoiceId, updatedInvoice);
        return ResponseEntity.ok(updated);
    }
}
