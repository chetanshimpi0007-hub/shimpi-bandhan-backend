package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessPaymentRepository extends JpaRepository<BusinessPayment, Long> {
    Optional<BusinessPayment> findByRazorpayOrderId(String razorpayOrderId);
    List<BusinessPayment> findByBusinessId(Long businessId);

    @Query("SELECT COALESCE(SUM(bp.amount), 0) FROM BusinessPayment bp WHERE bp.status = 'CAPTURED'")
    Double sumTotalRevenue();
}

