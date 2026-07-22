package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessSubscriptionRepository extends JpaRepository<BusinessSubscription, Long> {
    Optional<BusinessSubscription> findTopByBusinessIdAndIsActiveTrueOrderByExpiryDateDesc(Long businessId);
    List<BusinessSubscription> findByIsActiveTrueAndExpiryDateBetween(LocalDateTime start, LocalDateTime end);
    List<BusinessSubscription> findByIsActiveTrueAndExpiryDateBefore(LocalDateTime now);
}
