package com.shimpimilan.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDTO {
    private Double totalPremiumRevenue;
    private Double totalBusinessAdRevenue;
    private List<Map<String, Object>> monthlyRevenueTrend;
    private List<Map<String, Object>> paymentMethodBreakdown;
    private List<Map<String, Object>> yearlyRevenue;
}
