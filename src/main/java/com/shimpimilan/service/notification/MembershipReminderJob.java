package com.shimpimilan.service.notification;

import com.shimpimilan.model.Profile;
import com.shimpimilan.model.PlanType;
import com.shimpimilan.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipReminderJob {

    private final ProfileRepository profileRepository;
    private final NotificationService notificationService;

    // Run every day at 12:00 PM (cron: second minute hour day month weekday)
    @Scheduled(cron = "0 0 12 * * ?")
    @Transactional
    public void checkExpiries() {
        log.info("Starting MembershipReminderJob to check expiries");
        
        List<Profile> premiumProfiles = profileRepository.findByPlanTypeIn(List.of(PlanType.PREMIUM, PlanType.FREE_TRIAL));
        
        LocalDate today = LocalDate.now();
        
        for (Profile profile : premiumProfiles) {
            if (profile.getPremiumExpiryDate() == null) continue;
            
            LocalDate expiryDate = profile.getPremiumExpiryDate().toLocalDate();
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
            
            String templateName = profile.getPlanType() == PlanType.FREE_TRIAL ? "trial-expiry-reminder" : "premium-expiry-reminder";
            
            if (daysLeft == 7 || daysLeft == 3 || daysLeft == 1) {
                Map<String, Object> payload = new HashMap<>();
                payload.put("name", profile.getFullName());
                payload.put("message", "Your " + profile.getPlanType().name() + " membership will expire in " + daysLeft + " days.");
                payload.put("actionUrl", "https://shimpibandhan.com/premium");
                
                notificationService.notifyUser(
                    profile.getUser().getId(),
                    templateName,
                    "Membership Expiry Reminder",
                    "Your membership expires in " + daysLeft + " days. Upgrade now to keep premium benefits.",
                    payload
                );
            } else if (daysLeft <= 0) {
                // Expired
                profile.setPlanType(PlanType.FREE);
                profileRepository.save(profile);
                
                Map<String, Object> payload = new HashMap<>();
                payload.put("name", profile.getFullName());
                payload.put("message", "Your " + profile.getPlanType().name() + " membership has expired. You have been downgraded to the Free Plan.");
                payload.put("actionUrl", "https://shimpibandhan.com/premium");
                
                notificationService.notifyUser(
                    profile.getUser().getId(),
                    "premium-expired",
                    "Membership Expired",
                    "Your premium membership has expired.",
                    payload
                );
            }
        }
    }
}
