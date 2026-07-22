package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessCategoryDTO;
import com.shimpimilan.dto.business.BusinessRegistrationRequest;
import com.shimpimilan.dto.business.BusinessResponse;
import com.shimpimilan.dto.business.BusinessSearchRequest;

import java.util.List;

import org.springframework.data.domain.Page;

public interface BusinessService {
    List<BusinessCategoryDTO> getAllActiveCategories();
    BusinessResponse registerBusiness(Long ownerId, BusinessRegistrationRequest request);
    BusinessResponse getBusinessDetails(Long businessId);
    org.springframework.data.domain.Page<BusinessResponse> searchBusinesses(BusinessSearchRequest request, int page, int size);
    
    // Admin Methods
    void changeBusinessStatus(Long businessId, com.shimpimilan.model.business.BusinessStatus status);
    com.shimpimilan.dto.business.BusinessAnalyticsResponse getAnalytics();
    void toggleAdminFeatured(Long businessId, boolean featured);
    void setPriorityOverride(Long businessId, int priority);
    
    // Owner Methods
    com.shimpimilan.dto.business.BusinessDashboardDTO getDashboardStats(Long businessId, Long userId);
}
