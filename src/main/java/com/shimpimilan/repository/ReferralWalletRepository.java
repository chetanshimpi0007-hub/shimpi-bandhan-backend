package com.shimpimilan.repository;

import com.shimpimilan.model.ReferralWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralWalletRepository extends JpaRepository<ReferralWallet, Long> {
    Optional<ReferralWallet> findByUserId(Long userId);
}
