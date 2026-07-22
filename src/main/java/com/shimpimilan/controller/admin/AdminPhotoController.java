package com.shimpimilan.controller.admin;

import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.photo.ProfilePhoto;
import com.shimpimilan.repository.ProfilePhotoRepository;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.ProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/photos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPhotoController {

    private final ProfilePhotoService profilePhotoService;
    private final ProfilePhotoRepository profilePhotoRepository;

    @GetMapping("/pending")
    public ResponseEntity<Page<ProfilePhoto>> getPendingPhotos(Pageable pageable) {
        return ResponseEntity.ok(profilePhotoRepository.findByStatus(PhotoStatus.PENDING, pageable));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<ProfilePhoto> approvePhoto(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profilePhotoService.approvePhoto(id, userDetails.getUser()));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ProfilePhoto> rejectPhoto(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String reason = request.getOrDefault("rejectionReason", "Does not meet guidelines");
        return ResponseEntity.ok(profilePhotoService.rejectPhoto(id, reason, userDetails.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        profilePhotoService.adminDeletePhoto(id);
        return ResponseEntity.noContent().build();
    }
}
