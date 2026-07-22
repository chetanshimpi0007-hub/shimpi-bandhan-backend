package com.shimpimilan.controller;

import com.shimpimilan.model.FamilyDiscussionMember;
import com.shimpimilan.model.FamilyDiscussionRoom;
import com.shimpimilan.service.FamilyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family-rooms")
@RequiredArgsConstructor
public class FamilyRoomController {

    private final FamilyRoomService familyRoomService;

    @GetMapping("/{roomId}")
    public ResponseEntity<FamilyDiscussionRoom> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(familyRoomService.getRoom(roomId));
    }

    @GetMapping("/{roomId}/members")
    public ResponseEntity<List<FamilyDiscussionMember>> getMembers(@PathVariable Long roomId) {
        return ResponseEntity.ok(familyRoomService.getRoomMembers(roomId));
    }

    @PostMapping("/{roomId}/members")
    public ResponseEntity<FamilyDiscussionMember> addMember(
            @PathVariable Long roomId,
            @RequestParam Long userId,
            @RequestParam String role) {
        return ResponseEntity.ok(familyRoomService.addMember(roomId, userId, role));
    }

    @DeleteMapping("/{roomId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long roomId, @PathVariable Long userId) {
        familyRoomService.removeMember(roomId, userId);
        return ResponseEntity.ok().build();
    }
}
