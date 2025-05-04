package com.example.backend.controller;

import com.example.backend.dto.InvoiceCreateDTO;
import com.example.backend.model.Invoice;
import com.example.backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    // Save invoice with image upload
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(
            @RequestParam("invoiceName") String invoiceName,
            @RequestParam("status") String status,
            @RequestParam("folderId") String folderId,
            @RequestParam("file") MultipartFile file) throws IOException {

        InvoiceCreateDTO dto = new InvoiceCreateDTO();
        dto.setInvoiceName(invoiceName);
        dto.setStatus(status);
        dto.setFolderId(folderId);
        dto.setFile(file);

        Invoice savedInvoice = invoiceService.saveInvoice(file, dto);
        return ResponseEntity.ok(savedInvoice);
    }


    //Get all invoices for a folder
    @GetMapping("/folder/{folderId}")
    public ResponseEntity<?> getInvoicesByFolder(@PathVariable String folderId) {
        try {
            List<Invoice> invoices = invoiceService.getInvoicesByFolder(folderId);
            return ResponseEntity.ok(invoices);
        } catch (RuntimeException e) {
            // Return 404 if folder is not found or no invoices found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Folder not found or no invoices available for folder ID: " + folderId);
        } catch (Exception e) {
            // Handle any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }


    // Delete an invoice by ID
    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<?> deleteInvoice(@PathVariable String invoiceId) {
        try {
            invoiceService.deleteInvoice(invoiceId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();  // 204 No Content - successful deletion
        } catch (RuntimeException e) {
            // Return 404 if invoice not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invoice not found with ID: " + invoiceId);
        } catch (Exception e) {
            // Handle any other unexpected errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Update an invoice (with or without new image upload)
    @PutMapping("/{invoiceId}")
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable String invoiceId,
            @RequestParam(value = "invoiceName", required = false) String invoiceName,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "folderId", required = false) String folderId,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        InvoiceCreateDTO dto = new InvoiceCreateDTO();
        if (invoiceName != null) dto.setInvoiceName(invoiceName);
        if (status != null) dto.setStatus(status);
        if (folderId != null) dto.setFolderId(folderId);
        if (file != null) dto.setFile(file);  // Update the file if a new one is provided

        Invoice updatedInvoice = invoiceService.updateInvoice(invoiceId, dto, file);
        return ResponseEntity.ok(updatedInvoice);
    }
}





