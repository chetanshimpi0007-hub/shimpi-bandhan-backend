package com.shimpimilan.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumReportDTO {
    private Long freeMembers;
    private Long freeTrialMembers;
    private Long premiumMembers;
    private Long expiredMemberships;
    private Long upcomingRenewals;
    private Double totalRevenueGenerated;
    private Double referralDiscountsUsed;
    private Double conversionRate;
}
