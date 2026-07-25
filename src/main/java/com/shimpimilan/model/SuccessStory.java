package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "success_stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuccessStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brideName;
    private String groomName;
    private LocalDate weddingDate;
    private String city;

    @Column(columnDefinition = "TEXT")
    private String shortStory;

    @Column(columnDefinition = "TEXT")
    private String story; // Full Story

    private String photoUrl;
    private String videoUrl;

    @Column(columnDefinition = "TEXT")
    private String galleryImages; // Comma-separated or JSON list of gallery image URLs

    @Builder.Default
    private Boolean isFeatured = true;

    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private Boolean isPublished = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
