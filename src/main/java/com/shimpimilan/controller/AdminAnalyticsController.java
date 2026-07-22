package com.shimpimilan.controller;

import com.shimpimilan.model.business.EnquiryStatus;
import com.shimpimilan.model.notification.NotificationStatus;
import com.shimpimilan.repository.*;
import com.shimpimilan.repository.business.BusinessEnquiryRepository;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.notification.NotificationQueueRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final InterestRepository interestRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatReportRepository chatReportRepository;
    private final NotificationQueueRepository notificationQueueRepository;
    private final BusinessRepository businessRepository;
    private final BusinessEnquiryRepository businessEnquiryRepository;
    private final PaymentRepository paymentRepository;
    private final ExportJobRepository exportJobRepository;
    private final MeterRegistry meterRegistry;

    // ─── Date Utility ───────────────────────────────────────────────────────────

    private LocalDateTime[] resolveDateRange(String filter) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = switch (filter == null ? "Last 30 Days" : filter) {
            case "Today"       -> LocalDateTime.now().toLocalDate().atStartOfDay();
            case "Yesterday"   -> LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
            case "Last 7 Days" -> end.minusDays(7);
            case "This Month"  -> LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
            case "Last Month"  -> LocalDateTime.now().minusMonths(1).withDayOfMonth(1).toLocalDate().atStartOfDay();
            case "This Year"   -> LocalDateTime.now().withDayOfYear(1).toLocalDate().atStartOfDay();
            default            -> end.minusDays(30); // "Last 30 Days" is default
        };
        return new LocalDateTime[]{ start, end };
    }

    // ─── User Analytics ──────────────────────────────────────────────────────────

    @GetMapping("/users/daily")
    public ResponseEntity<List<Map<String, Object>>> getUserDailyRegistrations(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(userRepository.countByDayBetween(range[0], range[1]));
    }

    @GetMapping("/users/community")
    public ResponseEntity<List<Map<String, Object>>> getUserCommunityDistribution(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(userRepository.countByCommunityAggregation(range[0], range[1]));
    }

    @GetMapping("/users/verification")
    public ResponseEntity<List<Map<String, Object>>> getUserVerificationStatus(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(profileRepository.countByVerificationStatusAggregation(range[0], range[1]));
    }

    @GetMapping("/users/gender")
    public ResponseEntity<List<Map<String, Object>>> getUserGenderDistribution(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(profileRepository.countByGenderAggregation(range[0], range[1]));
    }

    @GetMapping("/users/age")
    public ResponseEntity<List<Map<String, Object>>> getUserAgeDistribution(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(profileRepository.countByAgeGroupAggregation(range[0], range[1]));
    }

    // ─── Membership Analytics ────────────────────────────────────────────────────

    @GetMapping("/membership/distribution")
    public ResponseEntity<List<Map<String, Object>>> getMembershipDistribution(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(profileRepository.countByPlanTypeAggregation(range[0], range[1]));
    }

    // ─── Revenue Analytics ───────────────────────────────────────────────────────

    @GetMapping("/revenue/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenue(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(paymentRepository.dailyRevenue(range[0], range[1]));
    }

    // ─── Business Analytics ──────────────────────────────────────────────────────

    @GetMapping("/business/category")
    public ResponseEntity<List<Map<String, Object>>> getBusinessCategoryDistribution(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(businessRepository.countByCategoryAggregation(range[0], range[1]));
    }

    @GetMapping("/business/city")
    public ResponseEntity<List<Map<String, Object>>> getBusinessCityDistribution(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(businessRepository.countByCityAggregation(range[0], range[1]));
    }

    // ─── Matrimonial Analytics ───────────────────────────────────────────────────

    @GetMapping("/matrimonial/profile-status")
    public ResponseEntity<List<Map<String, Object>>> getProfileStatusDistribution(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(profileRepository.countByVerificationStatusAggregation(range[0], range[1]));
    }

    @GetMapping("/matrimonial/interests")
    public ResponseEntity<List<Map<String, Object>>> getInterestTrend(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);
        return ResponseEntity.ok(interestRepository.countByMonthBetween(range[0], range[1]));
    }

    // ─── CRM Analytics ───────────────────────────────────────────────────────────

    @GetMapping("/crm/pipeline")
    public ResponseEntity<List<Map<String, Object>>> getCrmPipeline(
            @RequestParam(required = false) String filter) {
        List<Map<String, Object>> pipeline = new ArrayList<>();
        for (EnquiryStatus status : EnquiryStatus.values()) {
            pipeline.add(Map.of("stage", status.name(), "count", businessEnquiryRepository.countByStatus(status)));
        }
        return ResponseEntity.ok(pipeline);
    }

    // ─── Chat Analytics ──────────────────────────────────────────────────────────

    @GetMapping("/chat/summary")
    public ResponseEntity<Map<String, Object>> getChatSummary(
            @RequestParam(required = false) String filter) {
        var range = resolveDateRange(filter);

        List<Map<String, Object>> dailyMessages = chatMessageRepository.countByDayBetween(range[0], range[1]);

        List<Map<String, Object>> moderation = new ArrayList<>();
        moderation.add(Map.of("action", "Reported", "count", chatReportRepository.count()));
        moderation.add(Map.of("action", "Deleted", "count", chatMessageRepository.countByIsDeleted(true)));

        return ResponseEntity.ok(Map.of("daily", dailyMessages, "moderation", moderation));
    }

    // ─── Notification Analytics ──────────────────────────────────────────────────

    @GetMapping("/notifications/summary")
    public ResponseEntity<Map<String, Object>> getNotificationSummary(
            @RequestParam(required = false) String filter) {

        long sent    = notificationQueueRepository.countByStatus(NotificationStatus.SENT);
        long failed  = notificationQueueRepository.countByStatus(NotificationStatus.FAILED);
        long pending = notificationQueueRepository.countByStatus(NotificationStatus.PENDING);
        long total   = sent + failed + pending;

        List<Map<String, Object>> delivery = List.of(
            Map.of("status", "Sent",    "count", sent),
            Map.of("status", "Failed",  "count", failed),
            Map.of("status", "Pending", "count", pending)
        );

        List<Map<String, Object>> queue = List.of(
            Map.of("metric", "Total",    "value", total),
            Map.of("metric", "Sent",     "value", sent),
            Map.of("metric", "Failed",   "value", failed),
            Map.of("metric", "Pending",  "value", pending)
        );

        double successRate = total > 0 ? (sent * 100.0 / total) : 0.0;

        return ResponseEntity.ok(Map.of(
            "delivery", delivery,
            "queue", queue,
            "successRate", successRate
        ));
    }

    // ─── System Analytics (JVM + Optional OS) ───────────────────────────────────

    @GetMapping("/system/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        MemoryMXBean memBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long heapUsed    = memBean.getHeapMemoryUsage().getUsed();
        long heapMax     = memBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memBean.getNonHeapMemoryUsage().getUsed();
        int  threads     = threadBean.getThreadCount();

        long exportQueueSize = exportJobRepository.countByStatusIn(
            List.of(com.shimpimilan.model.ExportJobStatus.QUEUED, com.shimpimilan.model.ExportJobStatus.PROCESSING)
        );

        // Gather Hikari pool size via Micrometer
        double dbConnections = 0;
        try {
            dbConnections = meterRegistry.get("hikaricp.connections.active").gauge().value();
        } catch (Exception ignored) {}

        // HTTP request count and error rate from Micrometer
        double httpRequestRate = 0;
        double errorRate = 0;
        try {
            httpRequestRate = meterRegistry.get("http.server.requests").timer().count();
        } catch (Exception ignored) {}

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("heapUsed",       heapUsed);
        result.put("heapMax",        heapMax);
        result.put("nonHeapUsed",    nonHeapUsed);
        result.put("activeThreads",  threads);
        result.put("dbConnections",  (long) dbConnections);
        result.put("exportQueueSize", exportQueueSize);
        result.put("httpRequestRate", httpRequestRate);
        result.put("errorRate",      errorRate);

        // Optional OS metrics via OperatingSystem MBean
        try {
            com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = osBean.getCpuLoad() * 100;
            long freeRam   = osBean.getFreeMemorySize();
            long totalRam  = osBean.getTotalMemorySize();
            long usedRam   = (totalRam - freeRam) / (1024 * 1024);
            result.put("cpuUsage",  cpuLoad);
            result.put("ramUsedMb", usedRam);
        } catch (Exception ignored) {
            // Gracefully omit OS metrics if unavailable
            result.put("cpuUsage",  null);
            result.put("ramUsedMb", null);
        }

        return ResponseEntity.ok(result);
    }
}
