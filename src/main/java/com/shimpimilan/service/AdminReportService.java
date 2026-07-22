package com.shimpimilan.service;

import com.shimpimilan.dto.report.*;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

public interface AdminReportService {
    DashboardSummaryDTO getDashboardSummary();
    
    UserReportDTO getUserReports(LocalDateTime startDate, LocalDateTime endDate, String community, String gender, String planType, String status);
    
    PremiumReportDTO getPremiumReports();
    
    PaymentReportDTO getPaymentReports(LocalDateTime startDate, LocalDateTime endDate);
    
    BusinessReportDTO getBusinessReports();
    
    EnquiryReportDTO getEnquiryReports();
    
    ChatReportSummaryDTO getChatReports();
    
    AuditLogReportDTO getAuditLogReports(String module, Pageable pageable);
    
    RevenueReportDTO getRevenueReports(LocalDateTime startDate, LocalDateTime endDate);
}
