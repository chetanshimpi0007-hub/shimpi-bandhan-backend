package com.shimpimilan.controller;

import com.shimpimilan.dto.PaymentVerificationRequest;
import com.shimpimilan.model.Payment;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/premium")
@RequiredArgsConstructor
public class PremiumController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping("/create-order")
    public ResponseEntity<?> createPremiumOrder(
            Authentication authentication,
            @RequestParam(defaultValue = "false") boolean useReferralDiscount) {
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = paymentService.createPremiumOrder(user, useReferralDiscount);

        Map<String, Object> response = new HashMap<>();
        response.put("razorpayOrderId", payment.getRazorpayOrderId());
        response.put("amount", payment.getAmount());
        response.put("currency", "INR");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(
            Authentication authentication,
            @RequestBody PaymentVerificationRequest request) {
        
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            paymentService.verifyAndActivatePremium(
                    user,
                    request.getRazorpayOrderId(),
                    request.getRazorpayPaymentId(),
                    request.getRazorpaySignature()
            );

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Premium activated successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "failed");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication) {
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // For simplicity, we expose the entity. In production, use a DTO.
        return ResponseEntity.ok(paymentService.getPaymentHistory(user));
    }

    @GetMapping("/invoice/{paymentId}")
    public ResponseEntity<?> getInvoice(Authentication authentication, @PathVariable String paymentId) {
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Payment payment = paymentService.getPaymentByRazorpayId(paymentId);
        
        if (!payment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Access denied to this invoice.");
        }

        Map<String, Object> invoice = new HashMap<>();
        invoice.put("invoiceId", "INV-" + payment.getId());
        invoice.put("userName", user.getPhone());
        invoice.put("membershipPlan", "PREMIUM");
        invoice.put("membershipDuration", "30 Days");
        invoice.put("baseAmount", payment.getAmount());
        invoice.put("discount", payment.getDiscountApplied());
        invoice.put("finalPaidAmount", payment.getFinalAmountPaid());
        invoice.put("paymentDate", payment.getCreatedAt());
        invoice.put("razorpayPaymentId", payment.getRazorpayPaymentId());
        invoice.put("invoiceGenerationDate", java.time.LocalDateTime.now());
        invoice.put("status", payment.getStatus());
        invoice.put("billedTo", user.getPhone());
        
        return ResponseEntity.ok(invoice);
    }
}
