package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "linked_candidate_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkedCandidateAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @OneToOne
    @JoinColumn(name = "candidate_user_id", nullable = false)
    private User candidateUser;
}
