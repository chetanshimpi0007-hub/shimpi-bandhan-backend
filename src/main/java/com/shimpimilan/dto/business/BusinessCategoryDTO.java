package com.shimpimilan.dto.business;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BusinessCategoryDTO {
    private Long id;
    private String name;
    private Boolean isActive;
}
