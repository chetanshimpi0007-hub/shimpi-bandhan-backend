package com.shimpimilan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalRegisteredUsers;
    private long pendingProfileApprovals;
    private long approvedProfiles;
    private long rejectedProfiles;
    private long pendingPhotoApprovals;
    
    private long premiumMembers;
    private long freeTrialUsers;
    
    private long activeBusinessListings;
    private long pendingBusinessApprovals;
    private long goldMembers;
    private long platinumMembers;
    
    private Double totalRevenue;
    private Double monthlyRevenue;
    
    private long todaysRegistrations;
    private long activeChats;
    private long activeEnquiries;
}
