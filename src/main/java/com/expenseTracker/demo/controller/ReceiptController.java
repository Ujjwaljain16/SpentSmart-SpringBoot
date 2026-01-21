package com.expenseTracker.demo.controller;

import com.expenseTracker.demo.entity.Receipt;
import com.expenseTracker.demo.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/expenses/{expenseId}/receipt")
@RequiredArgsConstructor
@Tag(name = "Receipts", description = "Receipt file upload and download endpoints")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('USER')")
public class ReceiptController {

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload receipt", description = "Upload a receipt file for an expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Receipt uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or file already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Expense not found")
    })
    public ResponseEntity<Receipt> uploadReceipt(
            @PathVariable UUID expenseId,
            @RequestParam("file") MultipartFile file) {
        Receipt receipt = fileStorageService.uploadReceipt(expenseId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(receipt);
    }

    @GetMapping
    @Operation(summary = "Download receipt", description = "Download the receipt file for an expense")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Receipt downloaded successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Expense or receipt not found")
    })
    public ResponseEntity<Resource> downloadReceipt(@PathVariable UUID expenseId) {
        Resource resource = fileStorageService.downloadReceipt(expenseId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
