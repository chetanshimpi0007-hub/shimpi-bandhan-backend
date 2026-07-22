package com.shimpimilan.repository.notification;

import com.shimpimilan.model.notification.NotificationQueue;
import com.shimpimilan.model.notification.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {
    List<NotificationQueue> findByStatus(com.shimpimilan.model.notification.NotificationStatus status);
    long countByStatus(com.shimpimilan.model.notification.NotificationStatus status);

    
    @Query("SELECT n FROM NotificationQueue n WHERE n.status = 'PENDING' AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= CURRENT_TIMESTAMP)")
    List<NotificationQueue> findEligiblePendingEmails();
}
