package com.shimpimilan.service.business;

import com.shimpimilan.config.BusinessEnquiryConfig;
import com.shimpimilan.dto.business.EnquiryCreateDto;
import com.shimpimilan.dto.business.FollowUpCreateDto;
import com.shimpimilan.dto.business.MeetingCreateDto;
import com.shimpimilan.model.User;
import com.shimpimilan.model.business.*;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.business.*;
import com.shimpimilan.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessEnquiryServiceImpl implements BusinessEnquiryService {

    private final BusinessEnquiryRepository enquiryRepository;
    private final BusinessEnquiryHistoryRepository historyRepository;
    private final BusinessEnquiryNoteRepository noteRepository;
    private final BusinessEnquiryMeetingRepository meetingRepository;
    private final BusinessFollowUpRepository followUpRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final BusinessEnquiryConfig config;

    @Override
    @Transactional
    public BusinessEnquiry createEnquiry(Long userId, Long businessId, EnquiryCreateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        BusinessEnquiry enquiry = new BusinessEnquiry();
        enquiry.setUser(user);
        enquiry.setBusiness(business);
        enquiry.setBudget(dto.getBudget());
        enquiry.setWeddingDate(dto.getWeddingDate());
        enquiry.setMessage(dto.getMessage());
        enquiry.setStatus(EnquiryStatus.NEW);

        int profileCompletion = calculateProfileCompletion(user);
        enquiry.setProfileCompletion(profileCompletion);

        calculatePriorityAndScore(enquiry, business);

        enquiry = enquiryRepository.save(enquiry);

        createHistoryEntry(enquiry, null, EnquiryStatus.NEW, user, "Enquiry created");

        // Notify Business Owner
        notificationService.notifyUser(
                business.getOwner().getId(),
                "business_enquiry_new",
                "New Business Enquiry",
                "You have a new enquiry from " + (user.getProfile() != null ? user.getProfile().getFullName() : "User"),
                Map.of("enquiryId", enquiry.getId(), "businessName", business.getBusinessName())
        );

        return enquiry;
    }

    private int calculateProfileCompletion(User user) {
        if (user.getProfile() == null) return 0;
        int score = 0;
        if (user.getProfile().getFullName() != null) score += 40;
        if (user.getPhone() != null) score += 30;
        if (user.getProfile().getEmail() != null) score += 30;
        return score;
    }

    private void calculatePriorityAndScore(BusinessEnquiry enquiry, Business business) {
        double score = 0.0;
        BusinessEnquiryConfig.Scoring scoring = config.getScoring();
        BusinessEnquiryConfig.Priority priorityConfig = config.getPriority();

        // 1. Profile Completion (0-100)
        score += (enquiry.getProfileCompletion() * scoring.getProfileWeight());

        // 2. Budget
        boolean isHighBudget = false;
        if (enquiry.getBudget() != null) {
            double budgetPct = Math.min(100.0, (enquiry.getBudget() / priorityConfig.getHighBudgetThreshold()) * 100);
            score += (budgetPct * scoring.getBudgetWeight());
            if (enquiry.getBudget() >= priorityConfig.getHighBudgetThreshold()) {
                isHighBudget = true;
            }
        }

        // 3. Timeline
        boolean isShortTimeline = false;
        if (enquiry.getWeddingDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), enquiry.getWeddingDate());
            double timelineScore = 0;
            if (days < priorityConfig.getWeddingDaysThreshold()) {
                timelineScore = 100;
                isShortTimeline = true;
            } else if (days < 90) {
                timelineScore = 70;
            } else if (days < 180) {
                timelineScore = 40;
            } else {
                timelineScore = 10;
            }
            score += (timelineScore * scoring.getTimelineWeight());
        }

        enquiry.setLeadScore((int) Math.round(score));

        boolean isPremium = false; // Add logic if business is premium, assuming not implemented fully yet

        if (isPremium || isHighBudget || isShortTimeline) {
            enquiry.setPriority(EnquiryPriority.HIGH);
        } else if (enquiry.getLeadScore() > 40) {
            enquiry.setPriority(EnquiryPriority.MEDIUM);
        } else {
            enquiry.setPriority(EnquiryPriority.LOW);
        }
    }

    private void createHistoryEntry(BusinessEnquiry enquiry, EnquiryStatus oldStatus, EnquiryStatus newStatus, User changedBy, String note) {
        BusinessEnquiryHistory history = new BusinessEnquiryHistory();
        history.setEnquiry(enquiry);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNote(note);
        historyRepository.save(history);
    }

    @Override
    public BusinessEnquiry getEnquiryById(Long enquiryId) {
        return enquiryRepository.findById(enquiryId)
                .orElseThrow(() -> new RuntimeException("Enquiry not found"));
    }

    @Override
    public List<BusinessEnquiry> getEnquiriesByBusiness(Long businessId) {
        return enquiryRepository.findByBusinessId(businessId);
    }

    @Override
    public List<BusinessEnquiry> getEnquiriesByUser(Long userId) {
        return enquiryRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public BusinessEnquiry updateStatus(Long enquiryId, String newStatusStr, Long changedById, String note) {
        BusinessEnquiry enquiry = getEnquiryById(enquiryId);
        EnquiryStatus oldStatus = enquiry.getStatus();
        EnquiryStatus newStatus = EnquiryStatus.valueOf(newStatusStr);
        
        enquiry.setStatus(newStatus);
        enquiry = enquiryRepository.save(enquiry);
        
        User changedBy = userRepository.findById(changedById).orElse(null);
        createHistoryEntry(enquiry, oldStatus, newStatus, changedBy, note);

        // Notify user about status change
        notificationService.notifyUser(
                enquiry.getUser().getId(),
                "business_enquiry_status_update",
                "Enquiry Update",
                "Your enquiry for " + enquiry.getBusiness().getBusinessName() + " is now " + newStatus,
                Map.of("enquiryId", enquiry.getId(), "status", newStatus.name())
        );

        return enquiry;
    }

    @Override
    @Transactional
    public void addNote(Long enquiryId, Long addedById, String content) {
        BusinessEnquiry enquiry = getEnquiryById(enquiryId);
        User addedBy = userRepository.findById(addedById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        BusinessEnquiryNote note = new BusinessEnquiryNote();
        note.setEnquiry(enquiry);
        note.setAddedBy(addedBy);
        note.setContent(content);
        noteRepository.save(note);
    }

    @Override
    @Transactional
    public void addMeeting(Long enquiryId, MeetingCreateDto dto) {
        BusinessEnquiry enquiry = getEnquiryById(enquiryId);
        BusinessEnquiryMeeting meeting = new BusinessEnquiryMeeting();
        meeting.setEnquiry(enquiry);
        meeting.setType(MeetingType.valueOf(dto.getType()));
        meeting.setScheduledAt(dto.getScheduledAt());
        meeting.setNotes(dto.getNotes());
        meeting.setStatus(MeetingStatus.SCHEDULED);
        meetingRepository.save(meeting);
    }

    @Override
    @Transactional
    public void addFollowUp(Long enquiryId, FollowUpCreateDto dto) {
        BusinessEnquiry enquiry = getEnquiryById(enquiryId);
        BusinessFollowUp followUp = new BusinessFollowUp();
        followUp.setEnquiry(enquiry);
        followUp.setFollowUpDate(dto.getFollowUpDate());
        followUp.setOutcome(dto.getOutcome());
        followUp.setStatus(dto.getStatus() != null ? dto.getStatus() : "PENDING");
        followUpRepository.save(followUp);
        
        // Update enquiry's next follow up date
        enquiry.setNextFollowUpDate(dto.getFollowUpDate());
        enquiryRepository.save(enquiry);
    }

    @Override
    @Scheduled(cron = "0 0 * * * *") // Run hourly
    public void monitorSla() {
        LocalDateTime slaThreshold = LocalDateTime.now().minusHours(config.getSla().getOverdueHoursThreshold());
        
        // Find NEW inquiries that haven't been responded to
        List<BusinessEnquiry> overdueEnquiries = enquiryRepository.findAll().stream()
            .filter(e -> e.getStatus() == EnquiryStatus.NEW)
            .filter(e -> e.getCreatedAt().isBefore(slaThreshold))
            .toList();
            
        for (BusinessEnquiry enquiry : overdueEnquiries) {
            // Notify Business Owner about SLA breach
            notificationService.notifyUser(
                    enquiry.getBusiness().getOwner().getId(),
                    "business_enquiry_sla_breach",
                    "SLA Alert: Overdue Enquiry",
                    "Please respond to enquiry #" + enquiry.getId() + " immediately.",
                    Map.of("enquiryId", enquiry.getId())
            );
        }
    }
}
