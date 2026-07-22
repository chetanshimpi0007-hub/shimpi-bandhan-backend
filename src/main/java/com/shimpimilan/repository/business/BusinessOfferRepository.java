package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BusinessOfferRepository extends JpaRepository<BusinessOffer, Long> {
    List<BusinessOffer> findByBusinessId(Long businessId);
    List<BusinessOffer> findByBusinessIdAndIsActiveTrueAndValidUntilAfter(Long businessId, LocalDateTime date);
    List<BusinessOffer> findByIsActiveTrueAndValidUntilBefore(LocalDateTime date);
}
