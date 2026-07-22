package com.shimpimilan.controller;

import com.shimpimilan.dto.PublicProfileDTO;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.UserStatus;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.ProfilePhotoRepository;
import com.shimpimilan.model.photo.ProfilePhoto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/public")
@RequiredArgsConstructor
public class PublicProfileController {

    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;

    @GetMapping("/profiles/latest")
    public ResponseEntity<List<PublicProfileDTO>> getLatestApprovedProfiles() {
        List<Profile> profiles = profileRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")).stream()
                .filter(p -> p.getUser() != null && p.getUser().getStatus() == UserStatus.APPROVED)
                .filter(p -> p.getIsVerifiedProfile() != null && p.getIsVerifiedProfile())
                .limit(10)
                .collect(Collectors.toList());

        List<PublicProfileDTO> publicProfiles = profiles.stream()
                .map(this::mapToPublicDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(publicProfiles);
    }

    @GetMapping("/profiles")
    public ResponseEntity<List<PublicProfileDTO>> getLimitedPublicProfiles() {
        // Fetch up to 10 active and verified profiles
        List<Profile> profiles = profileRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getStatus() == UserStatus.APPROVED)
                .filter(p -> p.getIsVerifiedProfile() != null && p.getIsVerifiedProfile())
                .limit(10)
                .collect(Collectors.toList());

        List<PublicProfileDTO> publicProfiles = profiles.stream()
                .map(this::mapToPublicDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(publicProfiles);
    }

    private PublicProfileDTO mapToPublicDTO(Profile p) {
        String fullName = p.getFullName() != null ? p.getFullName() : "";
        String firstName = fullName.contains(" ") ? fullName.substring(0, fullName.indexOf(" ")) : fullName;
        
        String photoUrl = profilePhotoRepository.findByUserIdAndIsPrimaryTrue(p.getUser().getId())
                .map(ProfilePhoto::getPhotoUrl).orElse(null);
        
        return PublicProfileDTO.builder()
                .id(p.getId())
                .displayName(firstName)
                .age(p.getAge())
                .profilePhoto(photoUrl)
                .profilePhotoUrl(photoUrl)
                .build();
    }
}
