package com.shimpimilan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shimpimilan.model.*;
import com.shimpimilan.model.notification.InAppNotification;
import com.shimpimilan.repository.AuditLogRepository;
import com.shimpimilan.repository.ExportJobRepository;
import com.shimpimilan.repository.notification.InAppNotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Async worker that generates the actual export file in a background thread.
 * Called by AdminExportService after the ExportJob entity is persisted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportWorkerService {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm");

    @Value("${app.export.base-dir:./exports}")
    private String baseExportDir;

    private final ExportJobRepository exportJobRepository;
    private final ExcelExportService excelExportService;
    private final CsvExportService csvExportService;
    private final PdfExportService pdfExportService;
    private final InAppNotificationRepository inAppNotificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Async("exportTaskExecutor")
    @Transactional
    public void processExportJob(Long jobId, Map<String, String> filters) {
        ExportJob job = exportJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("ExportJob not found: " + jobId));

        log.info("Export worker starting: jobId={}, type={}, format={}",
                jobId, job.getReportType(), job.getFormat());

        // Update status to PROCESSING
        job.setStatus(ExportJobStatus.PROCESSING);
        exportJobRepository.save(job);
        messagingTemplate.convertAndSend("/topic/admin/exports", job);

        try {
            String subDir = job.getFormat().name().toLowerCase();
            Path dirPath = Paths.get(baseExportDir, subDir);
            Files.createDirectories(dirPath);

            String filename = buildFilename(job);
            String filePath = dirPath.resolve(filename).toAbsolutePath().toString();

            // Dispatch to the correct format handler
            switch (job.getFormat()) {
                case EXCEL -> generateExcel(job, filePath, filters);
                case PDF   -> generatePdf(job, filePath, filters);
                case CSV   -> generateCsv(job, filePath, filters);
            }

            long fileSize = new File(filePath).length();

            // Mark COMPLETED
            job.setStatus(ExportJobStatus.COMPLETED);
            job.setFilePath(filePath);
            job.setFileName(filename);
            job.setFileSizeBytes(fileSize);
            job.setCompletedAt(LocalDateTime.now());
            job.setExpiresAt(LocalDateTime.now().plusHours(24));
            exportJobRepository.save(job);
            messagingTemplate.convertAndSend("/topic/admin/exports", job);

            // Send in-app notification to admin
            sendAdminNotification(job);

            // Audit log entry
            logAuditEntry(job, fileSize, null);

            log.info("Export job COMPLETED: jobId={}, file={}, size={}B", jobId, filename, fileSize);

        } catch (Exception ex) {
            log.error("Export job FAILED: jobId={}, error={}", jobId, ex.getMessage(), ex);
            job.setStatus(ExportJobStatus.FAILED);
            job.setErrorDetails(ex.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            exportJobRepository.save(job);
            messagingTemplate.convertAndSend("/topic/admin/exports", job);

            // Audit log for failure
            logAuditEntry(job, 0L, ex.getMessage());
        }
    }

    // ----------------------------------------------------------------
    //  Format dispatch
    // ----------------------------------------------------------------
    private void generateExcel(ExportJob job, String filePath, Map<String, String> filters) throws Exception {
        switch (job.getReportType()) {
            case USERS       -> excelExportService.exportUsersToFile(filePath, filters, job.getGeneratedByName());
            case PAYMENTS    -> excelExportService.exportPaymentsToFile(filePath, filters, job.getGeneratedByName());
            case BUSINESSES  -> excelExportService.exportBusinessesToFile(filePath, filters, job.getGeneratedByName());
            case AUDIT_LOGS  -> excelExportService.exportAuditLogsToFile(filePath, filters, job.getGeneratedByName());
            default -> throw new UnsupportedOperationException("Excel export not yet supported for: " + job.getReportType());
        }
    }

    private void generatePdf(ExportJob job, String filePath, Map<String, String> filters) throws Exception {
        switch (job.getReportType()) {
            case USERS       -> pdfExportService.exportUsersToPdf(filePath, filters, job.getGeneratedByName());
            case PAYMENTS    -> pdfExportService.exportPaymentsToPdf(filePath, filters, job.getGeneratedByName());
            case BUSINESSES  -> pdfExportService.exportBusinessesToPdf(filePath, filters, job.getGeneratedByName());
            case AUDIT_LOGS  -> pdfExportService.exportAuditLogsToPdf(filePath, filters, job.getGeneratedByName());
            default -> throw new UnsupportedOperationException("PDF export not yet supported for: " + job.getReportType());
        }
    }

    private void generateCsv(ExportJob job, String filePath, Map<String, String> filters) throws Exception {
        switch (job.getReportType()) {
            case USERS       -> csvExportService.exportUsersToFile(filePath, filters, job.getGeneratedByName());
            case PAYMENTS    -> csvExportService.exportPaymentsToFile(filePath, filters, job.getGeneratedByName());
            case BUSINESSES  -> csvExportService.exportBusinessesToFile(filePath, filters, job.getGeneratedByName());
            case AUDIT_LOGS  -> csvExportService.exportAuditLogsToFile(filePath, filters, job.getGeneratedByName());
            default -> throw new UnsupportedOperationException("CSV export not yet supported for: " + job.getReportType());
        }
    }

    // ----------------------------------------------------------------
    //  Helpers
    // ----------------------------------------------------------------
    private String buildFilename(ExportJob job) {
        String ts = LocalDateTime.now().format(TS_FMT);
        String ext = switch (job.getFormat()) {
            case EXCEL -> ".xlsx";
            case PDF   -> ".pdf";
            case CSV   -> ".csv";
        };
        return job.getReportType().name() + "_REPORT_" + ts + "_" + job.getJobUuid().substring(0, 8).toUpperCase() + ext;
    }

    private void sendAdminNotification(ExportJob job) {
        try {
            InAppNotification notif = InAppNotification.builder()
                    .userId(job.getGeneratedBy())
                    .title("Export Ready: " + job.getReportType().name() + " " + job.getFormat().name())
                    .message("Your " + job.getReportType().name() + " report (" + job.getFormat().name()
                            + ") is ready. File: " + job.getFileName()
                            + ". Completed at: " + job.getCompletedAt().format(TS_FMT)
                            + ". Download expires in 24 hours.")
                    .build();
            inAppNotificationRepository.save(notif);
        } catch (Exception e) {
            log.warn("Failed to send in-app notification for export job {}: {}", job.getId(), e.getMessage());
        }
    }

    private void logAuditEntry(ExportJob job, long fileSize, String error) {
        try {
            String details = String.format(
                    "Export %s | Format: %s | File: %s | Size: %d bytes | Filters: %s%s",
                    error == null ? "COMPLETED" : "FAILED",
                    job.getFormat(), job.getFileName(), fileSize,
                    job.getFiltersApplied(),
                    error != null ? " | Error: " + error : "");

            AuditLog log2 = AuditLog.builder()
                    .userId(job.getGeneratedBy())
                    .adminName(job.getGeneratedByName())
                    .module("EXPORT")
                    .action(job.getReportType().name() + "_" + job.getFormat().name() + "_EXPORT")
                    .details(details)
                    .ipAddress(job.getRequestIpAddress())
                    .browser(job.getUserAgent())
                    .build();
            auditLogRepository.save(log2);
        } catch (Exception e) {
            log.warn("Failed to log audit entry for export job {}: {}", job.getId(), e.getMessage());
        }
    }
}
