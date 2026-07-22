package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessOfferRequest;
import com.shimpimilan.dto.business.BusinessOfferResponse;

import java.util.List;

public interface BusinessOfferService {
    BusinessOfferResponse createOffer(Long businessId, Long userId, BusinessOfferRequest request);
    BusinessOfferResponse updateOffer(Long businessId, Long userId, Long offerId, BusinessOfferRequest request);
    void deleteOffer(Long businessId, Long userId, Long offerId);
    void toggleOfferStatus(Long businessId, Long userId, Long offerId, boolean status);
    List<BusinessOfferResponse> getActiveOffersByBusiness(Long businessId);
}
