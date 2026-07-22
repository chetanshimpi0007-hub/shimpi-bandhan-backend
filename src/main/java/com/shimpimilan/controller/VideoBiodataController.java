package com.shimpimilan.controller;

import com.shimpimilan.model.VideoBiodata;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.VideoBiodataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
public class VideoBiodataController {

    private final VideoBiodataService videoBiodataService;

    @PostMapping("/upload")
    public ResponseEntity<VideoBiodata> uploadVideo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody VideoBiodata request) {
        
        request.setUser(userDetails.getUser());
        request.setStatus(VideoBiodata.ApprovalStatus.PENDING); // Require admin approval
        return ResponseEntity.ok(videoBiodataService.saveVideoBiodata(request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<VideoBiodata> getUserVideo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
            
        // If the requesting user is not premium, we still return the record but the frontend will blur it.
        // We could also nullify the URL here for free users, but returning it allows the frontend to show the thumbnail.
        return videoBiodataService.getVideoByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin endpoints
    @PostMapping("/{videoId}/approve")
    public ResponseEntity<VideoBiodata> approveVideo(@PathVariable Long videoId) {
        return ResponseEntity.ok(videoBiodataService.approveVideo(videoId));
    }

    @PostMapping("/{videoId}/reject")
    public ResponseEntity<VideoBiodata> rejectVideo(@PathVariable Long videoId) {
        return ResponseEntity.ok(videoBiodataService.rejectVideo(videoId));
    }
}
