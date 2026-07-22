package com.shimpimilan.repository;

import com.shimpimilan.model.LinkedCandidateAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkedCandidateAccountRepository extends JpaRepository<LinkedCandidateAccount, Long> {
    Optional<LinkedCandidateAccount> findByProfileId(Long profileId);
    Optional<LinkedCandidateAccount> findByCandidateUserId(Long candidateUserId);
}
