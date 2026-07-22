package com.shimpimilan.controller;

import com.shimpimilan.model.User;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/referral")
@RequiredArgsConstructor
public class ReferralController {

    private final UserRepository userRepository;

    @GetMapping("/validate/{code}")
    public ResponseEntity<?> validateReferralCode(@PathVariable String code) {
        User referrer = userRepository.findByReferralCode(code).orElse(null);
        
        if (referrer == null) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Invalid Referral Code"));
        }

        if (referrer.getStatus() != com.shimpimilan.model.UserStatus.APPROVED) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "message", "Referral code belongs to an inactive account"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("message", "Referral Code is valid.");
        response.put("referrerName", referrer.getProfile() != null ? referrer.getProfile().getFullName() : "A Shimpi Milan Member");
        
        return ResponseEntity.ok(response);
    }
}
