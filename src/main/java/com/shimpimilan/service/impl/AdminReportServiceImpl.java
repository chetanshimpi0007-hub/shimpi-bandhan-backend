package com.shimpimilan.service.impl;

import com.shimpimilan.dto.report.*;
import com.shimpimilan.model.*;
import com.shimpimilan.model.business.*;
import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.profile.VerificationStatus;
import com.shimpimilan.repository.*;
import com.shimpimilan.repository.business.BusinessEnquiryRepository;
import com.shimpimilan.repository.business.BusinessPaymentRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.notification.InAppNotificationRepository;
import com.shimpimilan.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportServiceImpl implements AdminReportService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final BusinessEnquiryRepository businessEnquiryRepository;
    private final BusinessPaymentRepository businessPaymentRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReportRepository chatReportRepository;
    private final AuditLogRepository auditLogRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final InAppNotificationRepository inAppNotificationRepository;

    @Override
    public DashboardSummaryDTO getDashboardSummary() {
        return DashboardSummaryDTO.builder()
                .totalUsers(userRepository.count())
                .premiumMembers(profileRepository.countByPlanType(PlanType.PREMIUM))
                .freeTrialUsers(profileRepository.countByPlanType(PlanType.FREE_TRIAL))
                .freeUsers(profileRepository.countByPlanType(PlanType.FREE))
                .totalBusinesses(businessRepository.count())
                .goldBusinesses(businessRepository.countByPlanType(AdvertisementPlan.GOLD))
                .platinumBusinesses(businessRepository.countByPlanType(AdvertisementPlan.PLATINUM))
                .pendingProfiles(profileRepository.countByVerificationStatus(VerificationStatus.SUBMITTED_FOR_VERIFICATION))
                .pendingPhotos(profilePhotoRepository.countByStatus(PhotoStatus.PENDING))
                .totalRevenue(paymentRepository.sumFinalAmountPaidByStatus(PaymentStatus.CAPTURED))
                .activeChats(chatRoomRepository.count())
                .totalEnquiries(businessEnquiryRepository.count())
                .totalNotifications(inAppNotificationRepository.count())
                .build();
    }

    @Override
    public UserReportDTO getUserReports(LocalDateTime startDate, LocalDateTime endDate, String community, String gender, String planType, String status) {
        Long totalUsers = userRepository.countFiltered(startDate, endDate);

        Map<String, Long> communityWise = convertAggregation(userRepository.countByCommunityAggregation(startDate, endDate));
        Map<String, Long> genderRatio = convertAggregation(profileRepository.countByGenderAggregation(startDate, endDate));
        Map<String, Long> statusWise = convertAggregation(userRepository.countByStatusAggregation(startDate, endDate));
        Map<String, Long> planTypeDistribution = convertAggregation(profileRepository.countByPlanTypeAggregation(startDate, endDate));
        Map<String, Long> verificationStatus = convertAggregation(profileRepository.countByVerificationStatusAggregation(startDate, endDate));
        Map<String, Long> cityWise = convertAggregation(profileRepository.countByCityAggregation(startDate, endDate));
        Map<String, Long> stateWise = convertAggregation(profileRepository.countByStateAggregation(startDate, endDate));

        return UserReportDTO.builder()
                .totalUsers(totalUsers)
                .communityWise(communityWise)
                .genderRatio(genderRatio)
                .statusWise(statusWise)
                .planTypeDistribution(planTypeDistribution)
                .verificationStatus(verificationStatus)
                .cityWise(cityWise)
                .stateWise(stateWise)
                .build();
    }

    @Override
    public PremiumReportDTO getPremiumReports() {
        Long freeMembers = profileRepository.countByPlanType(PlanType.FREE);
        Long freeTrialMembers = profileRepository.countByPlanType(PlanType.FREE_TRIAL);
        Long premiumMembers = profileRepository.countByPlanType(PlanType.PREMIUM);
        Long totalUsers = freeMembers + freeTrialMembers + premiumMembers;
        double conversionRate = totalUsers > 0 ? (premiumMembers * 100.0) / totalUsers : 0;

        return PremiumReportDTO.builder()
                .freeMembers(freeMembers)
                .freeTrialMembers(freeTrialMembers)
                .premiumMembers(premiumMembers)
                .expiredMemberships(profileRepository.countExpiredPremium(LocalDateTime.now()))
                .upcomingRenewals(profileRepository.countUpcomingRenewals(LocalDateTime.now(), LocalDateTime.now().plusDays(30)))
                .totalRevenueGenerated(paymentRepository.sumFinalAmountPaidByStatus(PaymentStatus.CAPTURED))
                .referralDiscountsUsed(paymentRepository.sumDiscountsApplied())
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public PaymentReportDTO getPaymentReports(LocalDateTime startDate, LocalDateTime endDate) {
        return PaymentReportDTO.builder()
                .premiumPayments(paymentRepository.countByStatus(PaymentStatus.CAPTURED))
                .failedPayments(paymentRepository.countByStatus(PaymentStatus.FAILED))
                .refundedPayments(paymentRepository.countByStatus(PaymentStatus.REFUNDED))
                .totalRevenue(paymentRepository.sumFinalAmountPaidByStatus(PaymentStatus.CAPTURED))
                .monthlyRevenue(paymentRepository.monthlyRevenue(startDate, endDate))
                .paymentMethodSummary(paymentRepository.paymentMethodSummary())
                .businessAdPayments(businessPaymentRepository.count())
                .businessAdRevenue(businessPaymentRepository.sumTotalRevenue())
                .build();
    }

    @Override
    public BusinessReportDTO getBusinessReports() {
        Map<String, Long> statusWise = new LinkedHashMap<>();
        for (BusinessStatus s : BusinessStatus.values()) {
            statusWise.put(s.name(), businessRepository.countByStatus(s));
        }
        Map<String, Long> planWise = new LinkedHashMap<>();
        for (AdvertisementPlan p : AdvertisementPlan.values()) {
            planWise.put(p.name(), businessRepository.countByPlanType(p));
        }

        return BusinessReportDTO.builder()
                .totalBusinesses(businessRepository.count())
                .statusWise(statusWise)
                .planWise(planWise)
                .expiredPlans(businessRepository.countByStatus(BusinessStatus.EXPIRED))
                .build();
    }

    @Override
    public EnquiryReportDTO getEnquiryReports() {
        Map<String, Long> statusWise = new LinkedHashMap<>();
        for (EnquiryStatus s : EnquiryStatus.values()) {
            statusWise.put(s.name(), businessEnquiryRepository.countByStatus(s));
        }
        long total = businessEnquiryRepository.count();
        long completed = businessEnquiryRepository.countByStatus(EnquiryStatus.COMPLETED);
        double conversionRate = total > 0 ? (completed * 100.0) / total : 0;

        return EnquiryReportDTO.builder()
                .totalEnquiries(total)
                .statusWise(statusWise)
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public ChatReportSummaryDTO getChatReports() {
        return ChatReportSummaryDTO.builder()
                .totalConversations(chatRoomRepository.count())
                .totalMessages(chatMessageRepository.count())
                .reportedConversations(chatReportRepository.countByStatus(ReportStatus.PENDING))
                .deletedMessages(chatMessageRepository.countByIsDeleted(true))
                .blockedUsers(userRepository.countByStatus(UserStatus.BLOCKED))
                .moderationActionsToday(auditLogRepository.countByModuleAndTimestampAfter("CHAT_MODERATION", LocalDate.now().atStartOfDay()))
                .build();
    }

    @Override
    public AuditLogReportDTO getAuditLogReports(String module, Pageable pageable) {
        Map<String, Long> moduleWise = new LinkedHashMap<>();
        List<String> modules = List.of("USER_MANAGEMENT", "PROFILE_VERIFICATION", "PHOTO_MODERATION",
            "CHAT_MODERATION", "PAYMENT", "BUSINESS", "EXPORT", "SETTINGS");
        for (String m : modules) {
            moduleWise.put(m, auditLogRepository.countByModuleAndTimestampAfter(m, LocalDateTime.now().minusYears(10)));
        }

        Page<AuditLog> logs = (module != null && !module.isBlank()) 
            ? auditLogRepository.findByModule(module, pageable)
            : auditLogRepository.findAllByOrderByTimestampDesc(pageable);

        return AuditLogReportDTO.builder()
                .totalLogs(auditLogRepository.count())
                .moduleWise(moduleWise)
                .actionsToday(auditLogRepository.countByModuleAndTimestampAfter(null, LocalDate.now().atStartOfDay()))
                .logs(logs)
                .build();
    }

    @Override
    public RevenueReportDTO getRevenueReports(LocalDateTime startDate, LocalDateTime endDate) {
        return RevenueReportDTO.builder()
                .totalPremiumRevenue(paymentRepository.sumFinalAmountPaidByStatus(PaymentStatus.CAPTURED))
                .totalBusinessAdRevenue(businessPaymentRepository.sumTotalRevenue())
                .monthlyRevenueTrend(paymentRepository.monthlyRevenue(startDate, endDate))
                .paymentMethodBreakdown(paymentRepository.paymentMethodSummary())
                .yearlyRevenue(paymentRepository.yearlyRevenue())
                .build();
    }

    private Map<String, Long> convertAggregation(List<Map<String, Object>> result) {
        Map<String, Long> map = new LinkedHashMap<>();
        for (Map<String, Object> row : result) {
            Object labelObj = row.get("label");
            String label = labelObj != null ? labelObj.toString() : "Unknown";
            Long count = ((Number) row.get("count")).longValue();
            map.put(label, count);
        }
        return map;
    }
}
