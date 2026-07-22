package com.shimpimilan.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FcmNotificationService {

    public void sendPushNotification(Long targetUserId, String title, String body, String type) {
        // In a real application, this would use FirebaseAdmin SDK to send an FCM message to the user's device token.
        log.info("FCM MOCK -> Sending Push to User {}: [{}] {} (Type: {})", targetUserId, title, body, type);
    }
}
