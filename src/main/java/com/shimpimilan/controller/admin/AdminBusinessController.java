package com.shimpimilan.controller.admin;

import com.shimpimilan.dto.business.BusinessAnalyticsResponse;
import com.shimpimilan.model.business.BusinessStatus;
import com.shimpimilan.service.business.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/business")
@RequiredArgsConstructor
public class AdminBusinessController {

    private final BusinessService businessService;

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateBusinessStatus(
            @PathVariable Long id,
            @RequestParam BusinessStatus status) {
        businessService.changeBusinessStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/analytics")
    public ResponseEntity<BusinessAnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(businessService.getAnalytics());
    }

    @PutMapping("/{id}/feature")
    public ResponseEntity<?> toggleAdminFeatured(
            @PathVariable Long id,
            @RequestParam boolean featured) {
        businessService.toggleAdminFeatured(id, featured);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<?> setPriorityOverride(
            @PathVariable Long id,
            @RequestParam int priority) {
        businessService.setPriorityOverride(id, priority);
        return ResponseEntity.ok().build();
    }
}
