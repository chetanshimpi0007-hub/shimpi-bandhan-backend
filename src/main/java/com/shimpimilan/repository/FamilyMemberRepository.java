package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByProfileId(Long profileId);
    List<FamilyMember> findByFamilyAccountId(Long familyAccountId);
    boolean existsByProfileIdAndFamilyAccountId(Long profileId, Long familyAccountId);
}
