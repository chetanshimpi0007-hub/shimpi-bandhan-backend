package com.shimpimilan.controller;

import com.shimpimilan.model.*;
import com.shimpimilan.model.business.AdvertisementPlan;
import com.shimpimilan.model.business.BusinessStatus;
import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.profile.VerificationStatus;
import com.shimpimilan.repository.*;
import com.shimpimilan.repository.business.BusinessEnquiryRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PaymentRepository paymentRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final BusinessRepository businessRepository;
    private final BusinessEnquiryRepository businessEnquiryRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final SubscriptionRepository subscriptionRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();

        // --- Users ---
        stats.put("totalUsers", userRepository.count());
        stats.put("todayRegistrations", userRepository.countByCreatedAtAfter(today));
        stats.put("activeUsers", userRepository.countByStatus(UserStatus.APPROVED));
        stats.put("suspendedUsers", userRepository.countByStatus(UserStatus.SUSPENDED));
        stats.put("blockedUsers", userRepository.countByStatus(UserStatus.BLOCKED));
        stats.put("pendingUsers", userRepository.countByStatus(UserStatus.PENDING));

        // --- Profiles ---
        stats.put("pendingProfiles", profileRepository.countByVerificationStatus(VerificationStatus.SUBMITTED_FOR_VERIFICATION));
        stats.put("approvedProfiles", profileRepository.countByVerificationStatus(VerificationStatus.APPROVED));
        stats.put("rejectedProfiles", profileRepository.countByVerificationStatus(VerificationStatus.REJECTED));

        // --- Memberships ---
        stats.put("premiumMembers", profileRepository.countByPlanType(PlanType.PREMIUM));
        stats.put("freeTrialMembers", profileRepository.countByPlanType(PlanType.FREE_TRIAL));
        stats.put("freeMembers", profileRepository.countByPlanType(PlanType.FREE));

        // --- Photos ---
        stats.put("pendingPhotos", profilePhotoRepository.countByStatus(PhotoStatus.PENDING));
        stats.put("approvedPhotos", profilePhotoRepository.countByStatus(PhotoStatus.APPROVED));

        // --- Businesses ---
        stats.put("totalBusinesses", businessRepository.count());
        stats.put("pendingBusinesses", businessRepository.countByStatus(BusinessStatus.PENDING_PAYMENT));
        stats.put("approvedBusinesses", businessRepository.countByStatus(BusinessStatus.ACTIVE));
        stats.put("goldBusinesses", businessRepository.countByPlanType(AdvertisementPlan.GOLD));
        stats.put("platinumBusinesses", businessRepository.countByPlanType(AdvertisementPlan.PLATINUM));

        // --- Revenue ---
        Double totalRevenue = paymentRepository.sumFinalAmountPaidByStatus(PaymentStatus.CAPTURED);
        Double monthlyRevenue = paymentRepository.sumFinalAmountPaidByStatusAndCreatedAtAfter(PaymentStatus.CAPTURED, monthStart);
        Double todayRevenue = paymentRepository.sumFinalAmountPaidByStatusAndCreatedAtAfter(PaymentStatus.CAPTURED, today);

        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        stats.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
        stats.put("todayRevenue", todayRevenue != null ? todayRevenue : 0.0);

        // --- Chats ---
        stats.put("activeChatRooms", chatRoomRepository.count());

        // --- Additional Live Analytics ---
        // Enquiries
        stats.put("totalEnquiries", businessEnquiryRepository.count());

        // Payments
        stats.put("totalPayments", paymentRepository.count());
        stats.put("capturedPayments", paymentRepository.countByStatus(PaymentStatus.CAPTURED));

        // Notifications
        // I will just return 0 for now until I wire the NotificationQueueRepository
        stats.put("totalNotifications", 0);

        return ResponseEntity.ok(stats);
    }
}
