package com.shimpimilan.service.business;

import com.shimpimilan.dto.business.BusinessSearchRequest;
import com.shimpimilan.model.business.Business;
import com.shimpimilan.model.business.BusinessStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BusinessSpecification {

    public static Specification<Business> getSearchSpecification(BusinessSearchRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only show APPROVED (ACTIVE) businesses
            predicates.add(criteriaBuilder.equal(root.get("status"), BusinessStatus.ACTIVE));
            
            // Only show verified businesses
            predicates.add(criteriaBuilder.equal(root.get("isVerified"), true));

            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                String searchPattern = "%" + request.getQuery().toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("businessName")), searchPattern);
                Predicate descMatch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(nameMatch, descMatch));
            }

            if (request.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), request.getCategoryId()));
            }

            if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + request.getCity().toLowerCase() + "%"));
            }

            if (request.getState() != null && !request.getState().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), "%" + request.getState().toLowerCase() + "%"));
            }

            if (request.getActiveOffersOnly() != null && request.getActiveOffersOnly()) {
                // In a real production app we'd join BusinessOffer and check validFrom/validUntil,
                // but since we want to keep the query simple for H2, we can just require a subquery or join.
                jakarta.persistence.criteria.Join<Object, Object> offers = root.join("offers");
                predicates.add(criteriaBuilder.equal(offers.get("isActive"), true));
                predicates.add(criteriaBuilder.greaterThan(offers.get("validUntil"), java.time.LocalDateTime.now()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
