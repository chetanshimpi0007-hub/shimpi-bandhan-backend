package com.shimpimilan.controller;
// Trigger restart for config update

import com.shimpimilan.model.photo.PhotoType;
import com.shimpimilan.model.photo.ProfilePhoto;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.ProfilePhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile/photos")
@RequiredArgsConstructor
public class ProfilePhotoController {

    private final ProfilePhotoService profilePhotoService;

    @PostMapping
    public ResponseEntity<ProfilePhoto> uploadPhoto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "PRIMARY") PhotoType type) throws IOException {
        
        return ResponseEntity.ok(profilePhotoService.uploadPhoto(userDetails.getUser(), file, type));
    }

    @GetMapping
    public ResponseEntity<List<ProfilePhoto>> getMyPhotos(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(profilePhotoService.getPhotos(userDetails.getUser().getId()));
    }

    @PutMapping("/{id}/primary")
    public ResponseEntity<Void> setPrimaryPhoto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        
        profilePhotoService.setPrimaryPhoto(userDetails.getUser().getId(), id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        
        profilePhotoService.deletePhoto(userDetails.getUser().getId(), id);
        return ResponseEntity.ok().build();
    }
}
