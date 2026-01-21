package com.expenseTracker.demo.service;

import com.expenseTracker.demo.entity.Expense;
import com.expenseTracker.demo.entity.Receipt;
import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.exception.ResourceNotFoundException;
import com.expenseTracker.demo.repository.ExpenseRepository;
import com.expenseTracker.demo.repository.ReceiptRepository;
import com.expenseTracker.demo.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final ExpenseRepository expenseRepository;
    private final ReceiptRepository receiptRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional
    public Receipt uploadReceipt(UUID expenseId, MultipartFile file) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findByIdAndUserAndIsDeletedFalse(expenseId, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.EXPENSE_NOT_FOUND));

        if (receiptRepository.existsByExpense(expense)) {
            throw new IllegalArgumentException("Receipt already exists for this expense");
        }

        validateFile(file);

        String fileName = generateFileName(file.getOriginalFilename());
        String filePath = saveFile(file, user.getId(), expenseId, fileName);

        Receipt receipt = Receipt.builder()
                .expense(expense)
                .fileName(fileName)
                .filePath(filePath)
                .fileSize(file.getSize())
                .fileType(file.getContentType())
                .uploadedAt(LocalDateTime.now())
                .build();

        return receiptRepository.save(receipt);
    }

    @Transactional(readOnly = true)
    public Resource downloadReceipt(UUID expenseId) {
        User user = getCurrentUser();

        Expense expense = expenseRepository.findByIdAndUserAndIsDeletedFalse(expenseId, user)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.ErrorMessages.EXPENSE_NOT_FOUND));

        Receipt receipt = receiptRepository.findByExpense(expense)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));

        try {
            Path file = Paths.get(receipt.getFilePath());
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > Constants.Validation.MAX_FILE_SIZE) {
            throw new IllegalArgumentException(Constants.ErrorMessages.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        boolean isValidType = Arrays.stream(Constants.FileUpload.ALLOWED_IMAGE_TYPES).anyMatch(type -> type.equals(contentType)) ||
                              Arrays.stream(Constants.FileUpload.ALLOWED_DOCUMENT_TYPES).anyMatch(type -> type.equals(contentType));

        if (!isValidType) {
            throw new IllegalArgumentException(Constants.ErrorMessages.INVALID_FILE_TYPE);
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String saveFile(MultipartFile file, UUID userId, UUID expenseId, String fileName) {
        try {
            Path userDir = Paths.get(uploadDir, userId.toString(), expenseId.toString());
            Files.createDirectories(userDir);

            Path targetLocation = userDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
