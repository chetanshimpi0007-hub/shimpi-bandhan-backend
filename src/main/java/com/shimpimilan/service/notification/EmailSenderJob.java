package com.shimpimilan.service.notification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shimpimilan.model.notification.NotificationQueue;
import com.shimpimilan.model.notification.NotificationStatus;
import com.shimpimilan.repository.notification.NotificationQueueRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderJob {

    private final NotificationQueueRepository queueRepository;
    private final EmailTemplateService templateService;
    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Run every 60 seconds to pick up emails, wait 60s before starting to avoid startup timeout
    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    @Transactional
    public void processEmailQueue() {
        List<NotificationQueue> pendingEmails = queueRepository.findEligiblePendingEmails();
        
        for (NotificationQueue email : pendingEmails) {
            email.setStatus(NotificationStatus.PROCESSING);
            queueRepository.save(email); // Lock it briefly (Optimistic/Pessimistic locking would be better in a real cluster)
            
            try {
                // Parse payload
                Map<String, Object> payload = objectMapper.readValue(email.getPayloadJson(), new TypeReference<Map<String, Object>>() {});
                
                // Generate HTML
                String htmlBody = templateService.generateHtml(email.getTemplateName(), payload);
                
                // Send email
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(email.getEmailAddress());
                helper.setSubject(email.getSubject());
                helper.setText(htmlBody, true); // true indicates HTML
                
                mailSender.send(message);
                
                // Update status on success
                email.setStatus(NotificationStatus.SENT);
                email.setSentAt(LocalDateTime.now());
                
            } catch (Exception e) {
                log.error("Failed to send email to {}", email.getEmailAddress(), e);
                email.setLastError(e.getMessage());
                email.setRetryCount(email.getRetryCount() + 1);
                
                if (email.getRetryCount() >= 3) {
                    email.setStatus(NotificationStatus.FAILED);
                } else {
                    email.setStatus(NotificationStatus.PENDING);
                    // Next retry backoff:
                    // Retry 1 (after 1st fail): 5 mins
                    // Retry 2 (after 2nd fail): 15 mins
                    // Retry 3 (after 3rd fail): 30 mins (never reached because max retries is 3)
                    int minutesToWait = email.getRetryCount() == 1 ? 5 : 15;
                    email.setNextRetryAt(LocalDateTime.now().plusMinutes(minutesToWait));
                }
            }
            queueRepository.save(email);
        }
    }
}
