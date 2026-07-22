package com.shimpimilan.service;

import com.shimpimilan.model.FamilyDiscussionRoom;
import com.shimpimilan.model.FamilyMeeting;
import com.shimpimilan.model.FamilyTimeline;
import com.shimpimilan.model.FamilyTimelineEventType;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.FamilyDiscussionRoomRepository;
import com.shimpimilan.repository.FamilyMeetingRepository;
import com.shimpimilan.repository.FamilyTimelineRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyMeetingService {

    private final FamilyMeetingRepository meetingRepository;
    private final FamilyDiscussionRoomRepository roomRepository;
    private final FamilyTimelineRepository timelineRepository;
    private final UserRepository userRepository;

    public List<FamilyMeeting> getMeetings(Long roomId) {
        return meetingRepository.findByRoomId(roomId);
    }

    public FamilyMeeting createMeeting(Long roomId, Long createdById, FamilyMeeting meetingDetails) {
        FamilyDiscussionRoom room = roomRepository.findById(roomId).orElseThrow();
        User creator = userRepository.findById(createdById).orElseThrow();

        meetingDetails.setRoom(room);
        meetingDetails.setCreatedBy(creator);
        FamilyMeeting saved = meetingRepository.save(meetingDetails);

        FamilyTimeline timelineEvent = FamilyTimeline.builder()
                .room(room)
                .eventType(FamilyTimelineEventType.MEETING_SCHEDULED)
                .description("Meeting Scheduled: " + meetingDetails.getTitle())
                .build();
        timelineRepository.save(timelineEvent);

        return saved;
    }
}
