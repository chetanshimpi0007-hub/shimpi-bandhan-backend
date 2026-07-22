package com.shimpimilan.dto.business;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BusinessDashboardDTO {
    private String currentPlan;
    private String planStatus;
    private String planExpiryDate;
    private Long daysRemaining;
    private Integer profileCompletionPercentage;
    
    // Performance Score
    private Integer performanceScore;
    private List<String> improvementSuggestions;

    // Engagement Metrics
    private Long totalViews;
    private Long dailyViews;
    private Long monthlyViews;
    private Long phoneClicks;
    private Long whatsappClicks;
    private Long websiteClicks;
    private Long googleMapsClicks;
    private Long offerViews;
    private Long offerClicks;
    
    // Reviews
    private Long totalReviews;
    private Double averageRating;
    
    // Revenue
    private Double totalSpent;
}
