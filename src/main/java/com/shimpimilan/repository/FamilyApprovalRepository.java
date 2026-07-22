package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FamilyApprovalRepository extends JpaRepository<FamilyApproval, Long> {
    List<FamilyApproval> findByRoomId(Long roomId);
}
