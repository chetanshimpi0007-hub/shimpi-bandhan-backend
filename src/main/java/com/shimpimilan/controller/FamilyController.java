package com.shimpimilan.controller;

import com.shimpimilan.model.FamilyMember;
import com.shimpimilan.repository.FamilyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyMemberRepository familyMemberRepository;

    @GetMapping("/members/{profileId}")
    public ResponseEntity<List<FamilyMember>> getFamilyMembers(@PathVariable Long profileId) {
        return ResponseEntity.ok(familyMemberRepository.findByProfileId(profileId));
    }

    @PostMapping("/members/{profileId}")
    public ResponseEntity<FamilyMember> addFamilyMember(@PathVariable Long profileId, @RequestBody FamilyMember familyMember) {
        // Logic to add a member (normally requires validation and linking to existing FamilyAccount)
        return ResponseEntity.ok(familyMemberRepository.save(familyMember));
    }

    @DeleteMapping("/members/{memberId}")
    public ResponseEntity<Void> removeFamilyMember(@PathVariable Long memberId) {
        familyMemberRepository.deleteById(memberId);
        return ResponseEntity.ok().build();
    }
}
