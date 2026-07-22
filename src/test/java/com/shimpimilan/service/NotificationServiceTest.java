package com.shimpimilan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shimpimilan.model.notification.NotificationQueue;
import com.shimpimilan.model.notification.NotificationStatus;
import com.shimpimilan.repository.notification.NotificationQueueRepository;
import com.shimpimilan.service.notification.EmailSenderJob;
import com.shimpimilan.service.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationQueueRepository queueRepository;

    @Autowired
    private EmailSenderJob emailSenderJob;

    @MockBean
    private JavaMailSender mailSender;

    @BeforeEach
    public void setup() {
        queueRepository.deleteAll();
    }

    @Test
    public void testQueueCreation() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test User");

        notificationService.queueEmail(1L, "test@example.com", "welcome-email", "Welcome", payload);

        List<NotificationQueue> pending = queueRepository.findByStatus(NotificationStatus.PENDING);
        assertEquals(1, pending.size());
        assertEquals("test@example.com", pending.get(0).getEmailAddress());
    }

    @Test
    public void testSuccessfulDelivery() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test User");
        notificationService.queueEmail(1L, "test@example.com", "welcome-email", "Welcome", payload);

        // Process queue
        emailSenderJob.processEmailQueue();

        List<NotificationQueue> sent = queueRepository.findByStatus(NotificationStatus.SENT);
        assertEquals(1, sent.size());
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    public void testRetryLogicAndPermanentFailure() {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("SMTP Error")).when(mailSender).send(mimeMessage);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test User");
        notificationService.queueEmail(1L, "fail@example.com", "welcome-email", "Welcome", payload);

        // Attempt 1
        emailSenderJob.processEmailQueue();
        List<NotificationQueue> pending = queueRepository.findByStatus(NotificationStatus.PENDING);
        assertEquals(1, pending.size());
        assertEquals(1, pending.get(0).getRetryCount());
        assertNotNull(pending.get(0).getNextRetryAt());

        // Override nextRetryAt to simulate time passing for attempt 2
        NotificationQueue q = pending.get(0);
        q.setNextRetryAt(java.time.LocalDateTime.now().minusMinutes(1));
        queueRepository.save(q);

        // Attempt 2
        emailSenderJob.processEmailQueue();
        q = queueRepository.findById(q.getId()).get();
        assertEquals(2, q.getRetryCount());

        // Override nextRetryAt to simulate time passing for attempt 3
        q.setNextRetryAt(java.time.LocalDateTime.now().minusMinutes(1));
        queueRepository.save(q);

        // Attempt 3
        emailSenderJob.processEmailQueue();
        q = queueRepository.findById(q.getId()).get();
        assertEquals(3, q.getRetryCount());

        // Override nextRetryAt to simulate time passing for attempt 4
        q.setNextRetryAt(java.time.LocalDateTime.now().minusMinutes(1));
        queueRepository.save(q);

        // Attempt 4 (Final Failure)
        emailSenderJob.processEmailQueue();
        
        List<NotificationQueue> failed = queueRepository.findByStatus(NotificationStatus.FAILED);
        assertEquals(1, failed.size());
        assertEquals(3, failed.get(0).getRetryCount());
    }
}
