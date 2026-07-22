package com.shimpimilan.controller;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.model.Payment;
import com.shimpimilan.model.PaymentStatus;
import com.shimpimilan.repository.AuditLogRepository;
import com.shimpimilan.repository.PaymentRepository;
import com.shimpimilan.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentRepository paymentRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<Page<Payment>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {

        PaymentStatus tempStatus = null;
        try { if (status != null) tempStatus = PaymentStatus.valueOf(status); } catch (Exception ignored) {}

        final PaymentStatus statusEnum = tempStatus;

        Page<Payment> payments = (statusEnum != null)
                ? paymentRepository.findAll(
                    (root, query, cb) -> cb.equal(root.get("status"), statusEnum),
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                : paymentRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/failed")
    public ResponseEntity<Page<Payment>> getFailedPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Payment> payments = paymentRepository.findAll(
                (root, query, cb) -> cb.equal(root.get("status"), PaymentStatus.FAILED),
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getRevenueSummary() {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();

        Double total = paymentRepository.sumFinalAmountPaidByStatus(PaymentStatus.CAPTURED);
        Double monthly = paymentRepository.sumFinalAmountPaidByStatusAndCreatedAtAfter(PaymentStatus.CAPTURED, monthStart);
        Double todayRev = paymentRepository.sumFinalAmountPaidByStatusAndCreatedAtAfter(PaymentStatus.CAPTURED, today);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalRevenue", total != null ? total : 0.0);
        summary.put("monthlyRevenue", monthly != null ? monthly : 0.0);
        summary.put("todayRevenue", todayRev != null ? todayRev : 0.0);
        summary.put("totalCaptured", paymentRepository.countByStatus(PaymentStatus.CAPTURED));
        summary.put("totalFailed", paymentRepository.countByStatus(PaymentStatus.FAILED));
        summary.put("totalCreated", paymentRepository.countByStatus(PaymentStatus.CREATED));
        summary.put("totalPayments", paymentRepository.count());
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<String> markRefund(
            @PathVariable Long paymentId,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails adminDetails) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (payment.getStatus() != PaymentStatus.CAPTURED) {
            return ResponseEntity.badRequest().body("Only captured payments can be refunded");
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        
        auditLogRepository.save(AuditLog.builder()
                .userId(payment.getUser().getId())
                .action("REFUND_PAYMENT")
                .module("PAYMENT")
                .oldValue("CAPTURED")
                .newValue("REFUNDED")
                .details("Reason: " + reason)
                .adminName(adminDetails.getUser().getPhone())
                .timestamp(LocalDateTime.now())
                .build());
        
        return ResponseEntity.ok("Payment marked as refunded. Process manual refund via Razorpay dashboard.");
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long paymentId) {
        return paymentRepository.findById(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

