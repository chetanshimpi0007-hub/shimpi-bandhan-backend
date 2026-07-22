package com.shimpimilan.dto.business;

import com.shimpimilan.model.business.LeadType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BusinessLeadRequest {
    @NotNull(message = "Lead type is required")
    private LeadType leadType;
}
