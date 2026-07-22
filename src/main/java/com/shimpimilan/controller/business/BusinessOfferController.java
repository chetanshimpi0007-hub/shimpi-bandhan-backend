package com.shimpimilan.controller.business;

import com.shimpimilan.dto.business.BusinessOfferRequest;
import com.shimpimilan.model.User;
import com.shimpimilan.service.business.BusinessOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business/{id}/offers")
@RequiredArgsConstructor
public class BusinessOfferController {

    private final BusinessOfferService offerService;

    @PostMapping
    public ResponseEntity<?> createOffer(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BusinessOfferRequest request) {
        return ResponseEntity.ok(offerService.createOffer(id, user.getId(), request));
    }

    @PutMapping("/{offerId}")
    public ResponseEntity<?> updateOffer(
            @PathVariable Long id,
            @PathVariable Long offerId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BusinessOfferRequest request) {
        return ResponseEntity.ok(offerService.updateOffer(id, user.getId(), offerId, request));
    }

    @DeleteMapping("/{offerId}")
    public ResponseEntity<?> deleteOffer(
            @PathVariable Long id,
            @PathVariable Long offerId,
            @AuthenticationPrincipal User user) {
        offerService.deleteOffer(id, user.getId(), offerId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{offerId}/status")
    public ResponseEntity<?> toggleOfferStatus(
            @PathVariable Long id,
            @PathVariable Long offerId,
            @RequestParam boolean active,
            @AuthenticationPrincipal User user) {
        offerService.toggleOfferStatus(id, user.getId(), offerId, active);
        return ResponseEntity.ok().build();
    }
}
