package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessEnquiryMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessEnquiryMeetingRepository extends JpaRepository<BusinessEnquiryMeeting, Long> {
    List<BusinessEnquiryMeeting> findByEnquiryIdOrderByScheduledAtDesc(Long enquiryId);
}
