package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessEnquiry;
import com.shimpimilan.model.business.EnquiryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface BusinessEnquiryRepository extends JpaRepository<BusinessEnquiry, Long> {
    List<BusinessEnquiry> findByBusinessId(Long businessId);
    List<BusinessEnquiry> findByUserId(Long userId);
    List<BusinessEnquiry> findByStatusNotAndNextFollowUpDateBefore(EnquiryStatus status, LocalDateTime date);
    long countByStatus(EnquiryStatus status);

    @Query(value = "SELECT YEAR(e.created_at) AS year, MONTH(e.created_at) AS month, COUNT(*) AS count " +
                   "FROM business_enquiries e WHERE e.created_at BETWEEN :start AND :end " +
                   "GROUP BY YEAR(e.created_at), MONTH(e.created_at) ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> countByMonthBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

