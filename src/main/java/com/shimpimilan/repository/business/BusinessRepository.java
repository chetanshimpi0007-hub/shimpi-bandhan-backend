package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long>, JpaSpecificationExecutor<Business> {
    List<Business> findByOwnerId(Long ownerId);
    Page<Business> findByStatus(BusinessStatus status, Pageable pageable);

    // Custom query to find approved businesses mapped for the directory, prioritizing by Plan (Platinum -> Gold -> Silver -> Basic)
    // This will be handled in a custom Specification to allow dynamic sorting and filtering.

    long countByStatus(BusinessStatus status);
    long countByIsVerifiedFalse();
    long countByPlanType(com.shimpimilan.model.business.AdvertisementPlan planType);

    @Query(value = "SELECT YEAR(b.created_at) AS year, MONTH(b.created_at) AS month, COUNT(*) AS count " +
                   "FROM businesses b WHERE b.created_at BETWEEN :start AND :end " +
                   "GROUP BY YEAR(b.created_at), MONTH(b.created_at) ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> countByMonthBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT bc.name AS category, COUNT(b.id) AS count " +
                   "FROM businesses b LEFT JOIN business_categories bc ON b.category_id = bc.id " +
                   "GROUP BY bc.name ORDER BY count DESC LIMIT 10", nativeQuery = true)
    List<Map<String, Object>> countByCategoryAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT b.city AS city, COUNT(*) AS count FROM businesses b " +
                   "WHERE b.city IS NOT NULL GROUP BY b.city ORDER BY count DESC LIMIT 10", nativeQuery = true)
    List<Map<String, Object>> countByCityAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

