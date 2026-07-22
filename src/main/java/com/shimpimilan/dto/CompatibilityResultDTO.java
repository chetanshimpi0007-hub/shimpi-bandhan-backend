package com.shimpimilan.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CompatibilityResultDTO {
    private int overallCompatibilityPercentage;
    private Map<String, Integer> detailedBreakdown;
    private List<String> strengths;
    private List<String> possibleDifferences;
}
