package com.shimpimilan.controller;

import com.shimpimilan.dto.ProfileResponse;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.ProfileViewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/views")
@RequiredArgsConstructor
public class ProfileViewController {

    private final ProfileViewService profileViewService;
    private final UserRepository userRepository;

    @PostMapping("/record/{viewedUserId}")
    public ResponseEntity<Void> recordView(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long viewedUserId,
            @RequestHeader(value = "User-Agent", defaultValue = "Unknown") String device) {
        
        User viewedUser = userRepository.findById(viewedUserId).orElseThrow(() -> new RuntimeException("User not found"));
        profileViewService.recordView(userDetails.getUser(), viewedUser, device);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/visitors")
    public ResponseEntity<Page<ProfileResponse>> getMyVisitors(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        return ResponseEntity.ok(profileViewService.getProfileVisitors(userDetails.getUser(), PageRequest.of(page, size)));
    }
}
