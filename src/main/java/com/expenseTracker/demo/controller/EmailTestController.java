package com.expenseTracker.demo.controller;

import com.expenseTracker.demo.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/email")
@RequiredArgsConstructor
@Tag(name = "Test Endpoints", description = "Endpoints for verifying system functionality")
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/send-test")
    @Operation(summary = "Send Test Email", description = "Sends a test email to the specified address to verify SMTP configuration.")
    public ResponseEntity<String> sendTestEmail(@RequestBody EmailTestRequest request) {
        String to = request.getEmail();
        String subject = "Expense Tracker - Test Email";
        String body = "Hello,\n\nThis is a test email from your local Expense Tracker instance.\n\nIf you are reading this, your SMTP configuration is working correctly!\n\nBest,\nExpense Tracker Team";

        emailService.sendEmail(to, subject, body);

        return ResponseEntity.ok("Test email sent to " + to + ". Check your inbox (and spam folder).");
    }

    @Data
    public static class EmailTestRequest {
        private String email;
    }
}
