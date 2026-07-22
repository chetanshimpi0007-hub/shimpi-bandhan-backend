package com.shimpimilan.service;

import com.shimpimilan.dto.DashboardStatsDTO;
import com.shimpimilan.model.PaymentStatus;
import com.shimpimilan.model.PlanType;
import com.shimpimilan.model.business.AdvertisementPlan;
import com.shimpimilan.model.business.BusinessStatus;
import com.shimpimilan.model.business.EnquiryStatus;
import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.profile.VerificationStatus;
import com.shimpimilan.repository.ChatRoomRepository;
import com.shimpimilan.repository.PaymentRepository;
import com.shimpimilan.repository.ProfilePhotoRepository;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.business.BusinessEnquiryRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final BusinessRepository businessRepository;
    private final PaymentRepository paymentRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final BusinessEnquiryRepository businessEnquiryRepository;

    public DashboardStatsDTO getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        long totalRegisteredUsers = userRepository.count();
        long pendingProfileApprovals = profileRepository.countByVerificationStatus(VerificationStatus.SUBMITTED_FOR_VERIFICATION);
        long approvedProfiles = profileRepository.countByVerificationStatus(VerificationStatus.APPROVED);
        long rejectedProfiles = profileRepository.countByVerificationStatus(VerificationStatus.REJECTED);
        long pendingPhotoApprovals = profilePhotoRepository.countByStatus(PhotoStatus.PENDING);
        
        long premiumMembers = profileRepository.countByPlanType(PlanType.PREMIUM);
        long freeTrialUsers = profileRepository.countByPlanType(PlanType.FREE_TRIAL);
        
        long activeBusinessListings = businessRepository.countByStatus(BusinessStatus.ACTIVE);
        long pendingBusinessApprovals = businessRepository.countByIsVerifiedFalse(); // Assuming isVerified = false means pending approval
        long goldMembers = businessRepository.countByPlanType(AdvertisementPlan.GOLD);
        long platinumMembers = businessRepository.countByPlanType(AdvertisementPlan.PLATINUM);
        
        Double totalRevenue = paymentRepository.sumFinalAmountPaidByStatus(PaymentStatus.CAPTURED);
        Double monthlyRevenue = paymentRepository.sumFinalAmountPaidByStatusAndCreatedAtAfter(PaymentStatus.CAPTURED, startOfMonth);
        
        long todaysRegistrations = userRepository.countByCreatedAtAfter(startOfDay);
        long activeChats = chatRoomRepository.count();
        
        // Count active enquiries (not COMPLETED, REJECTED, CANCELLED)
        // Since we don't have a count by status not in, we'll just count all for simplicity or use stream
        long activeEnquiries = businessEnquiryRepository.findAll().stream()
                .filter(e -> e.getStatus() != EnquiryStatus.COMPLETED 
                        && e.getStatus() != EnquiryStatus.REJECTED 
                        && e.getStatus() != EnquiryStatus.CANCELLED)
                .count();

        return DashboardStatsDTO.builder()
                .totalRegisteredUsers(totalRegisteredUsers)
                .pendingProfileApprovals(pendingProfileApprovals)
                .approvedProfiles(approvedProfiles)
                .rejectedProfiles(rejectedProfiles)
                .pendingPhotoApprovals(pendingPhotoApprovals)
                .premiumMembers(premiumMembers)
                .freeTrialUsers(freeTrialUsers)
                .activeBusinessListings(activeBusinessListings)
                .pendingBusinessApprovals(pendingBusinessApprovals)
                .goldMembers(goldMembers)
                .platinumMembers(platinumMembers)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : 0.0)
                .todaysRegistrations(todaysRegistrations)
                .activeChats(activeChats)
                .activeEnquiries(activeEnquiries)
                .build();
    }
}
