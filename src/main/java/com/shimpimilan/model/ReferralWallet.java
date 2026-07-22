package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "referral_wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralWallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Builder.Default
    private Double totalEarnings = 0.0;

    @Builder.Default
    private Double availableBalance = 0.0;

    @Builder.Default
    private Double usedBalance = 0.0;
}
