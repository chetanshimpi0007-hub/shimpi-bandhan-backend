package com.shimpimilan.controller.business;

import com.shimpimilan.dto.business.BusinessResponse;
import com.shimpimilan.service.business.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/business")
@RequiredArgsConstructor
public class PublicBusinessController {

    private final BusinessService businessService;
    private final com.shimpimilan.service.business.BusinessInteractionService businessInteractionService;

    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> getBusinessDetails(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.getBusinessDetails(id));
    }
    
    @PostMapping("/search")
    public ResponseEntity<org.springframework.data.domain.Page<BusinessResponse>> searchBusinesses(
            @RequestBody com.shimpimilan.dto.business.BusinessSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(businessService.searchBusinesses(request, page, size));
    }

    // Since these can be public or authenticated depending on context, we will accept the user from SecurityContext if present
    @PostMapping("/{id}/review")
    public ResponseEntity<?> addReview(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.shimpimilan.model.User user,
            @jakarta.validation.Valid @RequestBody com.shimpimilan.dto.business.BusinessReviewRequest request) {
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        businessInteractionService.addReview(id, user.getId(), request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/lead")
    public ResponseEntity<?> trackLead(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.shimpimilan.model.User user,
            @jakarta.validation.Valid @RequestBody com.shimpimilan.dto.business.BusinessLeadRequest request) {
        Long userId = (user != null) ? user.getId() : null;
        businessInteractionService.trackLead(id, userId, request);
        return ResponseEntity.ok().build();
    }
}
