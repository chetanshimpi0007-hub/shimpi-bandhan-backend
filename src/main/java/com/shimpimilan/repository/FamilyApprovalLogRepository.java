package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyApprovalLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyApprovalLogRepository extends JpaRepository<FamilyApprovalLog, Long> {
    List<FamilyApprovalLog> findByApprovalId(Long approvalId);
}
