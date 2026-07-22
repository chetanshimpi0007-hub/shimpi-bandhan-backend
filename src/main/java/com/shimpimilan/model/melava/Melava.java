package com.shimpimilan.model.melava;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Entity
@Table(name = "melava")
@Data
public class Melava {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String melavaName;

    private String bannerUrl;
    private String logoUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private LocalDate registrationStartDate;
    private LocalDate registrationEndDate;

    @Column(nullable = false, length = 255)
    private String venueName;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String googleMapLocation;

    private String city;
    private String state;
    private String pincode;

    private String organizerName;
    private String contactPerson;
    private String mobileNumber;
    private String email;
    private String website;

    private Double registrationFee = 0.0;
    private Double coupleEntryFee = 0.0;
    private Double visitorFee = 0.0;

    private Integer maximumRegistrations;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MelavaStatus status = MelavaStatus.UPCOMING;

    private String brochurePdfUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
