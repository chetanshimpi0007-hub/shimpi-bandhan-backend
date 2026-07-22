package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessLeadRequest;
import com.shimpimilan.dto.business.BusinessReviewRequest;

public interface BusinessInteractionService {
    void addReview(Long businessId, Long userId, BusinessReviewRequest request);
    void trackLead(Long businessId, Long userId, BusinessLeadRequest request);
}
