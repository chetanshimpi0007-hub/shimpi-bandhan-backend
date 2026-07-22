package com.shimpimilan.controller;

import com.shimpimilan.model.LinkedCandidateAccount;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.LinkedCandidateAccountRepository;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/family")
@RequiredArgsConstructor
public class AdminFamilyController {

    private final LinkedCandidateAccountRepository linkedCandidateAccountRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @PostMapping("/link")
    public ResponseEntity<?> linkCandidateAccount(
            @RequestParam Long profileId,
            @RequestParam Long candidateUserId) {

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        User candidateUser = userRepository.findById(candidateUserId)
                .orElseThrow(() -> new RuntimeException("Candidate user not found"));

        LinkedCandidateAccount link = LinkedCandidateAccount.builder()
                .profile(profile)
                .candidateUser(candidateUser)
                .build();

        linkedCandidateAccountRepository.save(link);
        return ResponseEntity.ok("Successfully linked candidate account.");
    }
}
