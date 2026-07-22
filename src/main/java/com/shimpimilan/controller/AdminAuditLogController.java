package com.shimpimilan.controller;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Specification<AuditLog> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (module != null && !module.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.upper(root.get("module")), module.toUpperCase()));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.upper(root.get("action")), "%" + action.toUpperCase() + "%"));
        }

        return ResponseEntity.ok(auditLogRepository.findAll(spec, pageable));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAuditStats() {
        long total = auditLogRepository.count();
        return ResponseEntity.ok(Map.of("totalAuditLogs", total));
    }

    @GetMapping("/modules")
    public ResponseEntity<java.util.List<String>> getDistinctModules() {
        return ResponseEntity.ok(java.util.List.of(
                "USER_MANAGEMENT", "CHAT_MODERATION", "PAYMENT_MANAGEMENT",
                "PHOTO_MODERATION", "BUSINESS_MANAGEMENT", "PROFILE_VERIFICATION",
                "PLATFORM_SETTINGS", "AUDIT"
        ));
    }
}
