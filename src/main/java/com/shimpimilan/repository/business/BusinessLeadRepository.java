package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BusinessLeadRepository extends JpaRepository<BusinessLead, Long> {
    Long countByBusinessId(Long businessId);
    
    @Query("SELECT COUNT(l) FROM BusinessLead l WHERE l.business.id = :businessId AND l.createdAt >= :startDate")
    Long countByBusinessIdSince(Long businessId, LocalDateTime startDate);
}
