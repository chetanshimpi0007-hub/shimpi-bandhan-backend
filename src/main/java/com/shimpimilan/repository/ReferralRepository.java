package com.shimpimilan.repository;

import com.shimpimilan.model.Referral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralRepository extends JpaRepository<Referral, Long> {
    List<Referral> findByReferrerId(Long referrerId);
    boolean existsByReferredUserId(Long referredUserId);
    Referral findByReferredUserId(Long referredUserId);
}
