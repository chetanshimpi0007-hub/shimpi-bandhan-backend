package com.shimpimilan.repository;

import com.shimpimilan.model.ChatReport;
import com.shimpimilan.model.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatReportRepository extends JpaRepository<ChatReport, Long> {
    Page<ChatReport> findByStatus(ReportStatus status, Pageable pageable);
    List<ChatReport> findByChatRoomId(Long chatRoomId);
    long countByStatus(ReportStatus status);
}
