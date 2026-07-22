package com.shimpimilan.controller;

import com.shimpimilan.dto.ProfileRequest;
import com.shimpimilan.dto.ProfileResponse;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/me")
    public ResponseEntity<ProfileResponse> createOrUpdateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(profileService.createOrUpdateProfile(userDetails.getUser().getId(), request));
    }

    @PostMapping("/save-draft")
    public ResponseEntity<java.util.Map<String, Object>> saveDraft(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileRequest request) {
        Long userId = userDetails.getUser().getId();
        log.info("[SaveDraft] Request received for userId={}, fullName={}", userId, request.getFullName());
        try {
            profileService.createOrUpdateProfile(userId, request);
            log.info("[SaveDraft] SUCCESS for userId={}", userId);
            return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Draft saved successfully"));
        } catch (Exception e) {
            log.error("[SaveDraft] FAILED for userId={}, error={}", userId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userDetails.getUser().getId(), userDetails.getUser()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponse> getProfileById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfileByUserId(userId, userDetails.getUser()));
    }

    @PostMapping("/{userId}/submit-verification")
    public ResponseEntity<ProfileResponse> submitForVerification(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        return ResponseEntity.ok(profileService.submitForVerification(userId, userDetails.getUser()));
    }

    @PostMapping("/submit")
    public ResponseEntity<ProfileResponse> submitProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profileService.submitForVerification(userDetails.getUser().getId(), userDetails.getUser()));
    }


    @GetMapping("/search")
    public ResponseEntity<Page<ProfileResponse>> searchProfiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) Double minHeight,
            @RequestParam(required = false) Double maxHeight,
            @RequestParam(required = false) String maritalStatus,
            @RequestParam(required = false) String education,
            @RequestParam(required = false) String occupation,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Boolean manglik,
            @RequestParam(required = false) Double minIncome,
            @RequestParam(required = false) String gotra,
            @RequestParam(required = false) String familyType,
            @RequestParam(required = false) String lifestyle,
            @RequestParam(required = false) Boolean isPremiumMember,
            @RequestParam(required = false) Boolean isVerifiedProfile,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        // Handle custom sorting if requested
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        if ("premium_first".equalsIgnoreCase(sortBy)) {
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "isPremiumMember", "createdAt");
        } else if ("newest".equalsIgnoreCase(sortBy)) {
            sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        }

        Page<ProfileResponse> results = profileService.searchProfiles(
                userDetails.getUser(), minAge, maxAge, minHeight, maxHeight,
                maritalStatus, education, occupation, city, district, state, manglik,
                minIncome, gotra, familyType, lifestyle, isPremiumMember, isVerifiedProfile,
                PageRequest.of(page, size, sort));
                
        return ResponseEntity.ok(results);
    }
}
