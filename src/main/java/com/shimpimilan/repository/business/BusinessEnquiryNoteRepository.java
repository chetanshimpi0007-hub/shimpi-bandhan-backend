package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessEnquiryNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessEnquiryNoteRepository extends JpaRepository<BusinessEnquiryNote, Long> {
    List<BusinessEnquiryNote> findByEnquiryIdOrderByCreatedAtDesc(Long enquiryId);
}
