package com.shimpimilan.dto.business;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessAnalyticsResponse {
    private Double totalRevenue;
    private Double monthlyRevenue;
    private Long activeBusinesses;
    private Long expiredBusinesses;
    private Long pendingApprovals;
    private Long totalLeadsGenerated;
}
