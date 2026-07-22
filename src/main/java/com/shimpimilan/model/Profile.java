package com.shimpimilan.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "profiles", indexes = {
    @Index(name = "idx_profile_plan_type", columnList = "planType"),
    @Index(name = "idx_profile_trial_end", columnList = "trialEndDate"),
    @Index(name = "idx_profile_premium_expiry", columnList = "premiumExpiryDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    private String fullName;
    private String email;
    private String whatsappNumber;
    private String familyContact;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dateOfBirth;
    
    @Transient // Age is auto-calculated from DOB, no need to persist
    private Integer age;

    private Double height;
    private Double weight;
    private String bloodGroup;
    private String religion;

    @Enumerated(EnumType.STRING)
    private Community community;

    private String gotra;
    private Boolean manglik;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    private String education;
    private String occupation;
    private String company;
    private Double annualIncome;

    private String city;
    private String district;
    private String state;
    private String country;
    private String village;
    private String pincode;
    private String exactAddress;

    @Column(columnDefinition = "TEXT")
    private String familyDetails;
    private String familyType;
    private String lifestyle;

    @Column(columnDefinition = "TEXT")
    private String aboutMe;

    @Column(columnDefinition = "TEXT")
    private String partnerPreference;

    private Integer profileCompletionPercentage;
    
    // Premium & Verification
    private Boolean isPremiumMember;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PlanType planType = PlanType.FREE;
    
    @Enumerated(EnumType.STRING)
    private MembershipSource membershipSource;
    
    private LocalDateTime trialStartDate;
    private LocalDateTime trialEndDate;
    private LocalDateTime premiumExpiryDate;
    private LocalDateTime lastRenewalDate;
    private LocalDateTime nextRenewalDate;
    
    private Boolean isVerifiedProfile;
    private Boolean isMobileVerified;
    private Boolean isEmailVerified;
    private Boolean isAdminVerified;

    // Privacy Settings
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PrivacySetting contactNumberPrivacy = PrivacySetting.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PrivacySetting whatsappNumberPrivacy = PrivacySetting.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PrivacySetting familyContactPrivacy = PrivacySetting.PUBLIC;

    @Builder.Default
    private Boolean familyApprovalRequired = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    // New Embedded Objects
    @Embedded
    private com.shimpimilan.model.profile.BirthDetails birthDetails;
    
    @Embedded
    private com.shimpimilan.model.profile.HoroscopeDetails horoscopeDetails;
    
    @Embedded
    private com.shimpimilan.model.profile.PhysicalDetails physicalDetails;
    
    @Embedded
    private com.shimpimilan.model.profile.EducationCareerDetails educationCareerDetails;
    
    @Embedded
    private com.shimpimilan.model.profile.FamilyDetailsExtended familyDetailsExtended;
    
    @Embedded
    private com.shimpimilan.model.profile.PartnerPreferenceExtended partnerPreferenceExtended;
    
    @Embedded
    private com.shimpimilan.model.profile.Lifestyle lifestyleExtended;
    
    @Embedded
    private com.shimpimilan.model.profile.PassportDetails passportDetails;
    
    // Verification Enhancements
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private com.shimpimilan.model.profile.VerificationStatus verificationStatus = com.shimpimilan.model.profile.VerificationStatus.DRAFT;
    
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;
    
    private String profileType;
    private String alternateMobile;

    @PostLoad
    @PostPersist
    @PostUpdate
    public void calculateAge() {
        if (dateOfBirth != null) {
            this.age = java.time.Period.between(dateOfBirth, java.time.LocalDate.now()).getYears();
        }
    }
}
