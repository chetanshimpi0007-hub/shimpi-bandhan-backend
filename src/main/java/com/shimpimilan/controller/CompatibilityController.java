package com.shimpimilan.controller;

import com.shimpimilan.dto.CompatibilityResultDTO;
import com.shimpimilan.model.Profile;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.CompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/compatibility")
@RequiredArgsConstructor
public class CompatibilityController {

    private final CompatibilityService compatibilityService;
    private final ProfileRepository profileRepository;

    @GetMapping("/{targetUserId}")
    public ResponseEntity<CompatibilityResultDTO> getCompatibilityScore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long targetUserId) {
        
        Profile userProfile = profileRepository.findByUserId(userDetails.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Your profile is incomplete."));
        
        Profile targetProfile = profileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target profile not found."));

        return ResponseEntity.ok(compatibilityService.calculateCompatibility(userProfile, targetProfile));
    }
}
