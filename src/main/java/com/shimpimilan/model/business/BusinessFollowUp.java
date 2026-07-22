package com.shimpimilan.model.business;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_follow_up")
@Data
public class BusinessFollowUp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enquiry_id", nullable = false)
    private BusinessEnquiry enquiry;

    @Column(nullable = false)
    private LocalDateTime followUpDate;

    @Column(columnDefinition = "TEXT")
    private String outcome;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, COMPLETED

    @CreationTimestamp
    private LocalDateTime createdAt;
}
