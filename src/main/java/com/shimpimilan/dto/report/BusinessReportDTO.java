package com.shimpimilan.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessReportDTO {
    private Long totalBusinesses;
    private Map<String, Long> statusWise;
    private Map<String, Long> planWise;
    private Long expiredPlans;
}
