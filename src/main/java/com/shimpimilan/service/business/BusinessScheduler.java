package com.shimpimilan.service.business;

import com.shimpimilan.model.business.BusinessSubscription;
import com.shimpimilan.model.business.BusinessStatus;
import com.shimpimilan.model.notification.NotificationQueue;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.business.BusinessSubscriptionRepository;
import com.shimpimilan.repository.notification.NotificationQueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessScheduler {

    private final BusinessSubscriptionRepository subscriptionRepository;
    private final BusinessRepository businessRepository;
    private final com.shimpimilan.repository.business.BusinessOfferRepository offerRepository;
    private final NotificationQueueRepository notificationQueueRepository;

    @Scheduled(cron = "0 0 1 * * ?") // Runs daily at 1:00 AM
    @Transactional
    public void processBusinessExpirations() {
        log.info("Starting Daily Business Expiration Job...");
        
        LocalDateTime now = LocalDateTime.now();

        // 1. Expire past subscriptions
        List<BusinessSubscription> expiredSubs = subscriptionRepository.findByIsActiveTrueAndExpiryDateBefore(now);
        for (BusinessSubscription sub : expiredSubs) {
            sub.setIsActive(false);
            subscriptionRepository.save(sub);

            // Suspend Business visibility
            sub.getBusiness().setStatus(BusinessStatus.EXPIRED);
            businessRepository.save(sub.getBusiness());
            
            queueNotification(sub, "business_expired_template", "Your Business Advertisement has Expired");
        }

        // 1.5 Expire past offers
        log.info("Checking for expired offers...");
        List<com.shimpimilan.model.business.BusinessOffer> expiredOffers = offerRepository.findByIsActiveTrueAndValidUntilBefore(now);
        for (com.shimpimilan.model.business.BusinessOffer offer : expiredOffers) {
            offer.setIsActive(false);
            offerRepository.save(offer);
        }

        // 2. Send Reminders (7 days, 3 days, 1 day)
        LocalDateTime sevenDays = now.plusDays(7);
        LocalDateTime threeDays = now.plusDays(3);
        LocalDateTime oneDay = now.plusDays(1);

        sendRemindersForDateRange(now, sevenDays, sevenDays.plusDays(1), "7 Days");
        sendRemindersForDateRange(now, threeDays, threeDays.plusDays(1), "3 Days");
        sendRemindersForDateRange(now, oneDay, oneDay.plusDays(1), "1 Day");

        log.info("Completed Daily Business Expiration Job.");
    }

    private void sendRemindersForDateRange(LocalDateTime now, LocalDateTime start, LocalDateTime end, String period) {
        List<BusinessSubscription> expiringSubs = subscriptionRepository.findByIsActiveTrueAndExpiryDateBetween(start, end);
        for (BusinessSubscription sub : expiringSubs) {
            queueNotification(sub, "business_expiring_soon_template", "Action Required: Advertisement Expires in " + period);
        }
    }

    private void queueNotification(BusinessSubscription sub, String templateName, String subject) {
        // Construct basic JSON payload (mock)
        String payloadJson = "{\"businessName\":\"" + sub.getBusiness().getBusinessName() + "\"}";
        
        NotificationQueue notification = NotificationQueue.builder()
                .userId(sub.getBusiness().getOwner().getId())
                .emailAddress(sub.getBusiness().getEmail() != null ? sub.getBusiness().getEmail() : "noreply@shimpimilan.com")
                .templateName(templateName)
                .subject(subject)
                .payloadJson(payloadJson)
                .status(com.shimpimilan.model.notification.NotificationStatus.PENDING)
                .retryCount(0)
                .build();
        
        notificationQueueRepository.save(notification);
    }
}
