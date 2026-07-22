package com.shimpimilan.controller.business;

import com.shimpimilan.dto.business.EnquiryCreateDto;
import com.shimpimilan.model.User;
import com.shimpimilan.model.business.BusinessEnquiry;
import com.shimpimilan.service.business.BusinessEnquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/businesses")
@RequiredArgsConstructor
public class PublicBusinessEnquiryController {

    private final BusinessEnquiryService enquiryService;

    @PostMapping("/{id}/enquire")
    public ResponseEntity<BusinessEnquiry> submitEnquiry(
            @PathVariable Long id,
            @RequestBody EnquiryCreateDto dto,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        BusinessEnquiry enquiry = enquiryService.createEnquiry(user.getId(), id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(enquiry);
    }
}
