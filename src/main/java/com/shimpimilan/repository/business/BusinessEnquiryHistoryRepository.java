package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessEnquiryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessEnquiryHistoryRepository extends JpaRepository<BusinessEnquiryHistory, Long> {
    List<BusinessEnquiryHistory> findByEnquiryIdOrderByCreatedAtDesc(Long enquiryId);
}
