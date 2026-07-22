package com.shimpimilan.security;

import com.shimpimilan.model.PlanType;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PremiumAccessAspect {

    private final UserRepository userRepository;

    @Before("@annotation(PremiumOnly)")
    public void checkPremiumAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Authentication required to access this resource.");
        }

        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = user.getProfile();
        if (profile == null) {
            throw new RuntimeException("Profile not found. Complete profile first.");
        }

        if (profile.getPlanType() == PlanType.FREE) {
            throw new RuntimeException("Premium membership required. Please upgrade your plan.");
        }
        
        // Additional checks like expiration logic happen in the scheduled job, 
        // but we can enforce strict real-time verification here if desired.
        if (profile.getPlanType() == PlanType.PREMIUM) {
            if (profile.getPremiumExpiryDate() == null || profile.getPremiumExpiryDate().isBefore(java.time.LocalDateTime.now())) {
                throw new RuntimeException("Premium membership has expired. Please renew.");
            }
        }
        
        if (profile.getPlanType() == PlanType.FREE_TRIAL) {
            if (profile.getTrialEndDate() == null || profile.getTrialEndDate().isBefore(java.time.LocalDateTime.now())) {
                throw new RuntimeException("Free Trial has expired. Please upgrade to Premium.");
            }
        }
    }
}
