package com.shimpimilan.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.shimpimilan.model.*;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.photo.ProfilePhoto;
import com.shimpimilan.repository.*;
import com.shimpimilan.repository.business.BusinessRepository;
import com.shimpimilan.repository.ProfilePhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfExportService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int BATCH_SIZE = 500;

    // Brand colours
    private static final Color BRAND_BLUE  = new Color(30, 64, 175);
    private static final Color BRAND_LIGHT = new Color(219, 234, 254);
    private static final Color HEADER_TXT  = Color.WHITE;
    private static final Color ROW_ALT     = new Color(239, 246, 255);

    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository profilePhotoRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final BusinessRepository businessRepository;
    private final AuditLogRepository auditLogRepository;

    // ====================================================================
    //  Per-user registration PDF (existing, enhanced)
    // ====================================================================
    public byte[] generateUserRegistrationPdf(Long userId) throws Exception {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Profile not found for user " + userId));
        User user = profile.getUser();
        List<ProfilePhoto> photos = profilePhotoRepository.findByUserIdAndStatus(userId, PhotoStatus.APPROVED);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 60);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        writer.setPageEvent(new BrandedPageHelper("SHIMPI BANDHAN", user.getProfile() != null ? user.getProfile().getFullName() : ""));

        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BRAND_BLUE);
        Font headFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, BRAND_BLUE);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

        // Title block
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{80, 20});
        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.addElement(new Paragraph("SHIMPI BANDHAN – Registration Form", titleFont));
        titleCell.addElement(new Paragraph("Official Platform for Shimpi Matrimonial",
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY)));
        titleCell.addElement(new Paragraph("www.shimpibandhan.com  |  Profile ID: " + profile.getId(),
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY)));
        headerTable.addCell(titleCell);

        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(Rectangle.NO_BORDER);
        qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Image qr = generateQrCodeImage("https://shimpibandhan.com/profile/" + profile.getId());
        if (qr != null) { qrCell.addElement(qr); }
        headerTable.addCell(qrCell);
        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // Sections
        addSection(document, "Registration Details", headFont);
        addRow(document, "Registration Date:", fmt(profile.getCreatedAt()), labelFont, valueFont);
        addRow(document, "Membership Plan:", str(profile.getPlanType()), labelFont, valueFont);
        addRow(document, "Verification Status:", str(profile.getVerificationStatus()), labelFont, valueFont);
        document.add(Chunk.NEWLINE);

        if (!photos.isEmpty()) {
            addSection(document, "Profile & Gallery Photos", headFont);
            PdfPTable photoTable = new PdfPTable(3);
            photoTable.setWidthPercentage(100);
            for (ProfilePhoto p : photos) {
                PdfPCell pCell = new PdfPCell();
                pCell.setBorder(Rectangle.NO_BORDER);
                pCell.setPadding(5);
                try {
                    String path = p.getPhotoUrl().startsWith("/") ? p.getPhotoUrl().substring(1) : p.getPhotoUrl();
                    Image img = Image.getInstance(path);
                    img.scaleToFit(120, 120);
                    pCell.addElement(img);
                } catch (Exception ignored) {
                    pCell.addElement(new Paragraph("[Image not found]"));
                }
                photoTable.addCell(pCell);
            }
            photoTable.completeRow();
            document.add(photoTable);
            document.add(Chunk.NEWLINE);
        }

        addSection(document, "Personal Details", headFont);
        addRow(document, "Full Name:", str(profile.getFullName()), labelFont, valueFont);
        addRow(document, "Email:", str(profile.getEmail()), labelFont, valueFont);
        addRow(document, "Mobile:", str(user.getPhone()), labelFont, valueFont);
        addRow(document, "Gender:", str(profile.getGender()), labelFont, valueFont);
        addRow(document, "Date of Birth:", profile.getDateOfBirth() != null ? profile.getDateOfBirth().toString() : "N/A", labelFont, valueFont);
        addRow(document, "Blood Group:", str(profile.getBloodGroup()), labelFont, valueFont);
        addRow(document, "Community:", str(profile.getCommunity()), labelFont, valueFont);
        addRow(document, "Marital Status:", str(profile.getMaritalStatus()), labelFont, valueFont);
        document.add(Chunk.NEWLINE);

        addSection(document, "Address Details", headFont);
        addRow(document, "City:", str(profile.getCity()), labelFont, valueFont);
        addRow(document, "State:", str(profile.getState()), labelFont, valueFont);
        addRow(document, "Country:", str(profile.getCountry()), labelFont, valueFont);
        document.add(Chunk.NEWLINE);

        addSection(document, "Education & Career", headFont);
        addRow(document, "Education:", str(profile.getEducation()), labelFont, valueFont);
        addRow(document, "Occupation:", str(profile.getOccupation()), labelFont, valueFont);
        addRow(document, "Annual Income (₹):", profile.getAnnualIncome() != null ? String.valueOf(profile.getAnnualIncome()) : "N/A", labelFont, valueFont);
        document.add(Chunk.NEWLINE);

        document.close();
        return baos.toByteArray();
    }

    // ====================================================================
    //  Users list PDF report
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportUsersToPdf(String filePath, Map<String, String> filters, String generatedByName) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new BrandedPageHelper("SHIMPI BANDHAN", generatedByName));
            doc.open();

            addReportHeader(doc, "SHIMPI BANDHAN – Users Report", generatedByName, filters);

            String[] headers = {"ID", "Phone", "Community", "Status", "Plan", "Name", "City", "Registered"};
            float[] widths = {4f, 10f, 10f, 8f, 8f, 16f, 10f, 12f};
            PdfPTable table = createTable(headers, widths);

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            int page = 0;
            int rowNum = 0;
            List<User> batch;
            do {
                batch = userRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (User u : batch) {
                    Color bg = (rowNum++ % 2 == 0) ? Color.WHITE : ROW_ALT;
                    String plan = (u.getProfile() != null && u.getProfile().getPlanType() != null)
                            ? u.getProfile().getPlanType().name() : "FREE";
                    String name = (u.getProfile() != null && u.getProfile().getFullName() != null) ? u.getProfile().getFullName() : "";
                    String city = (u.getProfile() != null && u.getProfile().getCity() != null) ? u.getProfile().getCity() : "";
                    addTableRow(table, bg, cellFont,
                            str(u.getId()), nvl(u.getPhone()),
                            u.getCommunity() != null ? u.getCommunity().name() : "",
                            u.getStatus() != null ? u.getStatus().name() : "",
                            plan, name, city,
                            u.getCreatedAt() != null ? u.getCreatedAt().format(FMT) : "");
                }
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

            doc.add(table);
            doc.close();
        }
        log.info("PDF users report written to {}", filePath);
    }

    // ====================================================================
    //  Payments list PDF report
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportPaymentsToPdf(String filePath, Map<String, String> filters, String generatedByName) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new BrandedPageHelper("SHIMPI BANDHAN", generatedByName));
            doc.open();

            addReportHeader(doc, "SHIMPI BANDHAN – Payments Report", generatedByName, filters);

            String[] headers = {"ID", "User Phone", "Order ID", "Amount (₹)", "Final Paid (₹)", "Status", "Method", "Date"};
            float[] widths = {4f, 10f, 18f, 8f, 10f, 9f, 9f, 12f};
            PdfPTable table = createTable(headers, widths);

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            int page = 0;
            int rowNum = 0;
            List<Payment> batch;
            do {
                batch = paymentRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (Payment p : batch) {
                    Color bg = (rowNum++ % 2 == 0) ? Color.WHITE : ROW_ALT;
                    addTableRow(table, bg, cellFont,
                            str(p.getId()),
                            p.getUser() != null ? nvl(p.getUser().getPhone()) : "",
                            nvl(p.getRazorpayOrderId()),
                            str(p.getAmount()),
                            str(p.getFinalAmountPaid()),
                            p.getStatus() != null ? p.getStatus().name() : "",
                            nvl(p.getPaymentMethod()),
                            p.getCreatedAt() != null ? p.getCreatedAt().format(FMT) : "");
                }
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

            doc.add(table);
            doc.close();
        }
        log.info("PDF payments report written to {}", filePath);
    }

    // ====================================================================
    //  Businesses list PDF report
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportBusinessesToPdf(String filePath, Map<String, String> filters, String generatedByName) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new BrandedPageHelper("SHIMPI BANDHAN", generatedByName));
            doc.open();

            addReportHeader(doc, "SHIMPI BANDHAN – Business Directory Report", generatedByName, filters);

            String[] headers = {"ID", "Business Name", "Owner", "City", "State", "Plan", "Status", "Verified", "Created"};
            float[] widths = {4f, 18f, 12f, 9f, 9f, 9f, 9f, 7f, 11f};
            PdfPTable table = createTable(headers, widths);

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            int page = 0;
            int rowNum = 0;
            List<Business> batch;
            do {
                batch = businessRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (Business b : batch) {
                    Color bg = (rowNum++ % 2 == 0) ? Color.WHITE : ROW_ALT;
                    addTableRow(table, bg, cellFont,
                            str(b.getId()), nvl(b.getBusinessName()), nvl(b.getOwnerName()),
                            nvl(b.getCity()), nvl(b.getState()),
                            b.getPlanType() != null ? b.getPlanType().name() : "",
                            b.getStatus() != null ? b.getStatus().name() : "",
                            Boolean.TRUE.equals(b.getIsVerified()) ? "Yes" : "No",
                            b.getCreatedAt() != null ? b.getCreatedAt().format(FMT) : "");
                }
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

            doc.add(table);
            doc.close();
        }
        log.info("PDF businesses report written to {}", filePath);
    }

    // ====================================================================
    //  Audit Logs PDF report
    // ====================================================================
    @Transactional(readOnly = true)
    public void exportAuditLogsToPdf(String filePath, Map<String, String> filters, String generatedByName) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            Document doc = new Document(PageSize.A4.rotate(), 30, 30, 50, 50);
            PdfWriter writer = PdfWriter.getInstance(doc, fos);
            writer.setPageEvent(new BrandedPageHelper("SHIMPI BANDHAN", generatedByName));
            doc.open();

            addReportHeader(doc, "SHIMPI BANDHAN – Audit Log Report", generatedByName, filters);

            String[] headers = {"ID", "Admin", "Module", "Action", "IP Address", "Timestamp"};
            float[] widths = {5f, 14f, 14f, 30f, 14f, 14f};
            PdfPTable table = createTable(headers, widths);

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
            int page = 0;
            int rowNum = 0;
            List<AuditLog> batch;
            do {
                batch = auditLogRepository.findAll(PageRequest.of(page++, BATCH_SIZE)).getContent();
                for (AuditLog a : batch) {
                    Color bg = (rowNum++ % 2 == 0) ? Color.WHITE : ROW_ALT;
                    addTableRow(table, bg, cellFont,
                            str(a.getId()), nvl(a.getAdminName()), nvl(a.getModule()),
                            nvl(a.getAction()), nvl(a.getIpAddress()),
                            a.getTimestamp() != null ? a.getTimestamp().format(FMT) : "");
                }
            } while (!batch.isEmpty() && batch.size() == BATCH_SIZE);

            doc.add(table);
            doc.close();
        }
        log.info("PDF audit log report written to {}", filePath);
    }

    // ====================================================================
    //  Shared PDF helpers
    // ====================================================================
    private void addReportHeader(Document doc, String title, String generatedBy,
                                 Map<String, String> filters) throws Exception {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BRAND_BLUE);
        Font metaFont  = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

        Paragraph t = new Paragraph(title, titleFont);
        t.setSpacingAfter(4);
        doc.add(t);

        String filterStr = filters != null && !filters.isEmpty() ? "Filters: " + filters : "No Filters Applied";
        doc.add(new Paragraph(
                "Generated By: " + generatedBy + "  |  Date: " + LocalDateTime.now().format(FMT) + "  |  " + filterStr,
                metaFont));

        LineSeparator sep = new LineSeparator();
        sep.setLineColor(BRAND_BLUE);
        doc.add(new Chunk(sep));
        doc.add(Chunk.NEWLINE);
    }

    private PdfPTable createTable(String[] headers, float[] widths) throws Exception {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setWidths(widths);
        table.setHeaderRows(1);

        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, HEADER_TXT);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, hFont));
            cell.setBackgroundColor(BRAND_BLUE);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        return table;
    }

    private void addTableRow(PdfPTable table, Color bg, Font font, String... values) {
        for (String val : values) {
            PdfPCell cell = new PdfPCell(new Phrase(val != null ? val : "", font));
            cell.setBackgroundColor(bg);
            cell.setPadding(4);
            table.addCell(cell);
        }
    }

    private void addSection(Document doc, String title, Font font) throws Exception {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(4);
        p.setSpacingAfter(4);
        doc.add(p);
        LineSeparator line = new LineSeparator();
        line.setLineColor(BRAND_BLUE);
        doc.add(new Chunk(line));
        doc.add(Chunk.NEWLINE);
    }

    private void addRow(Document doc, String label, String value, Font lf, Font vf) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{35, 65});
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setBorder(Rectangle.NO_BORDER);
        lc.setPaddingBottom(4);
        PdfPCell vc = new PdfPCell(new Phrase(value, vf));
        vc.setBorder(Rectangle.NO_BORDER);
        vc.setPaddingBottom(4);
        table.addCell(lc);
        table.addCell(vc);
        doc.add(table);
    }

    private Image generateQrCodeImage(String url) {
        try {
            com.google.zxing.qrcode.QRCodeWriter qrWriter = new com.google.zxing.qrcode.QRCodeWriter();
            com.google.zxing.common.BitMatrix matrix = qrWriter.encode(url, com.google.zxing.BarcodeFormat.QR_CODE, 150, 150);
            java.awt.image.BufferedImage bi = com.google.zxing.client.j2se.MatrixToImageWriter.toBufferedImage(matrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bi, "png", baos);
            Image img = Image.getInstance(baos.toByteArray());
            img.scaleAbsolute(80, 80);
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    private String fmt(java.time.LocalDateTime dt) { return dt != null ? dt.format(FMT) : "N/A"; }
    private String str(Object o) { return o != null ? o.toString() : "N/A"; }
    private String nvl(String s) { return s != null ? s : ""; }

    // ====================================================================
    //  Branded page event helper (watermark + footer + page numbers)
    // ====================================================================
    static class BrandedPageHelper extends PdfPageEventHelper {
        private final String watermarkText;
        private final String generatedBy;

        BrandedPageHelper(String watermarkText, String generatedBy) {
            this.watermarkText = watermarkText;
            this.generatedBy = generatedBy;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContentUnder();

            // Watermark
            cb.saveState();
            Font wmFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 55, Font.BOLD,
                    new Color(200, 200, 200, 35));
            try {
                Phrase wm = new Phrase(watermarkText, wmFont);
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, wm,
                        document.getPageSize().getWidth() / 2,
                        document.getPageSize().getHeight() / 2, 45);
            } catch (Exception ignored) {}
            cb.restoreState();

            // Footer bar
            PdfContentByte overContent = writer.getDirectContent();
            overContent.saveState();
            overContent.setColorFill(new Color(30, 64, 175));
            overContent.rectangle(document.left(), document.bottom() - 20,
                    document.right() - document.left(), 16);
            overContent.fill();

            Font footFont = FontFactory.getFont(FontFactory.HELVETICA, 7, Color.WHITE);
            String footLeft = "Designed & Developed by ArnavInfoWeb  |  www.shimpibandhan.com";
            String footRight = "Generated By: " + generatedBy + "  |  Page " + writer.getPageNumber();
            try {
                ColumnText.showTextAligned(overContent, Element.ALIGN_LEFT,
                        new Phrase(footLeft, footFont), document.left(), document.bottom() - 14, 0);
                ColumnText.showTextAligned(overContent, Element.ALIGN_RIGHT,
                        new Phrase(footRight, footFont), document.right(), document.bottom() - 14, 0);
            } catch (Exception ignored) {}
            overContent.restoreState();
        }
    }
}
