package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessGalleryRepository extends JpaRepository<BusinessGallery, Long> {
    List<BusinessGallery> findByBusinessId(Long businessId);
}
