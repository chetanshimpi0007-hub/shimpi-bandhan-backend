package com.shimpimilan.dto.business;

import com.shimpimilan.model.business.AdvertisementPlan;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusinessPaymentRequest {
    @NotNull(message = "Plan type is required")
    private AdvertisementPlan planType;
}
