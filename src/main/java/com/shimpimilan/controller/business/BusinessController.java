package com.shimpimilan.controller.business;

import com.shimpimilan.dto.business.BusinessCategoryDTO;
import com.shimpimilan.dto.business.BusinessRegistrationRequest;
import com.shimpimilan.dto.business.BusinessResponse;
import com.shimpimilan.model.User;
import com.shimpimilan.service.business.BusinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping("/categories")
    public ResponseEntity<List<BusinessCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(businessService.getAllActiveCategories());
    }

    @GetMapping("/{id}/dashboard-stats")
    public ResponseEntity<com.shimpimilan.dto.business.BusinessDashboardDTO> getDashboardStats(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(businessService.getDashboardStats(id, user.getId()));
    }

    @PostMapping("/register")
    public ResponseEntity<BusinessResponse> registerBusiness(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BusinessRegistrationRequest request) {
        // user.getId() ensures that only the authenticated user can register their own business
        BusinessResponse response = businessService.registerBusiness(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
