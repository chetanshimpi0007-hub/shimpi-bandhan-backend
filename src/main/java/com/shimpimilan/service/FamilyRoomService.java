package com.shimpimilan.service;

import com.shimpimilan.model.FamilyDiscussionMember;
import com.shimpimilan.model.FamilyDiscussionRoom;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.FamilyDiscussionMemberRepository;
import com.shimpimilan.repository.FamilyDiscussionRoomRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilyRoomService {

    private final FamilyDiscussionRoomRepository roomRepository;
    private final FamilyDiscussionMemberRepository memberRepository;
    private final UserRepository userRepository;

    public FamilyDiscussionRoom getRoom(Long roomId) {
        return roomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Room not found"));
    }

    public List<FamilyDiscussionMember> getRoomMembers(Long roomId) {
        return memberRepository.findByRoomId(roomId);
    }

    public FamilyDiscussionMember addMember(Long roomId, Long userId, String familyRole) {
        FamilyDiscussionRoom room = getRoom(roomId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        if (memberRepository.existsByRoomIdAndUserId(roomId, userId)) {
            throw new RuntimeException("Member already in room");
        }

        // Check 12 member limit
        if (memberRepository.findByRoomId(roomId).size() >= 12) {
            throw new RuntimeException("Room has reached the maximum of 12 members");
        }

        FamilyDiscussionMember member = FamilyDiscussionMember.builder()
                .room(room)
                .user(user)
                .familyRole(familyRole)
                .build();
        return memberRepository.save(member);
    }

    public void removeMember(Long roomId, Long userId) {
        memberRepository.deleteByRoomIdAndUserId(roomId, userId);
    }
}
