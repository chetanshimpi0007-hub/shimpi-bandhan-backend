package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FamilyActivityLogRepository extends JpaRepository<FamilyActivityLog, Long> {
    List<FamilyActivityLog> findByProfileIdOrderByTimestampDesc(Long profileId);
}
