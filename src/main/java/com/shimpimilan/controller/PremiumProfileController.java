package com.shimpimilan.controller;

import com.shimpimilan.model.Profile;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.security.PremiumOnly;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/premium-profiles")
@RequiredArgsConstructor
public class PremiumProfileController {

    private final ProfileRepository profileRepository;

    @GetMapping("/{profileId}")
    @PremiumOnly
    public ResponseEntity<Profile> getFullProfileDetails(@PathVariable Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
                
        // Returns the full profile with all unmasked details
        return ResponseEntity.ok(profile);
    }
}
