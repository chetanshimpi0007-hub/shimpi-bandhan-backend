package com.shimpimilan.controller.business;

import com.shimpimilan.model.User;
import com.shimpimilan.model.business.BusinessEnquiry;
import com.shimpimilan.service.business.BusinessEnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/enquiries")
@RequiredArgsConstructor
public class UserBusinessEnquiryController {

    private final BusinessEnquiryService enquiryService;

    @GetMapping
    public ResponseEntity<List<BusinessEnquiry>> getMyEnquiries(
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(enquiryService.getEnquiriesByUser(user.getId()));
    }
}
