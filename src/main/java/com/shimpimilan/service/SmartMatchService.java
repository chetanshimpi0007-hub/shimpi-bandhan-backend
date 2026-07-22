package com.shimpimilan.service;

import com.shimpimilan.model.User;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmartMatchService {

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final CompatibilityService compatibilityService;

    // Run every morning at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void generateDailySmartMatches() {
        log.info("Starting Daily Smart Match generation...");
        
        List<User> activeUsers = userRepository.findAll(); // In a real app, query only active users looking for matches

        for (User user : activeUsers) {
            try {
                // Here we would use FCM to send a push notification
                // e.g. fcmService.sendNotification(user.getFcmToken(), "Your Daily Smart Matches are ready!");
                log.info("Generated smart matches for user: " + user.getPhone());
            } catch (Exception e) {
                log.error("Failed to generate match for user " + user.getId(), e);
            }
        }
        
        log.info("Finished Daily Smart Match generation.");
    }
}
