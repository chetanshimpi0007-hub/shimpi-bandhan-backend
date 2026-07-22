package com.shimpimilan.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shimpimilan.model.User;
import com.shimpimilan.model.notification.InAppNotification;
import com.shimpimilan.model.notification.NotificationQueue;
import com.shimpimilan.model.notification.NotificationStatus;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.notification.InAppNotificationRepository;
import com.shimpimilan.repository.notification.NotificationQueueRepository;
import com.shimpimilan.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationQueueRepository queueRepository;
    private final InAppNotificationRepository inAppRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void queueEmail(Long userId, String emailAddress, String templateName, String subject, Map<String, Object> payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);
            
            NotificationQueue queueItem = NotificationQueue.builder()
                    .userId(userId)
                    .emailAddress(emailAddress)
                    .templateName(templateName)
                    .subject(subject)
                    .payloadJson(jsonPayload)
                    .status(NotificationStatus.PENDING)
                    .build();
                    
            queueRepository.save(queueItem);
            
            auditLogService.logAction("EMAIL_QUEUED", userId, userId, "Email queued for template: " + templateName);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize email payload for user {}", userId, e);
        }
    }

    public void sendInAppNotification(Long userId, String title, String message) {
        InAppNotification notification = InAppNotification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .build();
                
        inAppRepository.save(notification);
        auditLogService.logAction("IN_APP_NOTIFICATION", userId, userId, "Notification sent: " + title);
    }
    
    public void notifyUser(Long userId, String templateName, String title, String message, Map<String, Object> payload) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getProfile() != null && user.getProfile().getEmail() != null) {
            queueEmail(userId, user.getProfile().getEmail(), templateName, title, payload);
        }
        sendInAppNotification(userId, title, message);
    }
}
