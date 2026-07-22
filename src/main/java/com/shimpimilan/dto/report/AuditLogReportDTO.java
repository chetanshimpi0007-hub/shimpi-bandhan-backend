package com.shimpimilan.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import com.shimpimilan.model.AuditLog;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogReportDTO {
    private Long totalLogs;
    private Map<String, Long> moduleWise;
    private Long actionsToday;
    private Page<AuditLog> logs;
}
