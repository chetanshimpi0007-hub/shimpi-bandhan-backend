package com.shimpimilan.model.notification;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_queue", indexes = {
    @Index(name = "idx_status_retry", columnList = "status, next_retry_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String emailAddress;

    @Column(nullable = false)
    private String templateName;

    @Column(nullable = false)
    private String subject;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payloadJson; // To hold dynamic parameters for the template

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String lastError;

    private LocalDateTime nextRetryAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;
}
