package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String familyMemberName;

    @Column(nullable = false)
    private String mobileNumber;

    private String whatsappNumber;
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FamilyRelationship relationshipWithCandidate;

    @Column(columnDefinition = "TEXT")
    private String address;
}
