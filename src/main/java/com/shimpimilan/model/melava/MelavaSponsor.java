package com.shimpimilan.model.melava;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "melava_sponsor")
@Data
public class MelavaSponsor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "melava_id", nullable = false)
    private Melava melava;

    @Column(nullable = false, length = 150)
    private String sponsorName;

    private String logoUrl;
    
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;
    
    private Double sponsorshipAmount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
