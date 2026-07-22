package com.shimpimilan.controller.business;

import com.shimpimilan.dto.business.BusinessPaymentRequest;
import com.shimpimilan.dto.business.BusinessPaymentVerifyRequest;
import com.shimpimilan.model.User;
import com.shimpimilan.service.business.BusinessPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/business/{id}/payment")
@RequiredArgsConstructor
public class BusinessPaymentController {

    private final BusinessPaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BusinessPaymentRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(id, user.getId(), request));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BusinessPaymentVerifyRequest request) {
        paymentService.verifyAndActivateSubscription(id, user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
