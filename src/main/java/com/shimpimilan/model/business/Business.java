package com.shimpimilan.model.business;

import com.shimpimilan.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "businesses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private BusinessCategory category;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, length = 20)
    private String mobileNumber;

    @Column(length = 20)
    private String whatsappNumber;

    private String email;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String gstNumber;

    private String website;
    private String instagram;
    private String facebook;
    private String youtube;

    @Column(columnDefinition = "TEXT")
    private String addressLine;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 100)
    private String state;
    
    @Column(length = 20)
    private String pinCode;

    @Column(columnDefinition = "TEXT")
    private String googleMapsUrl;

    @Column(columnDefinition = "TEXT")
    private String logoUrl;
    
    @Column(columnDefinition = "TEXT")
    private String coverUrl;

    private String workingHours;
    private Integer yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private BusinessStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private AdvertisementPlan planType;

    @Builder.Default
    private Boolean isAdminFeatured = false;
    
    @Builder.Default
    private Integer priorityOverride = 0;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String workingHoursJson;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String socialLinksJson;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<BusinessOffer> offers = new java.util.ArrayList<>();
}
