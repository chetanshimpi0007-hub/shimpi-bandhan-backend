package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessCategoryDTO;
import com.shimpimilan.dto.business.BusinessRegistrationRequest;
import com.shimpimilan.dto.business.BusinessResponse;
import com.shimpimilan.dto.business.BusinessSearchRequest;
import com.shimpimilan.exception.ResourceNotFoundException;
import com.shimpimilan.model.User;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessCategory;
import com.shimpimilan.model.business.BusinessStatus;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.business.BusinessCategoryRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.business.BusinessReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final BusinessRepository businessRepository;
    private final BusinessCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BusinessReviewRepository reviewRepository;

    @Override
    public List<BusinessCategoryDTO> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::mapToCategoryDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BusinessResponse registerBusiness(Long ownerId, BusinessRegistrationRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BusinessCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Business business = Business.builder()
                .owner(owner)
                .category(category)
                .businessName(request.getBusinessName())
                .ownerName(request.getOwnerName())
                .mobileNumber(request.getMobileNumber())
                .whatsappNumber(request.getWhatsappNumber())
                .email(request.getEmail())
                .description(request.getDescription())
                .gstNumber(request.getGstNumber())
                .website(request.getWebsite())
                .instagram(request.getInstagram())
                .facebook(request.getFacebook())
                .youtube(request.getYoutube())
                .addressLine(request.getAddressLine())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .googleMapsUrl(request.getGoogleMapsUrl())
                .logoUrl(request.getLogoUrl())
                .coverUrl(request.getCoverUrl())
                .workingHours(request.getWorkingHours())
                .yearsOfExperience(request.getYearsOfExperience())
                .status(BusinessStatus.PENDING_PAYMENT)
                .isVerified(false)
                .build();

        Business savedBusiness = businessRepository.save(business);
        
        // TODO: Enqueue Notification for Business Registration Submitted

        return mapToBusinessResponse(savedBusiness);
    }

    @Override
    public BusinessResponse getBusinessDetails(Long businessId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        return mapToBusinessResponse(business);
    }

    @Override
    public org.springframework.data.domain.Page<BusinessResponse> searchBusinesses(BusinessSearchRequest request, int page, int size) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt");
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

        org.springframework.data.jpa.domain.Specification<Business> spec = BusinessSpecification.getSearchSpecification(request);
        org.springframework.data.domain.Page<Business> businesses = businessRepository.findAll(spec, pageable);

        return businesses.map(this::mapToBusinessResponse);
    }

    @Override
    @Transactional
    public void changeBusinessStatus(Long businessId, BusinessStatus status) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        business.setStatus(status);
        if (status == BusinessStatus.ACTIVE) {
            business.setIsVerified(true);
        }
        businessRepository.save(business);
    }

    @Override
    public com.shimpimilan.dto.business.BusinessAnalyticsResponse getAnalytics() {
        // Mock analytics data for now. In a real scenario, this would aggregate from repositories.
        return com.shimpimilan.dto.business.BusinessAnalyticsResponse.builder()
                .totalRevenue(15000.0)
                .monthlyRevenue(3500.0)
                .activeBusinesses(businessRepository.count()) // Simple approximation
                .expiredBusinesses(0L)
                .pendingApprovals(0L)
                .totalLeadsGenerated(0L)
                .build();
    }

    @Override
    @Transactional
    public void toggleAdminFeatured(Long businessId, boolean featured) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        business.setIsAdminFeatured(featured);
        businessRepository.save(business);
    }

    @Override
    @Transactional
    public void setPriorityOverride(Long businessId, int priority) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        business.setPriorityOverride(priority);
        businessRepository.save(business);
    }

    @Override
    @Transactional(readOnly = true)
    public com.shimpimilan.dto.business.BusinessDashboardDTO getDashboardStats(Long businessId, Long userId) {
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business not found"));
        if (!business.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Base metrics (In a real app, query from BusinessReviewRepository, BusinessLeadRepository etc.)
        Long totalViews = 0L;
        Long phoneClicks = 0L;
        Long whatsappClicks = 0L;
        Long websiteClicks = 0L;
        Long googleMapsClicks = 0L;
        Long offerViews = 0L;
        Long offerClicks = 0L;
        Long totalReviews = 0L;
        Double avgRating = 0.0;

        int completionScore = 50; // base score
        java.util.List<String> suggestions = new java.util.ArrayList<>();
        
        if (business.getCoverUrl() == null || business.getCoverUrl().isEmpty()) {
            suggestions.add("Upload a cover banner.");
        } else {
            completionScore += 10;
        }

        if (business.getWorkingHoursJson() == null || business.getWorkingHoursJson().isEmpty()) {
            suggestions.add("Add working hours.");
        } else {
            completionScore += 10;
        }

        if (business.getSocialLinksJson() == null || business.getSocialLinksJson().isEmpty()) {
            suggestions.add("Add social media links.");
        } else {
            completionScore += 10;
        }

        int performanceScore = completionScore; // Calculate out of 100 based on views and ratings as well

        return com.shimpimilan.dto.business.BusinessDashboardDTO.builder()
                .currentPlan(business.getPlanType() != null ? business.getPlanType().name() : "NONE")
                .planStatus(business.getStatus().name())
                .planExpiryDate("2026-12-31") // Example date, fetch from BusinessSubscription
                .daysRemaining(180L)
                .profileCompletionPercentage(completionScore)
                .performanceScore(performanceScore)
                .improvementSuggestions(suggestions)
                .totalViews(totalViews)
                .dailyViews(0L)
                .monthlyViews(0L)
                .phoneClicks(phoneClicks)
                .whatsappClicks(whatsappClicks)
                .websiteClicks(websiteClicks)
                .googleMapsClicks(googleMapsClicks)
                .offerViews(offerViews)
                .offerClicks(offerClicks)
                .totalReviews(totalReviews)
                .averageRating(avgRating)
                .totalSpent(0.0)
                .build();
    }

    private BusinessCategoryDTO mapToCategoryDTO(BusinessCategory category) {
        return BusinessCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .isActive(category.getIsActive())
                .build();
    }

    private BusinessResponse mapToBusinessResponse(Business business) {
        Double avgRating = reviewRepository.getAverageRatingForBusiness(business.getId());
        Long totalReviews = reviewRepository.countApprovedReviewsByBusinessId(business.getId());

        return BusinessResponse.builder()
                .id(business.getId())
                .ownerId(business.getOwner().getId())
                .categoryName(business.getCategory().getName())
                .businessName(business.getBusinessName())
                .ownerName(business.getOwnerName())
                .mobileNumber(business.getMobileNumber())
                .whatsappNumber(business.getWhatsappNumber())
                .email(business.getEmail())
                .description(business.getDescription())
                .gstNumber(business.getGstNumber())
                .website(business.getWebsite())
                .instagram(business.getInstagram())
                .facebook(business.getFacebook())
                .youtube(business.getYoutube())
                .addressLine(business.getAddressLine())
                .city(business.getCity())
                .state(business.getState())
                .pinCode(business.getPinCode())
                .googleMapsUrl(business.getGoogleMapsUrl())
                .logoUrl(business.getLogoUrl())
                .coverUrl(business.getCoverUrl())
                .workingHours(business.getWorkingHours())
                .yearsOfExperience(business.getYearsOfExperience())
                .status(business.getStatus())
                .isVerified(business.getIsVerified())
                .planType(business.getPlanType())
                .createdAt(business.getCreatedAt())
                .averageRating(avgRating == null ? 0.0 : Math.round(avgRating * 10.0) / 10.0)
                .totalReviews(totalReviews)
                .build();
    }
}
