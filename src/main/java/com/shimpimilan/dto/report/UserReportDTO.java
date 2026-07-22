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
public class UserReportDTO {
    private Long totalUsers;
    private Map<String, Long> communityWise;
    private Map<String, Long> genderRatio;
    private Map<String, Long> statusWise;
    private Map<String, Long> planTypeDistribution;
    private Map<String, Long> verificationStatus;
    private Map<String, Long> cityWise;
    private Map<String, Long> stateWise;
}
