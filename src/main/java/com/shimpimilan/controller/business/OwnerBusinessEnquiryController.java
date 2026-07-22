package com.shimpimilan.controller.business;

import com.shimpimilan.dto.business.FollowUpCreateDto;
import com.shimpimilan.dto.business.MeetingCreateDto;
import com.shimpimilan.model.User;
import com.shimpimilan.model.business.BusinessEnquiry;
import com.shimpimilan.service.business.BusinessEnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business/owner/enquiries")
@RequiredArgsConstructor
public class OwnerBusinessEnquiryController {

    private final BusinessEnquiryService enquiryService;

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<BusinessEnquiry>> getBusinessEnquiries(
            @PathVariable Long businessId,
            @AuthenticationPrincipal User user) {
        // Here we could add authorization checks to ensure the user is the owner of the business.
        return ResponseEntity.ok(enquiryService.getEnquiriesByBusiness(businessId));
    }

    @PutMapping("/{enquiryId}/status")
    public ResponseEntity<BusinessEnquiry> updateStatus(
            @PathVariable Long enquiryId,
            @RequestParam String status,
            @RequestParam(required = false) String note,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(enquiryService.updateStatus(enquiryId, status, user.getId(), note));
    }

    @PostMapping("/{enquiryId}/notes")
    public ResponseEntity<Void> addNote(
            @PathVariable Long enquiryId,
            @RequestBody String content,
            @AuthenticationPrincipal User user) {
        enquiryService.addNote(enquiryId, user.getId(), content);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{enquiryId}/meetings")
    public ResponseEntity<Void> addMeeting(
            @PathVariable Long enquiryId,
            @RequestBody MeetingCreateDto dto,
            @AuthenticationPrincipal User user) {
        enquiryService.addMeeting(enquiryId, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{enquiryId}/followups")
    public ResponseEntity<Void> addFollowUp(
            @PathVariable Long enquiryId,
            @RequestBody FollowUpCreateDto dto,
            @AuthenticationPrincipal User user) {
        enquiryService.addFollowUp(enquiryId, dto);
        return ResponseEntity.ok().build();
    }
}
