package com.shimpimilan.service;

import com.shimpimilan.model.Referral;
import com.shimpimilan.model.ReferralWallet;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.ReferralRepository;
import com.shimpimilan.repository.ReferralWalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralRepository referralRepository;
    private final ReferralWalletRepository referralWalletRepository;

    private static final double REWARD_AMOUNT_INR = 10.0;

    @Transactional
    public void processRewardForPremiumPurchase(User newlyPremiumUser) {
        Referral referral = referralRepository.findByReferredUserId(newlyPremiumUser.getId());
        
        if (referral != null && !referral.getIsRewardProcessed()) {
            User referrer = referral.getReferrer();
            
            ReferralWallet wallet = referralWalletRepository.findByUserId(referrer.getId())
                    .orElseThrow(() -> new RuntimeException("Referrer wallet not found"));
                    
            wallet.setTotalEarnings(wallet.getTotalEarnings() + REWARD_AMOUNT_INR);
            wallet.setAvailableBalance(wallet.getAvailableBalance() + REWARD_AMOUNT_INR);
            referralWalletRepository.save(wallet);
            
            referral.setIsRewardProcessed(true);
            referral.setRewardAmount(REWARD_AMOUNT_INR);
            referralRepository.save(referral);
        }
    }
}
