package com.shimpimilan.dto.business;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BusinessOfferResponse {
    private Long id;
    private Long businessId;
    private String title;
    private String description;
    private String discount;
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;
    private String bannerUrl;
    private String termsAndConditions;
    private Boolean isActive;
}
