package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "export_jobs", indexes = {
    @Index(name = "idx_export_job_generated_by", columnList = "generatedBy"),
    @Index(name = "idx_export_job_status", columnList = "status"),
    @Index(name = "idx_export_job_requested_at", columnList = "requestedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36, unique = true)
    private String jobUuid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExportJobStatus status = ExportJobStatus.QUEUED;

    /** The admin user ID who requested this export */
    @Column(nullable = false)
    private Long generatedBy;

    /** Admin display name for history view */
    private String generatedByName;

    /** Absolute file path on disk (never exposed in API responses) */
    @Column(columnDefinition = "TEXT")
    private String filePath;

    /** Safe public filename, e.g. USER_REPORT_2026_07_10_UUID.xlsx */
    private String fileName;

    /** File size in bytes, populated on completion */
    private Long fileSizeBytes;

    /** JSON string of applied filters */
    @Column(columnDefinition = "TEXT")
    private String filtersApplied;

    /** Error message if status = FAILED */
    @Column(columnDefinition = "TEXT")
    private String errorDetails;

    /** Admin IP address for audit trail */
    private String requestIpAddress;

    /** Browser/device info for audit trail */
    private String userAgent;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime completedAt;

    /** Files older than 24 hours are marked EXPIRED and physically deleted */
    private LocalDateTime expiresAt;
}
