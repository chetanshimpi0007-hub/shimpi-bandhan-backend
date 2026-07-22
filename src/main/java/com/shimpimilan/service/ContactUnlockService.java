package com.shimpimilan.service;

import com.shimpimilan.dto.ProfileResponse;
import com.shimpimilan.model.InterestStatus;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.Role;
import com.shimpimilan.model.User;
import com.shimpimilan.model.UserStatus;
import com.shimpimilan.repository.ChatRoomRepository;
import com.shimpimilan.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactUnlockService {

    private final ChatRoomRepository chatRoomRepository;
    private final InterestRepository interestRepository;

    public void applyMasking(ProfileResponse response, Profile targetProfile, User requestingUser) {
        User targetUser = targetProfile.getUser();
        boolean isOwnProfile = requestingUser.getId().equals(targetUser.getId());
        boolean isAdmin = requestingUser.getRole() == Role.ADMIN;
        boolean isUnlocked = isOwnProfile || isAdmin || isContactUnlocked(requestingUser, targetUser);

        if (isUnlocked) {
            response.setContactNumber(targetUser.getPhone());
            response.setWhatsappNumber(targetProfile.getWhatsappNumber());
            response.setEmail(targetProfile.getEmail());
            response.setExactAddress(targetProfile.getExactAddress());
            response.setFamilyContact(targetProfile.getFamilyContact());
        } else {
            response.setContactNumber("LOCKED");
            response.setWhatsappNumber("LOCKED");
            response.setEmail("LOCKED");
            response.setExactAddress("LOCKED");
            response.setFamilyContact("LOCKED");
        }
    }

    private boolean isContactUnlocked(User user1, User user2) {
        // Rule: Groom & Bride have active premium (premiumExpiryDate > NOW), Interest is ACCEPTED, ChatRoom exists, neither user is BLOCKED or SUSPENDED.
        if (user1.getStatus() == UserStatus.BLOCKED || user1.getStatus() == UserStatus.SUSPENDED ||
            user2.getStatus() == UserStatus.BLOCKED || user2.getStatus() == UserStatus.SUSPENDED) {
            return false;
        }

        Profile p1 = user1.getProfile();
        Profile p2 = user2.getProfile();

        if (p1 == null || p2 == null) {
            return false;
        }

        boolean p1Premium = p1.getPremiumExpiryDate() != null && p1.getPremiumExpiryDate().isAfter(LocalDateTime.now());
        boolean p2Premium = p2.getPremiumExpiryDate() != null && p2.getPremiumExpiryDate().isAfter(LocalDateTime.now());

        if (!p1Premium || !p2Premium) {
            return false;
        }

        // Determine who is groom and who is bride (or just check existence in ChatRoom)
        boolean hasChatRoom = chatRoomRepository.existsByGroomAndBride(user1, user2) || 
                              chatRoomRepository.existsByGroomAndBride(user2, user1);

        if (!hasChatRoom) {
            return false;
        }

        // Check if Interest is ACCEPTED
        boolean interestAccepted = interestRepository.findBySenderIdAndReceiverId(user1.getId(), user2.getId())
                .map(i -> i.getStatus() == InterestStatus.ACCEPTED).orElse(false) ||
                interestRepository.findBySenderIdAndReceiverId(user2.getId(), user1.getId())
                .map(i -> i.getStatus() == InterestStatus.ACCEPTED).orElse(false);

        return interestAccepted;
    }
}
