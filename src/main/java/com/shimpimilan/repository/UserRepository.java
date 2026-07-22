package com.shimpimilan.repository;

import com.shimpimilan.model.User;
import com.shimpimilan.model.UserStatus;
import com.shimpimilan.model.Community;
import com.shimpimilan.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    Optional<User> findByReferralCode(String referralCode);
    long countByCreatedAtAfter(LocalDateTime date);
    long countByStatus(UserStatus status);
    long countByRole(Role role);
    long countByCommunity(Community community);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT u FROM User u LEFT JOIN u.profile p WHERE " +
           "(:search IS NULL OR u.phone LIKE %:search% OR p.fullName LIKE %:search% OR p.email LIKE %:search%) AND " +
           "(:community IS NULL OR u.community = :community) AND " +
           "(:status IS NULL OR u.status = :status)")
    Page<User> findAllWithFilters(
        @Param("search") String search,
        @Param("community") Community community,
        @Param("status") UserStatus status,
        Pageable pageable
    );

    @Query("SELECT u.community AS label, COUNT(u) AS count FROM User u WHERE " +
           "(:start IS NULL OR u.createdAt >= :start) AND " +
           "(:end IS NULL OR u.createdAt <= :end) " +
           "GROUP BY u.community")
    List<Map<String, Object>> countByCommunityAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT u.status AS label, COUNT(u) AS count FROM User u WHERE " +
           "(:start IS NULL OR u.createdAt >= :start) AND " +
           "(:end IS NULL OR u.createdAt <= :end) " +
           "GROUP BY u.status")
    List<Map<String, Object>> countByStatusAggregation(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(u) FROM User u WHERE " +
           "(:start IS NULL OR u.createdAt >= :start) AND " +
           "(:end IS NULL OR u.createdAt <= :end)")
    long countFiltered(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);


    @Query(value = "SELECT YEAR(u.created_at) AS \"year\", MONTH(u.created_at) AS \"month\", COUNT(*) AS count " +
                   "FROM app_users u WHERE u.created_at BETWEEN :start AND :end " +
                   "GROUP BY YEAR(u.created_at), MONTH(u.created_at) ORDER BY YEAR(u.created_at), MONTH(u.created_at)", nativeQuery = true)
    List<Map<String, Object>> countByMonthBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT CAST(u.created_at AS DATE) AS \"day\", COUNT(*) AS count " +
                   "FROM app_users u WHERE u.created_at BETWEEN :start AND :end " +
                   "GROUP BY CAST(u.created_at AS DATE) ORDER BY CAST(u.created_at AS DATE)", nativeQuery = true)
    List<Map<String, Object>> countByDayBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}

