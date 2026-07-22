package com.shimpimilan.controller;

import com.shimpimilan.dto.report.ExportJobDTO;
import com.shimpimilan.dto.report.ExportRequestDTO;
import com.shimpimilan.model.ExportJob;
import com.shimpimilan.security.CustomUserDetails;
import com.shimpimilan.service.AdminExportService;
import com.shimpimilan.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/export")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminExportController {

    private final AdminExportService adminExportService;
    private final PdfExportService pdfExportService; // Keeping for single user PDF if needed

    // ============================================================
    //  Existing Single PDF endpoint (Fast/Sync)
    // ============================================================
    @GetMapping("/users/{userId}/pdf")
    public ResponseEntity<byte[]> exportUserPdf(@PathVariable Long userId) throws Exception {
        byte[] pdfBytes = pdfExportService.generateUserRegistrationPdf(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "user_" + userId + "_registration.pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ============================================================
    //  New Async Export Endpoints
    // ============================================================

    /**
     * Queues a new export job and returns the job ID immediately.
     */
    @PostMapping("/queue")
    public ResponseEntity<ExportJobDTO> queueExport(
            @RequestBody ExportRequestDTO request,
            @AuthenticationPrincipal CustomUserDetails currentUser,
            HttpServletRequest httpRequest) {

        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        String adminName = "Admin (" + currentUser.getUser().getPhone() + ")";

        ExportJobDTO jobDTO = adminExportService.queueExport(
                request, currentUser.getUser().getId(), adminName, ipAddress, userAgent);

        return ResponseEntity.accepted().body(jobDTO);
    }

    /**
     * Get the status of a specific job by UUID.
     */
    @GetMapping("/jobs/{jobUuid}")
    public ResponseEntity<ExportJobDTO> getJobStatus(
            @PathVariable String jobUuid,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        Optional<ExportJobDTO> opt = adminExportService.getJobStatus(jobUuid, currentUser.getUser().getId(), isSuperAdmin);
        return opt.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    /**
     * Get paginated history of all export jobs.
     */
    @GetMapping("/jobs")
    public ResponseEntity<Page<ExportJobDTO>> getExportHistory(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            Pageable pageable) {
        
        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        Page<ExportJobDTO> history = adminExportService.getExportHistory(currentUser.getUser().getId(), isSuperAdmin, pageable);
        return ResponseEntity.ok(history);
    }

    /**
     * Delete an export job and its physical file.
     */
    @DeleteMapping("/jobs/{jobUuid}")
    public ResponseEntity<Void> deleteJob(
            @PathVariable String jobUuid,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        boolean deleted = adminExportService.deleteJob(jobUuid, currentUser.getUser().getId(), isSuperAdmin);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /**
     * Securely download the completed exported file.
     */
    @GetMapping("/download/{jobUuid}")
    public ResponseEntity<Resource> downloadExport(
            @PathVariable String jobUuid,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        boolean isSuperAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        Optional<ExportJob> opt = adminExportService.getJobForDownload(jobUuid, currentUser.getUser().getId(), isSuperAdmin);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ExportJob job = opt.get();
        File file = new File(job.getFilePath());
        if (!file.exists()) {
            log.error("File not found on disk for job {}", jobUuid);
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        
        // Determine correct mime type
        String mimeType = "application/octet-stream";
        if (job.getFileName().endsWith(".xlsx")) {
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (job.getFileName().endsWith(".csv")) {
            mimeType = "text/csv";
        } else if (job.getFileName().endsWith(".pdf")) {
            mimeType = "application/pdf";
        }

        headers.setContentType(MediaType.parseMediaType(mimeType));
        headers.setContentDispositionFormData("attachment", job.getFileName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .body(resource);
    }
}
