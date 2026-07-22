package com.shimpimilan.repository.business;

import com.shimpimilan.model.business.BusinessCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessCategoryRepository extends JpaRepository<BusinessCategory, Long> {
    List<BusinessCategory> findByIsActiveTrueOrderByNameAsc();
    java.util.Optional<BusinessCategory> findByName(String name);
}
