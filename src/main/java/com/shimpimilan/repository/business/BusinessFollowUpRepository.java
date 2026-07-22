package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessFollowUp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessFollowUpRepository extends JpaRepository<BusinessFollowUp, Long> {
    List<BusinessFollowUp> findByEnquiryIdOrderByFollowUpDateDesc(Long enquiryId);
}
