package com.shimpimilan.service;

import com.shimpimilan.model.Profile;
import com.shimpimilan.model.User;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class ProfileSpecification {

    public static Specification<Profile> getMatchingProfiles(User loggedInUser,
                                                             Integer minAge, Integer maxAge,
                                                             Double minHeight, Double maxHeight,
                                                             String maritalStatus, String education,
                                                             String occupation, String city, String district, String state, Boolean manglik,
                                                             Double minIncome, String gotra, String familyType, String lifestyle,
                                                             Boolean isPremiumMember, Boolean isVerifiedProfile) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Core Matching Rules
            
            // Gender match: Opposite gender only
            if (loggedInUser.getProfile() != null && loggedInUser.getProfile().getGender() != null) {
                if (loggedInUser.getProfile().getGender() == com.shimpimilan.model.Gender.MALE) {
                    predicates.add(criteriaBuilder.equal(root.get("gender"), com.shimpimilan.model.Gender.FEMALE));
                } else {
                    predicates.add(criteriaBuilder.equal(root.get("gender"), com.shimpimilan.model.Gender.MALE));
                }
            }

            // Community match: Only same community
            predicates.add(criteriaBuilder.equal(root.get("community"), loggedInUser.getCommunity()));

            // Exclude self
            predicates.add(criteriaBuilder.notEqual(root.get("user").get("id"), loggedInUser.getId()));

            // Only show profiles of APPROVED users
            predicates.add(criteriaBuilder.equal(root.get("user").get("status"), com.shimpimilan.model.UserStatus.APPROVED));

            // 2. Search Filters
            if (minAge != null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("age"), minAge));
            if (maxAge != null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("age"), maxAge));
            if (minHeight != null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("height"), minHeight));
            if (maxHeight != null) predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("height"), maxHeight));
            
            if (maritalStatus != null && !maritalStatus.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("maritalStatus"), com.shimpimilan.model.MaritalStatus.valueOf(maritalStatus)));
            }
            if (education != null && !education.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("education")), "%" + education.toLowerCase() + "%"));
            }
            if (occupation != null && !occupation.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("occupation")), "%" + occupation.toLowerCase() + "%"));
            }
            if (city != null && !city.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("city")), "%" + city.toLowerCase() + "%"));
            }
            if (district != null && !district.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("district")), "%" + district.toLowerCase() + "%"));
            }
            if (state != null && !state.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("state")), "%" + state.toLowerCase() + "%"));
            }
            if (manglik != null) {
                predicates.add(criteriaBuilder.equal(root.get("manglik"), manglik));
            }
            if (minIncome != null) predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("annualIncome"), minIncome));
            if (gotra != null && !gotra.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("gotra")), "%" + gotra.toLowerCase() + "%"));
            }
            if (familyType != null && !familyType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("familyType"), familyType));
            }
            if (lifestyle != null && !lifestyle.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("lifestyle"), lifestyle));
            }
            if (isPremiumMember != null) {
                predicates.add(criteriaBuilder.equal(root.get("isPremiumMember"), isPremiumMember));
            }
            if (isVerifiedProfile != null) {
                predicates.add(criteriaBuilder.equal(root.get("isVerifiedProfile"), isVerifiedProfile));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
