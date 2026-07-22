package com.shimpimilan.service;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.model.PlanType;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.Subscription;
import com.shimpimilan.repository.AuditLogRepository;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipScheduler {

    private final ProfileRepository profileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 0 * * *") // Runs every midnight
    @Transactional
    public void processExpiredMemberships() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Process Expired Free Trials
        List<Profile> expiredTrials = profileRepository.findByPlanTypeAndTrialEndDateBefore(PlanType.FREE_TRIAL, now);
        for (Profile profile : expiredTrials) {
            profile.setPlanType(PlanType.FREE);
            profile.setIsPremiumMember(false);
            profileRepository.save(profile);

            deactivateActiveSubscriptions(profile.getUser().getId());

            AuditLog auditLog = AuditLog.builder()
                    .action("TRIAL_EXPIRED")
                    .userId(profile.getUser().getId())
                    .timestamp(now)
                    .details("Free trial expired automatically.")
                    .build();
            auditLogRepository.save(auditLog);
        }

        // 2. Process Expired Premium Memberships
        List<Profile> expiredPremiums = profileRepository.findByPlanTypeAndPremiumExpiryDateBefore(PlanType.PREMIUM, now);
        for (Profile profile : expiredPremiums) {
            profile.setPlanType(PlanType.FREE);
            profile.setIsPremiumMember(false);
            profileRepository.save(profile);

            deactivateActiveSubscriptions(profile.getUser().getId());

            AuditLog auditLog = AuditLog.builder()
                    .action("PREMIUM_EXPIRED")
                    .userId(profile.getUser().getId())
                    .timestamp(now)
                    .details("Premium membership expired automatically.")
                    .build();
            auditLogRepository.save(auditLog);
        }
    }

    private void deactivateActiveSubscriptions(Long userId) {
        List<Subscription> activeSubs = subscriptionRepository.findByUserIdAndIsActiveTrue(userId);
        for (Subscription sub : activeSubs) {
            sub.setIsActive(false);
            subscriptionRepository.save(sub);
        }
    }
}
