package com.shimpimilan.service;

import com.shimpimilan.model.*;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.SubscriptionRepository;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void startFreeTrial(User user) {
        if (Boolean.TRUE.equals(user.getFreeTrialUsed())) {
            throw new RuntimeException("Free trial already used by this user.");
        }

        Profile profile = user.getProfile();
        if (profile == null) {
            throw new RuntimeException("User profile not found.");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime trialEnd = now.plusDays(30);

        // Update User
        user.setFreeTrialUsed(true);
        userRepository.save(user);

        // Update Profile
        profile.setPlanType(PlanType.FREE_TRIAL);
        profile.setMembershipSource(MembershipSource.TRIAL);
        profile.setTrialStartDate(now);
        profile.setTrialEndDate(trialEnd);
        profile.setIsPremiumMember(true);
        profile.setPremiumExpiryDate(trialEnd);
        profileRepository.save(profile);

        // Create Subscription Record
        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(PlanType.FREE_TRIAL)
                .source(MembershipSource.TRIAL)
                .startDate(now)
                .endDate(trialEnd)
                .isActive(true)
                .build();
        subscriptionRepository.save(subscription);

        // Audit Log
        AuditLog auditLog = AuditLog.builder()
                .action("TRIAL_ACTIVATED")
                .userId(user.getId())
                .targetId(subscription.getId())
                .timestamp(now)
                .details("30-Day Free Trial activated automatically upon approval.")
                .build();
        auditLogRepository.save(auditLog);
    }
    
    @Transactional
    public void activatePremium(User user, int days, String orderId, String paymentId, MembershipSource source) {
        Profile profile = user.getProfile();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusDays(days);
        
        // If they already have an active premium/trial, we should extend from the end date.
        if (Boolean.TRUE.equals(profile.getIsPremiumMember()) && profile.getPremiumExpiryDate() != null && profile.getPremiumExpiryDate().isAfter(now)) {
            expiry = profile.getPremiumExpiryDate().plusDays(days);
        }

        profile.setPlanType(PlanType.PREMIUM);
        profile.setMembershipSource(source);
        profile.setIsPremiumMember(true);
        profile.setLastRenewalDate(now);
        profile.setNextRenewalDate(expiry);
        profile.setPremiumExpiryDate(expiry);
        profileRepository.save(profile);

        // Terminate existing active subscriptions for this user
        subscriptionRepository.findByUserIdAndIsActiveTrue(user.getId()).forEach(sub -> {
            sub.setIsActive(false);
            subscriptionRepository.save(sub);
        });

        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(PlanType.PREMIUM)
                .source(source)
                .startDate(now)
                .endDate(expiry)
                .isActive(true)
                .razorpayOrderId(orderId)
                .razorpayPaymentId(paymentId)
                .build();
        subscriptionRepository.save(subscription);
        
        AuditLog auditLog = AuditLog.builder()
                .action("PREMIUM_ACTIVATED")
                .userId(user.getId())
                .targetId(subscription.getId())
                .timestamp(now)
                .details("Premium activated for " + days + " days. Source: " + source)
                .build();
        auditLogRepository.save(auditLog);
    }
}
