package com.shimpimilan.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {
    private Long totalUsers;
    private Long premiumMembers;
    private Long freeTrialUsers;
    private Long freeUsers;
    private Long totalBusinesses;
    private Long goldBusinesses;
    private Long platinumBusinesses;
    private Long pendingProfiles;
    private Long pendingPhotos;
    private Double totalRevenue;
    private Long activeChats;
    private Long totalEnquiries;
    private Long totalNotifications;
}
