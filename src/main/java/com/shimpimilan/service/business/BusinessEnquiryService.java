package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.*;
import com.shimpimilan.model.business.BusinessEnquiry;

import java.util.List;

public interface BusinessEnquiryService {
    BusinessEnquiry createEnquiry(Long userId, Long businessId, EnquiryCreateDto dto);
    
    BusinessEnquiry getEnquiryById(Long enquiryId);
    
    List<BusinessEnquiry> getEnquiriesByBusiness(Long businessId);
    
    List<BusinessEnquiry> getEnquiriesByUser(Long userId);
    
    BusinessEnquiry updateStatus(Long enquiryId, String newStatus, Long changedById, String note);
    
    void addNote(Long enquiryId, Long addedById, String content);
    
    void addMeeting(Long enquiryId, MeetingCreateDto dto);
    
    void addFollowUp(Long enquiryId, FollowUpCreateDto dto);
    
    void monitorSla();
}
