package com.shimpimilan.controller;

import com.shimpimilan.model.Kundali;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.KundaliRepository;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.security.PremiumOnly;
import com.shimpimilan.service.AuditLogService;
import com.shimpimilan.service.InterestService;
import com.shimpimilan.service.KundaliService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/kundali")
@RequiredArgsConstructor
public class KundaliController {

    private final KundaliService kundaliService;
    private final KundaliRepository kundaliRepository;
    private final UserRepository userRepository;
    private final InterestService interestService;
    private final AuditLogService auditLogService;

    @GetMapping("/match/{targetUserId}")
    @PremiumOnly
    public ResponseEntity<?> matchKundali(@PathVariable Long targetUserId) {
        String phone = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getId().equals(targetUserId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot match kundali with yourself."));
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // Check User Statuses
        if (currentUser.getStatus() == com.shimpimilan.model.UserStatus.BLOCKED || currentUser.getStatus() == com.shimpimilan.model.UserStatus.SUSPENDED) {
            throw new RuntimeException("Your account is blocked or suspended.");
        }
        if (targetUser.getStatus() == com.shimpimilan.model.UserStatus.BLOCKED || targetUser.getStatus() == com.shimpimilan.model.UserStatus.SUSPENDED) {
            throw new RuntimeException("The target user's account is unavailable.");
        }

        // 1. Verify Mutual Interest (This also inherently proves they are not purely rejected or cancelled)
        if (!interestService.isMutualInterestEstablished(currentUser.getId(), targetUserId)) {
            throw new RuntimeException("Kundali match requires an ACCEPTED mutual interest.");
        }

        // 2. Fetch Kundali Details
        Kundali myKundali = kundaliRepository.findByUserId(currentUser.getId()).orElse(null);
        Kundali theirKundali = kundaliRepository.findByUserId(targetUserId).orElse(null);

        if (myKundali == null || theirKundali == null) {
            throw new RuntimeException("One or both users have not provided birth details for Kundali matching.");
        }

        // 3. Perform Match
        Map<String, Object> result = kundaliService.calculateGunMilan(currentUser.getId(), myKundali, theirKundali);

        // 4. Log Action
        auditLogService.logAction("KUNDALI_MATCHED", currentUser.getId(), targetUserId, "Performed Kundali Match");

        return ResponseEntity.ok(result);
    }
}
