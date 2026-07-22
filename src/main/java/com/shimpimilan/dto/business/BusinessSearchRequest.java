package com.shimpimilan.dto.business;

import lombok.Data;

@Data
public class BusinessSearchRequest {
    private String query; // Search by business name or description
    private Long categoryId;
    private String city;
    private String state;
    private String sortBy; // RATING, NEWEST, MOST_VIEWED, ALPHABETICAL
    private Boolean activeOffersOnly;
    private Boolean openNow;
}
