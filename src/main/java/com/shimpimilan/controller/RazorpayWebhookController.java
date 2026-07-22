package com.shimpimilan.controller;

import com.razorpay.Utils;
import com.shimpimilan.model.Payment;
import com.shimpimilan.model.PaymentStatus;
import com.shimpimilan.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/razorpay")
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final PaymentRepository paymentRepository;

    @Value("${razorpay.webhook.secret:placeholder_webhook_secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        
        try {
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            if (!isValid) {
                return ResponseEntity.status(400).body("Invalid signature");
            }

            JSONObject jsonPayload = new JSONObject(payload);
            String event = jsonPayload.getString("event");
            JSONObject paymentEntity = jsonPayload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
            String orderId = paymentEntity.getString("order_id");
            String paymentId = paymentEntity.getString("id");

            Payment payment = paymentRepository.findByRazorpayOrderId(orderId).orElse(null);
            
            if (payment != null) {
                if ("payment.captured".equals(event)) {
                    // For safety, the primary verification happens in verify-payment API
                    // Webhook serves as a fallback or status updater
                    if (payment.getStatus() != PaymentStatus.CAPTURED) {
                        // Note: Real activation logic (like allocating 30 days) shouldn't be fully automated here alone
                        // without calling the MembershipService, but we'll mark the payment success.
                        payment.setStatus(PaymentStatus.CAPTURED);
                        payment.setRazorpayPaymentId(paymentId);
                        paymentRepository.save(payment);
                    }
                } else if ("payment.failed".equals(event)) {
                    payment.setStatus(PaymentStatus.FAILED);
                    payment.setRazorpayPaymentId(paymentId);
                    paymentRepository.save(payment);
                }
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            System.err.println("Webhook processing failed: " + e.getMessage());
            return ResponseEntity.status(500).body("Webhook processing failed");
        }
    }
}
