package com.shimpimilan.repository;

import com.shimpimilan.model.FamilyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FamilyAccountRepository extends JpaRepository<FamilyAccount, Long> {
    Optional<FamilyAccount> findByUserId(Long userId);
}
