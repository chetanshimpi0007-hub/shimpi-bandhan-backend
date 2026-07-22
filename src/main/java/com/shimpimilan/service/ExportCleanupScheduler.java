package com.shimpimilan.service;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.model.ExportJob;
import com.shimpimilan.model.ExportJobStatus;
import com.shimpimilan.repository.AuditLogRepository;
import com.shimpimilan.repository.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Nightly cleanup job that:
 *  1. Finds all ExportJob records that have passed their expiresAt timestamp.
 *  2. Deletes the physical file from disk.
 *  3. Marks the job as EXPIRED in the database.
 *  4. Writes an audit log entry for every file deleted.
 *
 * Runs daily at 02:00 AM server time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportCleanupScheduler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ExportJobRepository exportJobRepository;
    private final AuditLogRepository auditLogRepository;

    @Scheduled(cron = "0 0 2 * * *")   // Every day at 02:00 AM
    @Transactional
    public void cleanupExpiredExports() {
        log.info("Export cleanup scheduler started at {}", LocalDateTime.now().format(FMT));
        int deleted = 0;
        int failed  = 0;

        List<ExportJob> expiredJobs = exportJobRepository
                .findExpiredJobs(LocalDateTime.now(), ExportJobStatus.EXPIRED);

        log.info("Found {} expired export jobs to clean up", expiredJobs.size());

        for (ExportJob job : expiredJobs) {
            try {
                // Delete physical file
                if (job.getFilePath() != null) {
                    boolean fileDeleted = Files.deleteIfExists(Paths.get(job.getFilePath()));
                    if (fileDeleted) {
                        log.info("Deleted export file: {}", job.getFilePath());
                    } else {
                        log.warn("Export file not found on disk (already deleted?): {}", job.getFilePath());
                    }
                }

                // Mark as EXPIRED
                job.setStatus(ExportJobStatus.EXPIRED);
                exportJobRepository.save(job);

                // Audit log
                auditLogRepository.save(AuditLog.builder()
                        .module("EXPORT")
                        .action("EXPORT_FILE_EXPIRED_DELETED")
                        .adminName("SYSTEM_SCHEDULER")
                        .details(String.format("Export job UUID=%s, Report=%s, Format=%s, File=%s, Size=%d bytes – deleted by nightly cleanup",
                                job.getJobUuid(), job.getReportType(), job.getFormat(),
                                job.getFileName(), job.getFileSizeBytes() != null ? job.getFileSizeBytes() : 0L))
                        .build());

                deleted++;
            } catch (Exception e) {
                log.error("Failed to clean up export job {}: {}", job.getJobUuid(), e.getMessage());
                failed++;
            }
        }

        log.info("Export cleanup finished. Deleted: {}, Failed: {}", deleted, failed);

        // Summary audit log
        if (deleted > 0 || failed > 0) {
            auditLogRepository.save(AuditLog.builder()
                    .module("EXPORT")
                    .action("EXPORT_CLEANUP_RUN")
                    .adminName("SYSTEM_SCHEDULER")
                    .details(String.format("Nightly export cleanup at %s: %d files deleted, %d failed",
                            LocalDateTime.now().format(FMT), deleted, failed))
                    .build());
        }
    }
}
