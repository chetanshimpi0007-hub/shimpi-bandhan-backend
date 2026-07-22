package com.shimpimilan.service;

import com.shimpimilan.model.*;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.repository.*;
import com.shimpimilan.repository.business.BusinessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int BATCH_SIZE = 500;

    // Brand colours
    private static final byte[] HEADER_BG  = {(byte)30,  (byte)64,  (byte)175}; // deep blue
    private static final byte[] ALT_ROW_BG = {(byte)239, (byte)246, (byte)255}; // light blue tint

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final AuditLogRepository auditLogRepository;

    // ====================================================================
    //  Users Export
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportUsersToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(BATCH_SIZE);
        workbook.setCompressTempFiles(true);

        Sheet sheet = workbook.createSheet("Users");
        sheet.createFreezePane(0, 2); // freeze top 2 rows (title + header)

        // Styles
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle altStyle = createAltRowStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);

        // Row 0: Report title spanning all columns
        String[] cols = {"ID", "Phone", "Community", "Role", "Status", "Plan Type", "Full Name", "Email",
                "City", "State", "Gender", "Verification", "Registered At", "Last Login"};
        createTitleRow(sheet, titleStyle, "SHIMPI BANDHAN – Users Report", cols.length,
                generatedByName, filters);

        // Row 1: Headers
        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(22);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.setAutoFilter(new CellRangeAddress(1, 1, 0, cols.length - 1));

        // Data rows streamed in batches
        int rowNum = 2;
        int page = 0;
        List<User> batch;
        do {
            batch = userRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
            for (User u : batch) {
                Row row = sheet.createRow(rowNum);
                CellStyle rowStyle  = (rowNum % 2 == 0) ? normalStyle : altStyle;
                CellStyle dStyle = cloneDateStyle(workbook, (rowNum % 2 == 0) ? false : true);

                setCellValue(row, 0, u.getId(), rowStyle);
                setCellValue(row, 1, u.getPhone(), rowStyle);
                setCellValue(row, 2, u.getCommunity() != null ? u.getCommunity().name() : "", rowStyle);
                setCellValue(row, 3, u.getRole() != null ? u.getRole().name() : "", rowStyle);
                setCellValue(row, 4, u.getStatus() != null ? u.getStatus().name() : "", rowStyle);
                String plan = (u.getProfile() != null && u.getProfile().getPlanType() != null)
                        ? u.getProfile().getPlanType().name() : "FREE";
                setCellValue(row, 5, plan, rowStyle);
                String name = (u.getProfile() != null) ? u.getProfile().getFullName() : "";
                setCellValue(row, 6, name != null ? name : "", rowStyle);
                String email = (u.getProfile() != null) ? u.getProfile().getEmail() : "";
                setCellValue(row, 7, email != null ? email : "", rowStyle);
                String city = (u.getProfile() != null) ? u.getProfile().getCity() : "";
                setCellValue(row, 8, city != null ? city : "", rowStyle);
                String state = (u.getProfile() != null) ? u.getProfile().getState() : "";
                setCellValue(row, 9, state != null ? state : "", rowStyle);
                String gender = (u.getProfile() != null && u.getProfile().getGender() != null)
                        ? u.getProfile().getGender().name() : "";
                setCellValue(row, 10, gender, rowStyle);
                String verification = (u.getProfile() != null && u.getProfile().getVerificationStatus() != null)
                        ? u.getProfile().getVerificationStatus().name() : "";
                setCellValue(row, 11, verification, rowStyle);
                setCellValue(row, 12, u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FMT) : "", rowStyle);
                setCellValue(row, 13, u.getLastLoginDate() != null ? u.getLastLoginDate().format(DATE_FMT) : "", rowStyle);
                rowNum++;
            }
        } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

        // Auto-size sampled columns (SXSSFWorkbook tracks, then flushes)
        workbook.setForceFormulaRecalculation(true);
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } finally {
            workbook.dispose();
        }
        log.info("Excel users export written to {}, total rows: {}", filePath, rowNum - 2);
    }

    // ====================================================================
    //  Payments Export
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportPaymentsToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(BATCH_SIZE);
        workbook.setCompressTempFiles(true);
        Sheet sheet = workbook.createSheet("Payments");
        sheet.createFreezePane(0, 2);

        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle altStyle = createAltRowStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);

        String[] cols = {"ID", "User Phone", "Razorpay Order ID", "Razorpay Payment ID",
                "Amount (₹)", "Final Paid (₹)", "Discount (₹)", "Status", "Method", "Plan Type", "Created At"};
        createTitleRow(sheet, titleStyle, "SHIMPI BANDHAN – Payments Report", cols.length, generatedByName, filters);

        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(22);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.setAutoFilter(new CellRangeAddress(1, 1, 0, cols.length - 1));

        int rowNum = 2;
        int page = 0;
        List<Payment> batch;
        do {
            batch = paymentRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
            for (Payment p : batch) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? normalStyle : altStyle;
                setCellValue(row, 0, p.getId(), style);
                setCellValue(row, 1, p.getUser() != null ? p.getUser().getPhone() : "", style);
                setCellValue(row, 2, nvl(p.getRazorpayOrderId()), style);
                setCellValue(row, 3, nvl(p.getRazorpayPaymentId()), style);
                setCellNumeric(row, 4, p.getAmount() != null ? p.getAmount() : 0.0, style);
                setCellNumeric(row, 5, p.getFinalAmountPaid() != null ? p.getFinalAmountPaid() : 0.0, style);
                setCellNumeric(row, 6, p.getDiscountApplied() != null ? p.getDiscountApplied() : 0.0, style);
                setCellValue(row, 7, p.getStatus() != null ? p.getStatus().name() : "", style);
                setCellValue(row, 8, nvl(p.getPaymentMethod()), style);
                setCellValue(row, 9, (p.getSubscription() != null && p.getSubscription().getPlanType() != null) ? p.getSubscription().getPlanType().name() : "", style);
                setCellValue(row, 10, p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "", style);
                rowNum++;
            }
        } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } finally {
            workbook.dispose();
        }
        log.info("Excel payments export written to {}, rows: {}", filePath, rowNum - 2);
    }

    // ====================================================================
    //  Businesses Export
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportBusinessesToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(BATCH_SIZE);
        workbook.setCompressTempFiles(true);
        Sheet sheet = workbook.createSheet("Businesses");
        sheet.createFreezePane(0, 2);

        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle altStyle = createAltRowStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);

        String[] cols = {"ID", "Business Name", "Owner", "Mobile", "Email", "City", "State",
                "Plan", "Status", "GST", "Verified", "Featured", "Created At"};
        createTitleRow(sheet, titleStyle, "SHIMPI BANDHAN – Business Directory Report", cols.length, generatedByName, filters);

        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(22);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.setAutoFilter(new CellRangeAddress(1, 1, 0, cols.length - 1));

        int rowNum = 2;
        int page = 0;
        List<Business> batch;
        do {
            batch = businessRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
            for (Business b : batch) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? normalStyle : altStyle;
                setCellValue(row, 0, b.getId(), style);
                setCellValue(row, 1, nvl(b.getBusinessName()), style);
                setCellValue(row, 2, nvl(b.getOwnerName()), style);
                setCellValue(row, 3, nvl(b.getMobileNumber()), style);
                setCellValue(row, 4, nvl(b.getEmail()), style);
                setCellValue(row, 5, nvl(b.getCity()), style);
                setCellValue(row, 6, nvl(b.getState()), style);
                setCellValue(row, 7, b.getPlanType() != null ? b.getPlanType().name() : "", style);
                setCellValue(row, 8, b.getStatus() != null ? b.getStatus().name() : "", style);
                setCellValue(row, 9, nvl(b.getGstNumber()), style);
                setCellValue(row, 10, Boolean.TRUE.equals(b.getIsVerified()) ? "Yes" : "No", style);
                setCellValue(row, 11, Boolean.TRUE.equals(b.getIsAdminFeatured()) ? "Yes" : "No", style);
                setCellValue(row, 12, b.getCreatedAt() != null ? b.getCreatedAt().format(DATE_FMT) : "", style);
                rowNum++;
            }
        } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } finally {
            workbook.dispose();
        }
        log.info("Excel businesses export written to {}, rows: {}", filePath, rowNum - 2);
    }

    // ====================================================================
    //  Audit Logs Export
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportAuditLogsToFile(String filePath, Map<String, String> filters, String generatedByName) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook(BATCH_SIZE);
        workbook.setCompressTempFiles(true);
        Sheet sheet = workbook.createSheet("Audit Logs");
        sheet.createFreezePane(0, 2);

        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle altStyle = createAltRowStyle(workbook);
        CellStyle normalStyle = createNormalStyle(workbook);

        String[] cols = {"ID", "Admin Name", "User ID", "Action", "Module", "Details",
                "IP Address", "Browser", "Device", "Timestamp"};
        createTitleRow(sheet, titleStyle, "SHIMPI BANDHAN – Audit Log Report", cols.length, generatedByName, filters);

        Row headerRow = sheet.createRow(1);
        headerRow.setHeightInPoints(22);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }
        sheet.setAutoFilter(new CellRangeAddress(1, 1, 0, cols.length - 1));

        int rowNum = 2;
        int page = 0;
        List<AuditLog> batch;
        do {
            batch = auditLogRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
            for (AuditLog log2 : batch) {
                Row row = sheet.createRow(rowNum);
                CellStyle style = (rowNum % 2 == 0) ? normalStyle : altStyle;
                setCellValue(row, 0, log2.getId(), style);
                setCellValue(row, 1, nvl(log2.getAdminName()), style);
                setCellValue(row, 2, log2.getUserId() != null ? log2.getUserId() : 0L, style);
                setCellValue(row, 3, nvl(log2.getAction()), style);
                setCellValue(row, 4, nvl(log2.getModule()), style);
                setCellValue(row, 5, nvl(log2.getDetails()), style);
                setCellValue(row, 6, nvl(log2.getIpAddress()), style);
                setCellValue(row, 7, nvl(log2.getBrowser()), style);
                setCellValue(row, 8, nvl(log2.getDevice()), style);
                setCellValue(row, 9, log2.getTimestamp() != null ? log2.getTimestamp().format(DATE_FMT) : "", style);
                rowNum++;
            }
        } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } finally {
            workbook.dispose();
        }
        log.info("Excel audit log export written to {}, rows: {}", filePath, rowNum - 2);
    }

    // ====================================================================
    //  Style helpers
    // ====================================================================
    private void createTitleRow(Sheet sheet, CellStyle style, String title, int colCount,
                                String generatedBy, Map<String, String> filters) {
        // Row 0: merged title
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        String filterStr = filters != null && !filters.isEmpty() ? "  |  Filters: " + filters.toString() : "";
        String meta = "  |  Generated By: " + generatedBy + "  |  Date: " + LocalDateTime.now().format(DATE_FMT);
        titleCell.setCellValue(title + filterStr + meta);
        titleCell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, colCount - 1));
    }

    private CellStyle createTitleStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.WHITE.getIndex());
        style.setWrapText(false);
        return style;
    }

    private CellStyle createNormalStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private CellStyle createAltRowStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.HAIR);
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return style;
    }

    private CellStyle createDateStyle(SXSSFWorkbook wb) {
        CellStyle style = wb.createCellStyle();
        CreationHelper ch = wb.getCreationHelper();
        style.setDataFormat(ch.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private CellStyle cloneDateStyle(SXSSFWorkbook wb, boolean alt) {
        CellStyle style = alt ? createAltRowStyle(wb) : createNormalStyle(wb);
        CreationHelper ch = wb.getCreationHelper();
        style.setDataFormat(ch.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
        return style;
    }

    private void setCellValue(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Long) {
            cell.setCellValue((Long) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }

    private void setCellNumeric(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private String nvl(String val) {
        return val != null ? val : "";
    }
}
