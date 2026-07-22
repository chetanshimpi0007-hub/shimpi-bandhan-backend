package com.shimpimilan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shimpimilan.dto.report.ExportJobDTO;
import com.shimpimilan.dto.report.ExportRequestDTO;
import com.shimpimilan.model.*;
import com.shimpimilan.repository.ExportJobRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminExportService {

    private final ExportJobRepository exportJobRepository;
    private final ExportWorkerService exportWorkerService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates an ExportJob record immediately (QUEUED status) and queues
     * the actual file generation asynchronously.
     *
     * @return the saved ExportJob with its UUID, for polling.
     */
    @Transactional
    public ExportJobDTO queueExport(ExportRequestDTO request, Long adminUserId,
                                    String adminName, String ipAddress, String userAgent) {
        Map<String, String> filters = request.getFilters();
        String filtersJson = "";
        try {
            filtersJson = filters != null ? objectMapper.writeValueAsString(filters) : "{}";
        } catch (JsonProcessingException ignored) {}

        ExportJob job = ExportJob.builder()
                .jobUuid(UUID.randomUUID().toString())
                .reportType(request.getReportType())
                .format(request.getFormat())
                .status(ExportJobStatus.QUEUED)
                .generatedBy(adminUserId)
                .generatedByName(adminName)
                .filtersApplied(filtersJson)
                .requestIpAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        ExportJob saved = exportJobRepository.save(job);
        log.info("Export job queued: id={}, uuid={}, type={}, format={}",
                saved.getId(), saved.getJobUuid(), saved.getReportType(), saved.getFormat());

        // Dispatch to background thread
        exportWorkerService.processExportJob(saved.getId(), filters);

        return toDTO(saved);
    }

    /**
     * Returns the current status of a job by UUID.
     * Only the job owner or SUPER_ADMIN can view it.
     */
    public Optional<ExportJobDTO> getJobStatus(String jobUuid, Long adminUserId, boolean isSuperAdmin) {
        return exportJobRepository.findByJobUuid(jobUuid)
                .filter(j -> isSuperAdmin || j.getGeneratedBy().equals(adminUserId))
                .map(this::toDTO);
    }

    /**
     * Returns paginated export history for admin.
     */
    public Page<ExportJobDTO> getExportHistory(Long adminUserId, boolean isSuperAdmin, Pageable pageable) {
        Page<ExportJob> page = isSuperAdmin
                ? exportJobRepository.findAllByOrderByRequestedAtDesc(pageable)
                : exportJobRepository.findByGeneratedByOrderByRequestedAtDesc(adminUserId, pageable);
        return page.map(this::toDTO);
    }

    /**
     * Validates ownership and returns the file path for secure download.
     */
    public Optional<ExportJob> getJobForDownload(String jobUuid, Long adminUserId, boolean isSuperAdmin) {
        return exportJobRepository.findByJobUuid(jobUuid)
                .filter(j -> j.getStatus() == ExportJobStatus.COMPLETED)
                .filter(j -> isSuperAdmin || j.getGeneratedBy().equals(adminUserId));
    }

    /**
     * Deletes the job record and its physical file.
     */
    @Transactional
    public boolean deleteJob(String jobUuid, Long adminUserId, boolean isSuperAdmin) {
        Optional<ExportJob> opt = exportJobRepository.findByJobUuid(jobUuid)
                .filter(j -> isSuperAdmin || j.getGeneratedBy().equals(adminUserId));
        if (opt.isEmpty()) return false;

        ExportJob job = opt.get();
        deletePhysicalFile(job.getFilePath());
        exportJobRepository.delete(job);
        log.info("Export job deleted: uuid={} by adminId={}", jobUuid, adminUserId);
        return true;
    }

    public void deletePhysicalFile(String filePath) {
        if (filePath == null) return;
        try {
            java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get(filePath));
        } catch (Exception e) {
            log.warn("Could not delete export file {}: {}", filePath, e.getMessage());
        }
    }

    private ExportJobDTO toDTO(ExportJob j) {
        return ExportJobDTO.builder()
                .id(j.getId())
                .jobUuid(j.getJobUuid())
                .reportType(j.getReportType())
                .format(j.getFormat())
                .status(j.getStatus())
                .generatedByName(j.getGeneratedByName())
                .fileName(j.getFileName())
                .fileSizeBytes(j.getFileSizeBytes())
                .filtersApplied(j.getFiltersApplied())
                .errorDetails(j.getErrorDetails())
                .requestedAt(j.getRequestedAt())
                .completedAt(j.getCompletedAt())
                .expiresAt(j.getExpiresAt())
                // Never expose real file path – provide download URL instead
                .downloadUrl(j.getStatus() == ExportJobStatus.COMPLETED
                        ? "/api/v1/admin/export/download/" + j.getJobUuid() : null)
                .build();
    }
}
