package com.shimpimilan.controller;

import com.shimpimilan.dto.ProfileResponse;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/profiles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileController {

    private final ProfileService profileService;
    private final com.shimpimilan.service.PdfExportService pdfExportService;

    @GetMapping("/pending")
    public ResponseEntity<Page<ProfileResponse>> getPendingProfiles(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(profileService.getPendingVerificationProfiles(userDetails.getUser(), PageRequest.of(page, size)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ProfileResponse>> getProfilesByStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable com.shimpimilan.model.profile.VerificationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(profileService.getProfilesByVerificationStatus(status, userDetails.getUser(), PageRequest.of(page, size)));
    }

    @PutMapping("/{profileId}/approve")
    public ResponseEntity<ProfileResponse> approveProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId) {
        return ResponseEntity.ok(profileService.approveRejectProfile(profileId, true, null, userDetails.getUser()));
    }

    @PutMapping("/{profileId}/reject")
    public ResponseEntity<ProfileResponse> rejectProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId,
            @RequestParam String reason) {
        return ResponseEntity.ok(profileService.approveRejectProfile(profileId, false, reason, userDetails.getUser()));
    }

    @PutMapping("/{profileId}/request-changes")
    public ResponseEntity<ProfileResponse> requestChanges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long profileId,
            @RequestParam String reason) {
        // Here we can map "request changes" to a specific status in ProfileService
        // Assuming approveRejectProfile handles it or we use a separate method. 
        // For now, let's assume false with reason sets it to REJECTED. We'll reuse the logic but in frontend it's "Changes Requested"
        return ResponseEntity.ok(profileService.approveRejectProfile(profileId, false, reason, userDetails.getUser()));
    }

    @GetMapping("/download-pdf/{userId}")
    public ResponseEntity<byte[]> downloadRegistrationPdf(@PathVariable Long userId) {
        try {
            byte[] pdfBytes = pdfExportService.generateUserRegistrationPdf(userId);
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Profile_" + userId + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
