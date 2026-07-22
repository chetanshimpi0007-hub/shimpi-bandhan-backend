package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_account_id", nullable = false)
    private FamilyAccount familyAccount;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isApproved = false;

    @OneToOne(mappedBy = "familyMember", cascade = CascadeType.ALL)
    private FamilyPermission permissions;
}
