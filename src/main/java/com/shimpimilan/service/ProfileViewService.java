package com.shimpimilan.service;

import com.shimpimilan.dto.ProfileResponse;
import com.shimpimilan.model.ProfileView;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.ProfileViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProfileViewService {

    private final ProfileViewRepository profileViewRepository;
    private final ProfileService profileService;

    public void recordView(User viewer, User viewedUser, String device) {
        if (viewer.getId().equals(viewedUser.getId())) return; // Ignore self views

        // Optionally, check if a view already exists today to avoid spamming
        // For now, let's just log every view or update the timestamp if they already viewed
        ProfileView view = profileViewRepository.findByViewerIdAndViewedUserId(viewer.getId(), viewedUser.getId())
                .orElse(ProfileView.builder()
                        .viewer(viewer)
                        .viewedUser(viewedUser)
                        .build());
        
        view.setDevice(device);
        view.setViewedAt(LocalDateTime.now());
        profileViewRepository.save(view);
    }

    public Page<ProfileResponse> getProfileVisitors(User requestingUser, Pageable pageable) {
        // Return who viewed the requestingUser
        Page<ProfileView> views = profileViewRepository.findByViewedUserIdOrderByViewedAtDesc(requestingUser.getId(), pageable);
        
        // Map to ProfileResponse and handle blurring for free users
        return views.map(view -> {
            ProfileResponse response = profileService.getProfileByUserId(view.getViewer().getId(), requestingUser);
            // Additional check: Free users see a restricted list
            boolean isPremium = requestingUser.getProfile() != null && Boolean.TRUE.equals(requestingUser.getProfile().getIsPremiumMember());
            if (!isPremium) {
                // Blur data
                response.setFullName("Hidden User");
                response.setProfilePhotoUrl(null);
                response.setCity("Hidden");
                response.setAge(null);
            }
            return response;
        });
    }
}
