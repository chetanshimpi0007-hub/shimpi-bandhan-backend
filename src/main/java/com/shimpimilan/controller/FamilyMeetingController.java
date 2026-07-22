package com.shimpimilan.controller;

import com.shimpimilan.model.FamilyMeeting;
import com.shimpimilan.service.FamilyMeetingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family-meetings")
@RequiredArgsConstructor
public class FamilyMeetingController {

    private final FamilyMeetingService familyMeetingService;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<FamilyMeeting>> getMeetings(@PathVariable Long roomId) {
        return ResponseEntity.ok(familyMeetingService.getMeetings(roomId));
    }

    @PostMapping("/room/{roomId}")
    public ResponseEntity<FamilyMeeting> createMeeting(
            @PathVariable Long roomId,
            @RequestParam Long createdById,
            @RequestBody FamilyMeeting meeting) {
        return ResponseEntity.ok(familyMeetingService.createMeeting(roomId, createdById, meeting));
    }
}
