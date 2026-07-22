package com.shimpimilan.service;

import com.shimpimilan.model.Interest;
import com.shimpimilan.model.InterestStatus;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.InterestRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final UserRepository userRepository;
    private final com.shimpimilan.repository.ChatRoomRepository chatRoomRepository;
    private final com.shimpimilan.repository.FamilyDiscussionRoomRepository familyRoomRepository;
    private final com.shimpimilan.repository.FamilyDiscussionMemberRepository familyMemberRepository;
    private final com.shimpimilan.repository.FamilyTimelineRepository timelineRepository;
    private final AuditLogService auditLogService;

    public Interest sendInterest(User sender, Long receiverId) {
        if (sender.getId().equals(receiverId)) {
            throw new RuntimeException("You cannot send interest to yourself");
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Optional<Interest> existingInterest = interestRepository.findBySenderIdAndReceiverId(sender.getId(), receiverId);
        if (existingInterest.isPresent()) {
            Interest interest = existingInterest.get();
            if (interest.getStatus() == InterestStatus.PENDING || interest.getStatus() == InterestStatus.ACCEPTED) {
                throw new RuntimeException("An active interest already exists");
            }
            // If it was cancelled or rejected, maybe they can resend? Let's assume they can resend if CANCELLED.
            interest.setStatus(InterestStatus.PENDING);
            return interestRepository.save(interest);
        }

        Interest newInterest = Interest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(InterestStatus.PENDING)
                .build();
        Interest saved = interestRepository.save(newInterest);
        
        auditLogService.logAction("INTEREST_SENT", sender.getId(), receiver.getId(), "Interest sent");
        
        return saved;
    }

    public Interest updateInterestStatus(User user, Long interestId, InterestStatus newStatus) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new RuntimeException("Interest not found"));

        // Only receiver can ACCEPT or REJECT
        if ((newStatus == InterestStatus.ACCEPTED || newStatus == InterestStatus.REJECTED)) {
            if (!interest.getReceiver().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to update this interest");
            }
        }
        // Only sender can CANCEL
        if (newStatus == InterestStatus.CANCELLED) {
            if (!interest.getSender().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to cancel this interest");
            }
        }

        interest.setStatus(newStatus);
        
        auditLogService.logAction("INTEREST_" + newStatus.name(), user.getId(), interest.getId(), "Interest status updated to " + newStatus.name());
        
        if (newStatus == InterestStatus.ACCEPTED) {
            User sender = interest.getSender();
            User receiver = interest.getReceiver();
            
            if (!chatRoomRepository.existsByGroomAndBride(sender, receiver) &&
                !chatRoomRepository.existsByGroomAndBride(receiver, sender)) {
                
                User groom = null;
                User bride = null;
                
                if (sender.getProfile() != null && sender.getProfile().getGender() == com.shimpimilan.model.Gender.MALE) {
                    groom = sender;
                    bride = receiver;
                } else if (sender.getProfile() != null && sender.getProfile().getGender() == com.shimpimilan.model.Gender.FEMALE) {
                    bride = sender;
                    groom = receiver;
                } else {
                    groom = sender;
                    bride = receiver;
                }
                
                com.shimpimilan.model.ChatRoom chatRoom = com.shimpimilan.model.ChatRoom.builder()
                        .groom(groom)
                        .bride(bride)
                        .build();
                chatRoom = chatRoomRepository.save(chatRoom);
                
                // Create Family Discussion Room
                com.shimpimilan.model.FamilyDiscussionRoom familyRoom = com.shimpimilan.model.FamilyDiscussionRoom.builder()
                        .chatRoom(chatRoom)
                        .isActive(true)
                        .build();
                familyRoom = familyRoomRepository.save(familyRoom);
                
                // Add Groom to Family Room
                com.shimpimilan.model.FamilyDiscussionMember groomMember = com.shimpimilan.model.FamilyDiscussionMember.builder()
                        .room(familyRoom)
                        .user(groom)
                        .familyRole("Groom")
                        .build();
                familyMemberRepository.save(groomMember);
                
                // Add Bride to Family Room
                com.shimpimilan.model.FamilyDiscussionMember brideMember = com.shimpimilan.model.FamilyDiscussionMember.builder()
                        .room(familyRoom)
                        .user(bride)
                        .familyRole("Bride")
                        .build();
                familyMemberRepository.save(brideMember);
                
                // Create Timeline Event
                com.shimpimilan.model.FamilyTimeline timelineEvent = com.shimpimilan.model.FamilyTimeline.builder()
                        .room(familyRoom)
                        .eventType(com.shimpimilan.model.FamilyTimelineEventType.ROOM_CREATED)
                        .description("Family Discussion Room created.")
                        .build();
                timelineRepository.save(timelineEvent);
            }
        }
        
        return interestRepository.save(interest);
    }

    public List<Interest> getReceivedInterests(User user) {
        return interestRepository.findByReceiverId(user.getId());
    }

    public List<Interest> getSentInterests(User user) {
        return interestRepository.findBySenderId(user.getId());
    }

    public boolean isMutualInterestEstablished(Long userId1, Long userId2) {
        return interestRepository.existsMutualInterest(userId1, userId2, InterestStatus.ACCEPTED);
    }
}
