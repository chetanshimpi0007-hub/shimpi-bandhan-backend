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
public class EnquiryReportDTO {
    private Long totalEnquiries;
    private Map<String, Long> statusWise;
    private Double conversionRate;
}
