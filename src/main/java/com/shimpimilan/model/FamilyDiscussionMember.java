package com.shimpimilan.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "family_discussion_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyDiscussionMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private FamilyDiscussionRoom room;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String familyRole; // e.g., "Bride", "Groom", "Father", "Mother", "Brother", "Sister", "Guardian"

    @Builder.Default
    private Boolean isMuted = false;

    @CreationTimestamp
    private LocalDateTime joinedAt;
}
