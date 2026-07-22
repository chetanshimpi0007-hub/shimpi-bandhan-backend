package com.shimpimilan.service;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void logAction(Long userId, String action, String module, String ipAddress, 
                          String browser, String device, String oldValue, String newValue, String adminName) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .module(module)
                .ipAddress(ipAddress)
                .browser(browser)
                .device(device)
                .oldValue(oldValue)
                .newValue(newValue)
                .adminName(adminName)
                .build();
        auditLogRepository.save(auditLog);
    }

    public void logAction(String action, Long userId, Long targetId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .userId(userId)
                .action(action)
                .targetId(targetId)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }
}
