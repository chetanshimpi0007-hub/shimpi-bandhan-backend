package com.shimpimilan.service;

import com.shimpimilan.model.*;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.repository.*;
import com.shimpimilan.repository.business.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * UTF-8 CSV export service.
 * Writes a BOM (0xEF 0xBB 0xBF) so that Excel / LibreOffice auto-detects UTF-8
 * and renders Marathi / Unicode characters correctly without manual import steps.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CsvExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int BATCH_SIZE = 1000;
    // UTF-8 BOM
    private static final byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final AuditLogRepository auditLogRepository;

    // ============================================================
    //  Users CSV
    // ============================================================
    @Transactional(readOnly = true)
    public void exportUsersToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(BOM);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));

            writeMeta(writer, "SHIMPIMILAN - Users Report", generatedByName, filters);
            writer.write(csvRow("ID", "Phone", "Community", "Role", "Status", "Plan Type",
                    "Full Name", "Email", "City", "State", "Gender", "Verification", "Registered At", "Last Login"));

            int page = 0;
            List<User> batch;
            int count = 0;
            do {
                batch = userRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (User u : batch) {
                    String plan = (u.getProfile() != null && u.getProfile().getPlanType() != null)
                            ? u.getProfile().getPlanType().name() : "FREE";
                    String name = (u.getProfile() != null && u.getProfile().getFullName() != null) ? u.getProfile().getFullName() : "";
                    String email = (u.getProfile() != null && u.getProfile().getEmail() != null) ? u.getProfile().getEmail() : "";
                    String city = (u.getProfile() != null && u.getProfile().getCity() != null) ? u.getProfile().getCity() : "";
                    String state = (u.getProfile() != null && u.getProfile().getState() != null) ? u.getProfile().getState() : "";
                    String gender = (u.getProfile() != null && u.getProfile().getGender() != null) ? u.getProfile().getGender().name() : "";
                    String verification = (u.getProfile() != null && u.getProfile().getVerificationStatus() != null) ? u.getProfile().getVerificationStatus().name() : "";
                    writer.write(csvRow(
                            str(u.getId()),
                            nvl(u.getPhone()),
                            u.getCommunity() != null ? u.getCommunity().name() : "",
                            u.getRole() != null ? u.getRole().name() : "",
                            u.getStatus() != null ? u.getStatus().name() : "",
                            plan, name, email, city, state, gender, verification,
                            u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : "",
                            u.getLastLoginDate() != null ? u.getLastLoginDate().format(FMT) : ""
                    ));
                    count++;
                }
                writer.flush();
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);
            log.info("CSV users export: {} rows -> {}", count, filePath);
        }
    }

    // ============================================================
    //  Payments CSV
    // ============================================================
    @Transactional(readOnly = true)
    public void exportPaymentsToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(BOM);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writeMeta(writer, "SHIMPIMILAN - Payments Report", generatedByName, filters);
            writer.write(csvRow("ID", "User Phone", "Order ID", "Payment ID",
                    "Amount (Rs)", "Final Paid (Rs)", "Discount (Rs)", "Status", "Method", "Plan", "Created At"));

            int page = 0;
            List<Payment> batch;
            do {
                batch = paymentRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (Payment p : batch) {
                    writer.write(csvRow(
                            str(p.getId()),
                            p.getUser() != null ? nvl(p.getUser().getPhone()) : "",
                            nvl(p.getRazorpayOrderId()), nvl(p.getRazorpayPaymentId()),
                            str(p.getAmount()), str(p.getFinalAmountPaid()), str(p.getDiscountApplied()),
                            p.getStatus() != null ? p.getStatus().name() : "",
                            nvl(p.getPaymentMethod()),
                            (p.getSubscription() != null && p.getSubscription().getPlanType() != null) ? p.getSubscription().getPlanType().name() : "",
                            p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : ""
                    ));
                }
                writer.flush();
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);
        }
    }

    // ============================================================
    //  Businesses CSV
    // ============================================================
    @Transactional(readOnly = true)
    public void exportBusinessesToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(BOM);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writeMeta(writer, "SHIMPIMILAN - Business Directory Report", generatedByName, filters);
            writer.write(csvRow("ID", "Business Name", "Owner", "Mobile", "Email",
                    "City", "State", "Plan", "Status", "GST", "Verified", "Featured", "Created At"));

            int page = 0;
            List<Business> batch;
            do {
                batch = businessRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (Business b : batch) {
                    writer.write(csvRow(
                            str(b.getId()), nvl(b.getBusinessName()), nvl(b.getOwnerName()),
                            nvl(b.getMobileNumber()), nvl(b.getEmail()), nvl(b.getCity()), nvl(b.getState()),
                            b.getPlanType() != null ? b.getPlanType().name() : "",
                            b.getStatus() != null ? b.getStatus().name() : "",
                            nvl(b.getGstNumber()),
                            Boolean.TRUE.equals(b.getIsVerified()) ? "Yes" : "No",
                            Boolean.TRUE.equals(b.getIsAdminFeatured()) ? "Yes" : "No",
                            b.getCreatedAt() != null ? b.getCreatedAt().format(FMT) : ""
                    ));
                }
                writer.flush();
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);
        }
    }

    // ============================================================
    //  Audit Logs CSV
    // ============================================================
    @Transactional(readOnly = true)
    public void exportAuditLogsToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(BOM);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8));
            writeMeta(writer, "SHIMPIMILAN - Audit Log Report", generatedByName, filters);
            writer.write(csvRow("ID", "Admin Name", "User ID", "Action", "Module",
                    "Details", "IP Address", "Browser", "Device", "Timestamp"));

            int page = 0;
            List<AuditLog> batch;
            do {
                batch = auditLogRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (AuditLog a : batch) {
                    writer.write(csvRow(
                            str(a.getId()), nvl(a.getAdminName()), str(a.getUserId()),
                            nvl(a.getAction()), nvl(a.getModule()), nvl(a.getDetails()),
                            nvl(a.getIpAddress()), nvl(a.getBrowser()), nvl(a.getDevice()),
                            a.getTimestamp() != null ? a.getTimestamp().format(FMT) : ""
                    ));
                }
                writer.flush();
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);
        }
    }

    // ============================================================
    //  Utilities
    // ============================================================
    private void writeMeta(BufferedWriter writer, String title, String generatedBy, Map<String, String> filters) throws IOException {
        writer.write(escape(title) + "\n");
        writer.write("Generated By: " + escape(generatedBy) + "\n");
        writer.write("Generated At: " + java.time.LocalDateTime.now().format(FMT) + "\n");
        if (filters != null && !filters.isEmpty()) {
            writer.write("Filters: " + escape(filters.toString()) + "\n");
        }
        writer.write("\n");
    }

    /** Produce a CSV row with proper escaping. */
    private String csvRow(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escape(values[i]));
        }
        sb.append("\r\n");
        return sb.toString();
    }

    /** RFC 4180: surround with quotes if contains comma, quote, or newline. */
    private String escape(String val) {
        if (val == null) return "";
        if (val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private String nvl(String s) { return s != null ? s : ""; }
    private String str(Object o) { return o != null ? o.toString() : ""; }
}
