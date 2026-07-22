package com.shimpimilan.dto.report;

import com.shimpimilan.model.ExportFormat;
import com.shimpimilan.model.ReportType;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDTO {
    private ReportType reportType;
    private ExportFormat format;
    /** Optional filters: startDate, endDate, community, status, etc. */
    private Map<String, String> filters;
}
