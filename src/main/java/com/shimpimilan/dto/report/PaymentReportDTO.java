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
public class PaymentReportDTO {
    private Long premiumPayments;
    private Long failedPayments;
    private Long refundedPayments;
    private Double totalRevenue;
    private List<Map<String, Object>> monthlyRevenue;
    private List<Map<String, Object>> paymentMethodSummary;
    private Long businessAdPayments;
    private Double businessAdRevenue;
}
