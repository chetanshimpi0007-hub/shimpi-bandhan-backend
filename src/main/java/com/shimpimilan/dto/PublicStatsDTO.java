package com.shimpimilan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicStatsDTO {
    private long totalMembers;
    private long verifiedProfiles;
    private long premiumMembers;
    private long successStories;
}
