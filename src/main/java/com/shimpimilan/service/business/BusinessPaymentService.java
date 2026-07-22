package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessPaymentRequest;
import com.shimpimilan.dto.business.BusinessPaymentVerifyRequest;
import com.shimpimilan.model.business.BusinessPayment;

public interface BusinessPaymentService {
    BusinessPayment createOrder(Long businessId, Long userId, BusinessPaymentRequest request);
    void verifyAndActivateSubscription(Long businessId, Long userId, BusinessPaymentVerifyRequest request);
}
