package com.shimpimilan.controller;

import com.shimpimilan.dto.report.*;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.PaymentRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.business.BusinessEnquiryRepository;
import com.shimpimilan.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final BusinessEnquiryRepository businessEnquiryRepository;

    @GetMapping("/dashboard-summary")
    public ResponseEntity<DashboardSummaryDTO> getDashboardSummary() {
        return ResponseEntity.ok(adminReportService.getDashboardSummary());
    }

    @GetMapping("/users")
    public ResponseEntity<UserReportDTO> getUserReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String community,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status) {
        
        LocalDateTime start = parseStart(startDate, 1200); // Very old if null
        LocalDateTime end = parseEnd(endDate);
        
        return ResponseEntity.ok(adminReportService.getUserReports(start, end, community, gender, planType, status));
    }

    @GetMapping("/registrations")
    public ResponseEntity<Map<String, Object>> getRegistrationReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        
        return ResponseEntity.ok(Map.of(
            "monthlyRegistrations", userRepository.countByMonthBetween(start, end),
            "dailyRegistrations", userRepository.countByDayBetween(end.minusDays(30), end),
            "totalInRange", userRepository.countFiltered(start, end)
        ));
    }

    @GetMapping("/premium")
    public ResponseEntity<PremiumReportDTO> getPremiumReports() {
        return ResponseEntity.ok(adminReportService.getPremiumReports());
    }

    @GetMapping("/payments")
    public ResponseEntity<PaymentReportDTO> getPaymentReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(adminReportService.getPaymentReports(start, end));
    }

    @GetMapping("/businesses")
    public ResponseEntity<BusinessReportDTO> getBusinessReports() {
        return ResponseEntity.ok(adminReportService.getBusinessReports());
    }

    @GetMapping("/enquiries")
    public ResponseEntity<EnquiryReportDTO> getEnquiryReports() {
        return ResponseEntity.ok(adminReportService.getEnquiryReports());
    }

    @GetMapping("/chats")
    public ResponseEntity<ChatReportSummaryDTO> getChatReports() {
        return ResponseEntity.ok(adminReportService.getChatReports());
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<AuditLogReportDTO> getAuditLogReports(
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminReportService.getAuditLogReports(module, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"))));
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueReportDTO> getRevenueReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(adminReportService.getRevenueReports(start, end));
    }

    @GetMapping("/charts/user-growth")
    public ResponseEntity<List<Map<String, Object>>> getUserGrowthChart(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(userRepository.countByMonthBetween(start, end));
    }

    @GetMapping("/charts/revenue-growth")
    public ResponseEntity<List<Map<String, Object>>> getRevenueGrowthChart(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(paymentRepository.monthlyRevenue(start, end));
    }

    @GetMapping("/charts/premium-growth")
    public ResponseEntity<List<Map<String, Object>>> getPremiumGrowthChart(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(paymentRepository.premiumGrowth(start, end));
    }

    @GetMapping("/charts/business-growth")
    public ResponseEntity<List<Map<String, Object>>> getBusinessGrowthChart(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(businessRepository.countByMonthBetween(start, end));
    }

    @GetMapping("/charts/enquiry-trends")
    public ResponseEntity<List<Map<String, Object>>> getEnquiryTrendsChart(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(businessEnquiryRepository.countByMonthBetween(start, end));
    }

    @GetMapping("/charts/payment-trends")
    public ResponseEntity<List<Map<String, Object>>> getPaymentTrendsChart(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDateTime start = parseStart(startDate, 12);
        LocalDateTime end = parseEnd(endDate);
        return ResponseEntity.ok(paymentRepository.monthlyPaymentCount(start, end));
    }

    @GetMapping("/charts/daily-activity")
    public ResponseEntity<List<Map<String, Object>>> getDailyActivityChart() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return ResponseEntity.ok(userRepository.countByDayBetween(thirtyDaysAgo, LocalDateTime.now()));
    }

    private LocalDateTime parseStart(String date, int defaultMonthsBack) {
        if (date != null && !date.isBlank()) {
            return LocalDate.parse(date).atStartOfDay();
        }
        return LocalDateTime.now().minusMonths(defaultMonthsBack);
    }

    private LocalDateTime parseEnd(String date) {
        if (date != null && !date.isBlank()) {
            return LocalDate.parse(date).atTime(LocalTime.MAX);
        }
        return LocalDateTime.now();
    }
}
