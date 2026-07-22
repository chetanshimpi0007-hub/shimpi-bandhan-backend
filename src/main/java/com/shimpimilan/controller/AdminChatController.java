package com.shimpimilan.controller;

import com.shimpimilan.model.AuditLog;
import com.shimpimilan.model.ChatMessage;
import com.shimpimilan.model.ChatRoom;
import com.shimpimilan.model.ChatReport;
import com.shimpimilan.model.ReportStatus;
import com.shimpimilan.model.User;
import com.shimpimilan.model.UserStatus;
import com.shimpimilan.repository.AuditLogRepository;
import com.shimpimilan.repository.ChatMessageRepository;
import com.shimpimilan.repository.ChatRoomRepository;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.ChatReportRepository;
import com.shimpimilan.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.io.IOException;

// Apache POI and OpenPDF imports
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@RestController
@RequestMapping("/api/v1/admin/chats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ChatReportRepository chatReportRepository;

    @GetMapping
    public ResponseEntity<Page<ChatRoom>> getAllChatRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long roomId) {
        return ResponseEntity.ok(chatRoomRepository.findWithFilters(search, roomId,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"))));
    }

    @GetMapping("/stats")
    public ResponseEntity<java.util.Map<String, Object>> getChatStats() {
        long totalRooms = chatRoomRepository.count();
        long activeRooms = totalRooms; // Simplification, could be based on recent messages
        long deletedMessages = chatMessageRepository.countByIsDeleted(true);
        long reportedMessages = chatReportRepository.count();
        long blockedUsers = userRepository.countByStatus(UserStatus.BLOCKED);
        long actionsToday = auditLogRepository.countByModuleAndTimestampAfter(
                "CHAT_MODERATION", LocalDateTime.now().with(LocalTime.MIN));

        return ResponseEntity.ok(java.util.Map.of(
            "totalConversations", totalRooms,
            "activeConversations", activeRooms,
            "deletedMessages", deletedMessages,
            "reportedMessages", reportedMessages,
            "blockedUsers", blockedUsers,
            "moderationActionsToday", actionsToday
        ));
    }

    @GetMapping("/{chatRoomId}/messages")
    public ResponseEntity<Page<ChatMessage>> getChatMessages(
            @PathVariable Long chatRoomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Boolean isDeleted) {
        
        return ResponseEntity.ok(chatMessageRepository.findWithFilters(
                chatRoomId, content, startDate, endDate, isDeleted,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"))));
    }

    @DeleteMapping("/rooms/{chatRoomId}")
    public ResponseEntity<String> deleteChatRoom(@PathVariable Long chatRoomId,
                                                  @AuthenticationPrincipal CustomUserDetails adminDetails) {
        ChatRoom room = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));
        chatMessageRepository.deleteByChatRoomId(chatRoomId);
        chatRoomRepository.deleteById(chatRoomId);
        logAction("DELETE_CHAT_ROOM", "ChatRoom #" + chatRoomId, "DELETED", adminDetails);
        return ResponseEntity.ok("Chat room deleted");
    }

    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<String> deleteMessage(@PathVariable Long messageId,
                                                @AuthenticationPrincipal CustomUserDetails adminDetails) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        msg.setDeleted(true);
        chatMessageRepository.save(msg);
        logAction("DELETE_CHAT_MESSAGE", "Message #" + messageId, "SOFT_DELETED", adminDetails);
        return ResponseEntity.ok("Message soft deleted");
    }

    @PutMapping("/messages/{messageId}/restore")
    public ResponseEntity<String> restoreMessage(@PathVariable Long messageId,
                                                 @AuthenticationPrincipal CustomUserDetails adminDetails) {
        ChatMessage msg = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        msg.setDeleted(false);
        chatMessageRepository.save(msg);
        logAction("RESTORE_CHAT_MESSAGE", "Message #" + messageId, "RESTORED", adminDetails);
        return ResponseEntity.ok("Message restored");
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<String> blockUserFromChat(@PathVariable Long userId,
                                                    @AuthenticationPrincipal CustomUserDetails adminDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
        logAction("BLOCK_USER", "User #" + userId, "BLOCKED", adminDetails);
        return ResponseEntity.ok("User blocked");
    }
    
    @PutMapping("/users/{userId}/warn")
    public ResponseEntity<String> warnUser(@PathVariable Long userId,
                                           @AuthenticationPrincipal CustomUserDetails adminDetails) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        // Could send email/notification here
        logAction("WARN_USER", "User #" + userId, "WARNED", adminDetails);
        return ResponseEntity.ok("User warned");
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<ChatReport>> getReportQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReportStatus status) {
        if (status != null) {
            return ResponseEntity.ok(chatReportRepository.findByStatus(status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reportDate"))));
        }
        return ResponseEntity.ok(chatReportRepository.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "reportDate"))));
    }

    @PutMapping("/reports/{reportId}/close")
    public ResponseEntity<String> closeReport(@PathVariable Long reportId,
                                              @RequestParam ReportStatus status,
                                              @AuthenticationPrincipal CustomUserDetails adminDetails) {
        ChatReport report = chatReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(status);
        chatReportRepository.save(report);
        logAction("CLOSE_REPORT", "Report #" + reportId, status.name(), adminDetails);
        return ResponseEntity.ok("Report closed with status " + status);
    }

    private void logAction(String action, String oldValue, String newValue, CustomUserDetails adminDetails) {
        auditLogRepository.save(AuditLog.builder()
                .action(action)
                .module("CHAT_MODERATION")
                .oldValue(oldValue)
                .newValue(newValue)
                .adminName(adminDetails != null ? adminDetails.getUser().getPhone() : "SYSTEM")
                .timestamp(LocalDateTime.now())
                .build());
    }
    
    // Exports
    @GetMapping("/export/csv")
    public void exportToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"chat_moderation_logs.csv\"");
        
        List<AuditLog> logs = auditLogRepository.findAll();
        StringBuilder csv = new StringBuilder("ID,Action,Module,OldValue,NewValue,AdminName,Timestamp\n");
        for (AuditLog log : logs) {
            if ("CHAT_MODERATION".equals(log.getModule())) {
                csv.append(log.getId()).append(",")
                   .append(log.getAction()).append(",")
                   .append(log.getModule()).append(",")
                   .append(escapeCsv(log.getOldValue())).append(",")
                   .append(escapeCsv(log.getNewValue())).append(",")
                   .append(escapeCsv(log.getAdminName())).append(",")
                   .append(log.getTimestamp()).append("\n");
            }
        }
        response.getWriter().write(csv.toString());
    }
    
    private String escapeCsv(String val) {
        if (val == null) return "";
        if (val.contains(",")) return "\"" + val + "\"";
        return val;
    }

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=\"chat_moderation_logs.xlsx\"");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Moderation Logs");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Action");
            header.createCell(2).setCellValue("Module");
            header.createCell(3).setCellValue("Old Value");
            header.createCell(4).setCellValue("New Value");
            header.createCell(5).setCellValue("Admin Name");
            header.createCell(6).setCellValue("Timestamp");
            
            List<AuditLog> logs = auditLogRepository.findAll();
            int rowIdx = 1;
            for (AuditLog log : logs) {
                if ("CHAT_MODERATION".equals(log.getModule())) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(log.getId());
                    row.createCell(1).setCellValue(log.getAction());
                    row.createCell(2).setCellValue(log.getModule());
                    row.createCell(3).setCellValue(log.getOldValue() != null ? log.getOldValue() : "");
                    row.createCell(4).setCellValue(log.getNewValue() != null ? log.getNewValue() : "");
                    row.createCell(5).setCellValue(log.getAdminName() != null ? log.getAdminName() : "");
                    row.createCell(6).setCellValue(log.getTimestamp() != null ? log.getTimestamp().toString() : "");
                }
            }
            workbook.write(response.getOutputStream());
        }
    }

    @GetMapping("/export/pdf")
    public void exportToPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"chat_moderation_logs.pdf\"");
        
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();
            document.add(new Paragraph("Chat Moderation Logs"));
            
            List<AuditLog> logs = auditLogRepository.findAll();
            for (AuditLog log : logs) {
                if ("CHAT_MODERATION".equals(log.getModule())) {
                    document.add(new Paragraph(
                        String.format("ID: %d | Action: %s | Admin: %s | Time: %s",
                                log.getId(), log.getAction(), log.getAdminName(), log.getTimestamp())
                    ));
                }
            }
        } catch (Exception e) {
            throw new IOException("Failed to generate PDF", e);
        }
    }
}
