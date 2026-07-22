package com.shimpimilan.dto.report;

import com.shimpimilan.model.ExportFormat;
import com.shimpimilan.model.ExportJobStatus;
import com.shimpimilan.model.ReportType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobDTO {
    private Long id;
    private String jobUuid;
    private ReportType reportType;
    private ExportFormat format;
    private ExportJobStatus status;
    private String generatedByName;
    private String fileName;
    private Long fileSizeBytes;
    private String filtersApplied;
    private String errorDetails;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
    private String downloadUrl;
}
