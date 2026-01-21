package com.expenseTracker.demo.scheduler;

import com.expenseTracker.demo.entity.User;
import com.expenseTracker.demo.repository.UserRepository;
import com.expenseTracker.demo.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailScheduler {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 1 1 * ?")
    public void sendMonthlyReports() {
        log.info("Starting monthly expense report email job");
        
        List<User> activeUsers = userRepository.findAll().stream()
                .filter(User::getIsActive)
                .toList();

        log.info("Sending monthly reports to {} active users", activeUsers.size());

        for (User user : activeUsers) {
            try {
                emailService.sendMonthlyReport(user);
            } catch (Exception e) {
                log.error("Failed to send report to user: {}", user.getEmail(), e);
            }
        }

        log.info("Monthly expense report email job completed");
    }
}
