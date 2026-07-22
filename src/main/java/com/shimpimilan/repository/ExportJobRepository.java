package com.shimpimilan.repository;

import com.shimpimilan.model.ExportJob;
import com.shimpimilan.model.ExportJobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {
    Optional<ExportJob> findByJobUuid(String jobUuid);
    Page<ExportJob> findByGeneratedByOrderByRequestedAtDesc(Long generatedBy, Pageable pageable);
    Page<ExportJob> findAllByOrderByRequestedAtDesc(Pageable pageable);
    List<ExportJob> findByStatusAndExpiresAtBefore(ExportJobStatus status, LocalDateTime now);

    @Query("SELECT e FROM ExportJob e WHERE e.expiresAt < :now AND e.status != :expired")
    List<ExportJob> findExpiredJobs(@Param("now") LocalDateTime now, @Param("expired") ExportJobStatus expired);

    long countByStatusIn(List<ExportJobStatus> statuses);
}
