package com.shimpimilan.repository;

import com.shimpimilan.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    Optional<Payment> findByRazorpayOrderId(String orderId);
    Optional<Payment> findByRazorpayPaymentId(String paymentId);

    @Query("SELECT COALESCE(SUM(p.finalAmountPaid), 0) FROM Payment p WHERE p.status = :status")
    Double sumFinalAmountPaidByStatus(@Param("status") com.shimpimilan.model.PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.finalAmountPaid), 0) FROM Payment p WHERE p.status = :status AND p.createdAt >= :startDate")
    Double sumFinalAmountPaidByStatusAndCreatedAtAfter(@Param("status") com.shimpimilan.model.PaymentStatus status, @Param("startDate") java.time.LocalDateTime startDate);

    long countByStatus(com.shimpimilan.model.PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.discountApplied), 0) FROM Payment p WHERE p.discountApplied > 0")
    Double sumDiscountsApplied();

    @Query(value = "SELECT YEAR(p.created_at) AS year, MONTH(p.created_at) AS month, COALESCE(SUM(p.final_amount_paid), 0) AS revenue " +
                   "FROM payments p WHERE p.status = 'CAPTURED' AND p.created_at BETWEEN :start AND :end " +
                   "GROUP BY YEAR(p.created_at), MONTH(p.created_at) ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> monthlyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT YEAR(p.created_at) AS year, COALESCE(SUM(p.final_amount_paid), 0) AS revenue " +
                   "FROM payments p WHERE p.status = 'CAPTURED' " +
                   "GROUP BY YEAR(p.created_at) ORDER BY year", nativeQuery = true)
    List<Map<String, Object>> yearlyRevenue();

    @Query(value = "SELECT p.payment_method AS method, COUNT(*) AS count, COALESCE(SUM(p.final_amount_paid), 0) AS revenue " +
                   "FROM payments p WHERE p.status = 'CAPTURED' AND p.payment_method IS NOT NULL " +
                   "GROUP BY p.payment_method", nativeQuery = true)
    List<Map<String, Object>> paymentMethodSummary();

    @Query(value = "SELECT YEAR(p.created_at) AS year, MONTH(p.created_at) AS month, COUNT(*) AS count " +
                   "FROM payments p WHERE p.status = 'CAPTURED' AND p.created_at BETWEEN :start AND :end " +
                   "GROUP BY YEAR(p.created_at), MONTH(p.created_at) ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> premiumGrowth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT YEAR(p.created_at) AS year, MONTH(p.created_at) AS month, COUNT(*) AS count " +
                   "FROM payments p WHERE p.created_at BETWEEN :start AND :end " +
                   "GROUP BY YEAR(p.created_at), MONTH(p.created_at) ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> monthlyPaymentCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT CAST(p.created_at AS DATE) AS date, COALESCE(SUM(p.final_amount_paid), 0) AS revenue " +
                   "FROM payments p WHERE p.status = 'CAPTURED' AND p.created_at BETWEEN :start AND :end " +
                   "GROUP BY CAST(p.created_at AS DATE) ORDER BY date", nativeQuery = true)
    List<Map<String, Object>> dailyRevenue(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
