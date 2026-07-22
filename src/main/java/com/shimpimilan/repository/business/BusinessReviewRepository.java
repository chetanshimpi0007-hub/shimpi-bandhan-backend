package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessReviewRepository extends JpaRepository<BusinessReview, Long> {
    Page<BusinessReview> findByBusinessIdAndIsApprovedTrue(Long businessId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM BusinessReview r WHERE r.business.id = :businessId AND r.isApproved = true")
    Double getAverageRatingForBusiness(Long businessId);
    
    @Query("SELECT COUNT(r) FROM BusinessReview r WHERE r.business.id = :businessId AND r.isApproved = true")
    Long countApprovedReviewsByBusinessId(Long businessId);
}
