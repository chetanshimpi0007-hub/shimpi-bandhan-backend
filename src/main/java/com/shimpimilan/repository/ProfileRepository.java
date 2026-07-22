package com.shimpimilan.repository;

import com.shimpimilan.model.Gender;
import com.shimpimilan.model.PlanType;
import com.shimpimilan.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long>, JpaSpecificationExecutor<Profile> {
    Optional<Profile> findByUserId(Long userId);
    List<Profile> findByPlanTypeAndTrialEndDateBefore(com.shimpimilan.model.PlanType planType, java.time.LocalDateTime date);
    List<Profile> findByPlanTypeAndPremiumExpiryDateBefore(com.shimpimilan.model.PlanType planType, java.time.LocalDateTime date);
    List<Profile> findByPlanTypeIn(List<PlanType> planTypes);

    long countByVerificationStatus(com.shimpimilan.model.profile.VerificationStatus status);
    long countByPlanType(com.shimpimilan.model.PlanType planType);
    long countByGender(Gender gender);

    @Query("SELECT p.city AS label, COUNT(p) AS count FROM Profile p WHERE p.city IS NOT NULL AND " +
           "(:start IS NULL OR p.createdAt >= :start) AND " +
           "(:end IS NULL OR p.createdAt <= :end) " +
           "GROUP BY p.city ORDER BY COUNT(p) DESC")
    List<Map<String, Object>> countByCityAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.state AS label, COUNT(p) AS count FROM Profile p WHERE p.state IS NOT NULL AND " +
           "(:start IS NULL OR p.createdAt >= :start) AND " +
           "(:end IS NULL OR p.createdAt <= :end) " +
           "GROUP BY p.state ORDER BY COUNT(p) DESC")
    List<Map<String, Object>> countByStateAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.gender AS label, COUNT(p) AS count FROM Profile p WHERE " +
           "(:start IS NULL OR p.createdAt >= :start) AND " +
           "(:end IS NULL OR p.createdAt <= :end) " +
           "GROUP BY p.gender")
    List<Map<String, Object>> countByGenderAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.planType AS label, COUNT(p) AS count FROM Profile p WHERE " +
           "(:start IS NULL OR p.createdAt >= :start) AND " +
           "(:end IS NULL OR p.createdAt <= :end) " +
           "GROUP BY p.planType")
    List<Map<String, Object>> countByPlanTypeAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.verificationStatus AS label, COUNT(p) AS count FROM Profile p WHERE " +
           "(:start IS NULL OR p.createdAt >= :start) AND " +
           "(:end IS NULL OR p.createdAt <= :end) " +
           "GROUP BY p.verificationStatus")
    List<Map<String, Object>> countByVerificationStatusAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.planType = 'PREMIUM' AND p.premiumExpiryDate < :now")
    long countExpiredPremium(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.planType = 'PREMIUM' AND p.premiumExpiryDate BETWEEN :start AND :end")
    long countUpcomingRenewals(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value =
        "SELECT " +
        "  CASE " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) < 25 THEN 'Under 25' " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) BETWEEN 25 AND 30 THEN '25-30' " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) BETWEEN 31 AND 35 THEN '31-35' " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) BETWEEN 36 AND 40 THEN '36-40' " +
        "    ELSE 'Above 40' " +
        "  END AS ageGroup, COUNT(*) AS count " +
        "FROM profiles p WHERE p.date_of_birth IS NOT NULL " +
        "GROUP BY CASE " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) < 25 THEN 'Under 25' " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) BETWEEN 25 AND 30 THEN '25-30' " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) BETWEEN 31 AND 35 THEN '31-35' " +
        "    WHEN TIMESTAMPDIFF(YEAR, p.date_of_birth, CURDATE()) BETWEEN 36 AND 40 THEN '36-40' " +
        "    ELSE 'Above 40' " +
        "  END " +
        "ORDER BY MIN(p.date_of_birth) DESC",
        nativeQuery = true)
    List<Map<String, Object>> countByAgeGroupAggregation(
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end);
}
