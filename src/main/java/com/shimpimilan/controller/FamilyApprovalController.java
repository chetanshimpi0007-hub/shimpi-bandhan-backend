package com.shimpimilan.controller;

import com.shimpimilan.model.ApprovalStatus;
import com.shimpimilan.model.FamilyApproval;
import com.shimpimilan.service.FamilyApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/family-approvals")
@RequiredArgsConstructor
public class FamilyApprovalController {

    private final FamilyApprovalService familyApprovalService;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<FamilyApproval>> getRoomApprovals(@PathVariable Long roomId) {
        return ResponseEntity.ok(familyApprovalService.getRoomApprovals(roomId));
    }

    @PostMapping("/{approvalId}/submit")
    public ResponseEntity<FamilyApproval> submitApproval(
            @PathVariable Long approvalId,
            @RequestParam Long userId,
            @RequestParam ApprovalStatus status,
            @RequestParam(required = false) String comment) {
        return ResponseEntity.ok(familyApprovalService.submitApproval(approvalId, userId, status, comment));
    }
}
