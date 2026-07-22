package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessOfferRequest;
import com.shimpimilan.dto.business.BusinessOfferResponse;
import com.shimpimilan.exception.ResourceNotFoundException;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessOffer;
import com.shimpimilan.repository.business.BusinessOfferRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessOfferServiceImpl implements BusinessOfferService {

    private final BusinessRepository businessRepository;
    private final BusinessOfferRepository offerRepository;

    private Business validateBusinessOwnership(Long businessId, Long userId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        if (!business.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        return business;
    }

    @Override
    @Transactional
    public BusinessOfferResponse createOffer(Long businessId, Long userId, BusinessOfferRequest request) {
        Business business = validateBusinessOwnership(businessId, userId);

        BusinessOffer offer = BusinessOffer.builder()
                .business(business)
                .title(request.getTitle())
                .description(request.getDescription())
                .discount(request.getDiscount())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .bannerUrl(request.getBannerUrl())
                .termsAndConditions(request.getTermsAndConditions())
                .isActive(true)
                .build();

        return mapToResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional
    public BusinessOfferResponse updateOffer(Long businessId, Long userId, Long offerId, BusinessOfferRequest request) {
        validateBusinessOwnership(businessId, userId);
        BusinessOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        if (!offer.getBusiness().getId().equals(businessId)) {
            throw new RuntimeException("Offer does not belong to this business");
        }

        offer.setTitle(request.getTitle());
        offer.setDescription(request.getDescription());
        offer.setDiscount(request.getDiscount());
        offer.setValidFrom(request.getValidFrom());
        offer.setValidUntil(request.getValidUntil());
        offer.setBannerUrl(request.getBannerUrl());
        offer.setTermsAndConditions(request.getTermsAndConditions());

        return mapToResponse(offerRepository.save(offer));
    }

    @Override
    @Transactional
    public void deleteOffer(Long businessId, Long userId, Long offerId) {
        validateBusinessOwnership(businessId, userId);
        BusinessOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
        if (!offer.getBusiness().getId().equals(businessId)) {
            throw new RuntimeException("Offer does not belong to this business");
        }
        offerRepository.delete(offer);
    }

    @Override
    @Transactional
    public void toggleOfferStatus(Long businessId, Long userId, Long offerId, boolean status) {
        validateBusinessOwnership(businessId, userId);
        BusinessOffer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));
        if (!offer.getBusiness().getId().equals(businessId)) {
            throw new RuntimeException("Offer does not belong to this business");
        }
        offer.setIsActive(status);
        offerRepository.save(offer);
    }

    @Override
    public List<BusinessOfferResponse> getActiveOffersByBusiness(Long businessId) {
        return offerRepository.findByBusinessIdAndIsActiveTrueAndValidUntilAfter(businessId, LocalDateTime.now())
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private BusinessOfferResponse mapToResponse(BusinessOffer offer) {
        return BusinessOfferResponse.builder()
                .id(offer.getId())
                .businessId(offer.getBusiness().getId())
                .title(offer.getTitle())
                .description(offer.getDescription())
                .discount(offer.getDiscount())
                .validFrom(offer.getValidFrom())
                .validUntil(offer.getValidUntil())
                .bannerUrl(offer.getBannerUrl())
                .termsAndConditions(offer.getTermsAndConditions())
                .isActive(offer.getIsActive())
                .build();
    }
}
