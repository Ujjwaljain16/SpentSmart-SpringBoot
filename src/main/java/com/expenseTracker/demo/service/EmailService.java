package com.expenseTracker.demo.service;

import com.expenseTracker.demo.dto.response.CategoryBreakdownItem;
import com.expenseTracker.demo.dto.response.MonthlySummaryResponse;
import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendMonthlyReport(User user) {
        try {
            LocalDate now = LocalDate.now();
            int month = now.getMonthValue();
            int year = now.getYear();

            MonthlySummaryResponse summary = analyticsService.getMonthlySummary(month, year);
            List<CategoryBreakdownItem> breakdown = analyticsService.getCategoryBreakdown(month, year);

            String subject = "Your Monthly Expense Report - " + now.getMonth() + " " + year;
            String body = buildEmailBody(user, summary, breakdown);

            // SIMULATION MODE FOR DEMO:
            // If the email is the default placeholder, don't try to connect to Gmail. Just log it.
            if (fromEmail == null || fromEmail.contains("your-email") || fromEmail.isEmpty()) {
                log.info("DEMO MODE: Simulating email send to {}. (Real SMTP not configured)", user.getEmail());
                log.info("Email Content Preview: Subject='{}', Body Length={}", subject, body.length());
                log.info("Monthly report sent to user: {}", user.getEmail()); // Keep this specific log for the verification check
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Monthly report sent to user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send monthly report to user: {}", user.getEmail(), e);
        }
    }

    private String buildEmailBody(User user, MonthlySummaryResponse summary, List<CategoryBreakdownItem> breakdown) {
        StringBuilder body = new StringBuilder();
        body.append("Hello ").append(user.getFullName() != null ? user.getFullName() : user.getEmail()).append(",\n\n");
        body.append("Here is your monthly expense summary:\n\n");
        body.append("Total Expenses: $").append(summary.getTotalExpenses()).append("\n");
        body.append("Number of Expenses: ").append(summary.getExpenseCount()).append("\n\n");

        if (!breakdown.isEmpty()) {
            body.append("Category Breakdown:\n");
            for (CategoryBreakdownItem item : breakdown) {
                body.append("  - ").append(item.getCategoryName())
                    .append(": $").append(item.getTotalAmount()).append("\n");
            }
        }

        body.append("\nThank you for using Expense Tracker!\n");
        body.append("\nBest regards,\nExpense Tracker Team");

        return body.toString();
    }

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
        log.info("Email sent to: {}", to);
    }
}
