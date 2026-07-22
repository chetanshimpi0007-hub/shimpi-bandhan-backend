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
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_approval_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyApprovalLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "approval_id", nullable = false)
    private FamilyApproval approval;

    private String action; // e.g., "APPROVED", "REJECTED", "OVERRIDE"

    @ManyToOne
    @JoinColumn(name = "action_by", nullable = false)
    private User actionBy;

    private String comment;

    @CreationTimestamp
    private LocalDateTime timestamp;
}
