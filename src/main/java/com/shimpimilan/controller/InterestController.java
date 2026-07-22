package com.shimpimilan.controller;

import com.shimpimilan.model.Interest;
import com.shimpimilan.model.InterestStatus;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<Interest> sendInterest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long receiverId) {
        return ResponseEntity.ok(interestService.sendInterest(userDetails.getUser(), receiverId));
    }

    @PutMapping("/{interestId}/status")
    public ResponseEntity<Interest> updateInterestStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long interestId,
            @RequestParam InterestStatus status) {
        return ResponseEntity.ok(interestService.updateInterestStatus(userDetails.getUser(), interestId, status));
    }

    @GetMapping("/received")
    public ResponseEntity<List<Interest>> getReceivedInterests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(interestService.getReceivedInterests(userDetails.getUser()));
    }

    @GetMapping("/sent")
    public ResponseEntity<List<Interest>> getSentInterests(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(interestService.getSentInterests(userDetails.getUser()));
    }
}
