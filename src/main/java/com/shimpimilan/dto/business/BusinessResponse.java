package com.shimpimilan.dto.business;

import com.shimpimilan.model.business.AdvertisementPlan;
import com.shimpimilan.model.business.BusinessStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusinessResponse {
    private Long id;
    private Long ownerId;
    private String categoryName;
    private String businessName;
    private String ownerName;
    private String mobileNumber;
    private String whatsappNumber;
    private String email;
    private String description;
    private String gstNumber;
    private String website;
    private String instagram;
    private String facebook;
    private String youtube;
    private String addressLine;
    private String city;
    private String state;
    private String pinCode;
    private String googleMapsUrl;
    private String logoUrl;
    private String coverUrl;
    private String workingHours;
    private Integer yearsOfExperience;
    private BusinessStatus status;
    private Boolean isVerified;
    private AdvertisementPlan planType;
    private LocalDateTime createdAt;
    
    // Additional computed fields
    private Double averageRating;
    private Long totalReviews;
}
