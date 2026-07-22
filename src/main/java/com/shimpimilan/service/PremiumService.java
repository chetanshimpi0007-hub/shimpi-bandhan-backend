package com.shimpimilan.service;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.AuditLogRepository;
import com.shimpimilan.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PremiumService {

    private final ProfileRepository profileRepository;
    private final AuditLogRepository auditLogRepository;

    public void checkPremiumAccess(User user, String featureName) {
        Profile profile = user.getProfile();
        if (profile == null) {
            throw new RuntimeException("Profile not found. Cannot access " + featureName);
        }

        if (!Boolean.TRUE.equals(profile.getIsPremiumMember()) || profile.getPremiumExpiryDate() == null || profile.getPremiumExpiryDate().isBefore(LocalDateTime.now())) {
            logAudit("PREMIUM_ACCESS_DENIED", user.getId(), user.getId(), "Denied access to feature: " + featureName);
            throw new RuntimeException("Active premium membership required to access: " + featureName);
        }
    }

    public void grantPremium(User user, int days) {
        Profile profile = user.getProfile();
        if (profile == null) {
            throw new RuntimeException("Profile not found");
        }

        LocalDateTime newExpiry;
        if (profile.getPremiumExpiryDate() != null && profile.getPremiumExpiryDate().isAfter(LocalDateTime.now())) {
            newExpiry = profile.getPremiumExpiryDate().plusDays(days);
        } else {
            newExpiry = LocalDateTime.now().plusDays(days);
        }

        profile.setIsPremiumMember(true);
        profile.setPremiumExpiryDate(newExpiry);
        profileRepository.save(profile);

        logAudit("PREMIUM_RENEWED", user.getId(), user.getId(), "Granted premium for " + days + " days. New expiry: " + newExpiry);
    }

    private void logAudit(String action, Long userId, Long targetId, String details) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .userId(userId)
                .targetId(targetId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }
}
