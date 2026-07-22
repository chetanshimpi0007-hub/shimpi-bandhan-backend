package com.shimpimilan.controller;

import com.shimpimilan.model.notification.NotificationQueue;
import com.shimpimilan.model.notification.NotificationStatus;
import com.shimpimilan.repository.notification.NotificationQueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationQueueRepository queueRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getStats() {
        long total = queueRepository.count();
        long pending = queueRepository.findByStatus(NotificationStatus.PENDING).size();
        long sent = queueRepository.findByStatus(NotificationStatus.SENT).size();
        long failed = queueRepository.findByStatus(NotificationStatus.FAILED).size();
        long processing = queueRepository.findByStatus(NotificationStatus.PROCESSING).size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("sent", sent);
        stats.put("failed", failed);
        stats.put("processing", processing);
        stats.put("successRate", total > 0 ? (sent * 100.0 / total) : 0);
        stats.put("failureRate", total > 0 ? (failed * 100.0 / total) : 0);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/queue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getQueue(@RequestParam(required = false) NotificationStatus status) {
        if (status != null) {
            return ResponseEntity.ok(queueRepository.findByStatus(status));
        }
        return ResponseEntity.ok(queueRepository.findAll());
    }

    @PostMapping("/retry/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> retryNotification(@PathVariable Long id) {
        NotificationQueue notification = queueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
                
        notification.setStatus(NotificationStatus.PENDING);
        notification.setRetryCount(0);
        notification.setNextRetryAt(null);
        queueRepository.save(notification);
        
        return ResponseEntity.ok(Map.of("message", "Notification queued for retry."));
    }
}
