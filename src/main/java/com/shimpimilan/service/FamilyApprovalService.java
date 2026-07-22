package com.shimpimilan.service;

import com.shimpimilan.model.ApprovalStatus;
import com.shimpimilan.model.FamilyApproval;
import com.shimpimilan.model.FamilyApprovalLog;
import com.shimpimilan.model.FamilyDiscussionRoom;
import com.shimpimilan.model.FamilyTimeline;
import com.shimpimilan.model.FamilyTimelineEventType;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.FamilyApprovalLogRepository;
import com.shimpimilan.repository.FamilyApprovalRepository;
import com.shimpimilan.repository.FamilyTimelineRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyApprovalService {

    private final FamilyApprovalRepository approvalRepository;
    private final FamilyApprovalLogRepository approvalLogRepository;
    private final FamilyTimelineRepository timelineRepository;
    private final UserRepository userRepository;

    public List<FamilyApproval> getRoomApprovals(Long roomId) {
        return approvalRepository.findByRoomId(roomId);
    }

    public FamilyApproval submitApproval(Long approvalId, Long userId, ApprovalStatus newStatus, String comment) {
        FamilyApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!approval.getMember().getUser().getId().equals(userId)) {
            // Check if admin override
            if (user.getRole() != com.shimpimilan.model.Role.ADMIN) {
                throw new RuntimeException("Unauthorized to change this approval");
            }
        }

        approval.setStatus(newStatus);
        approval.setComment(comment);
        FamilyApproval saved = approvalRepository.save(approval);

        FamilyApprovalLog log = FamilyApprovalLog.builder()
                .approval(saved)
                .action(newStatus.name())
                .actionBy(user)
                .comment(comment)
                .build();
        approvalLogRepository.save(log);

        checkWorkflowCompletion(saved.getRoom());

        return saved;
    }

    private void checkWorkflowCompletion(FamilyDiscussionRoom room) {
        List<FamilyApproval> approvals = approvalRepository.findByRoomId(room.getId());
        if (approvals.isEmpty()) return;

        boolean allApproved = approvals.stream().allMatch(a -> a.getStatus() == ApprovalStatus.APPROVED);
        boolean anyRejected = approvals.stream().anyMatch(a -> a.getStatus() == ApprovalStatus.REJECTED);

        if (allApproved) {
            FamilyTimeline timelineEvent = FamilyTimeline.builder()
                    .room(room)
                    .eventType(FamilyTimelineEventType.MARRIAGE_CONFIRMED)
                    .description("Family Approved Match \uD83C\uDF89")
                    .build();
            timelineRepository.save(timelineEvent);
        } else if (anyRejected) {
            FamilyTimeline timelineEvent = FamilyTimeline.builder()
                    .room(room)
                    .eventType(FamilyTimelineEventType.MARRIAGE_CONFIRMED) // Overloading or could add new enum
                    .description("Family Approval Rejected")
                    .build();
            timelineRepository.save(timelineEvent);
        }
    }
}
