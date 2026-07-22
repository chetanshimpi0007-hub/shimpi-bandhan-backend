package com.shimpimilan.dto;

import com.shimpimilan.model.Gender;
import com.shimpimilan.model.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    private String fullName;
    private String email;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Double height;
    private Double weight;
    private String bloodGroup;
    private String religion;
    private String gotra;
    private Boolean manglik;
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
    private String familyContact;
    private String familyDetails;
    private String familyType;
    private String lifestyle;
    private String aboutMe;
    private String partnerPreference;
    private String profilePhotoUrl;
    private List<String> galleryPhotos;

    private String profileType;
    private String alternateMobile;

    private com.shimpimilan.model.profile.BirthDetails birthDetails;
    private com.shimpimilan.model.profile.HoroscopeDetails horoscopeDetails;
    private com.shimpimilan.model.profile.PhysicalDetails physicalDetails;
    private com.shimpimilan.model.profile.EducationCareerDetails educationCareerDetails;
    private com.shimpimilan.model.profile.FamilyDetailsExtended familyDetailsExtended;
    private com.shimpimilan.model.profile.PartnerPreferenceExtended partnerPreferenceExtended;
    private com.shimpimilan.model.profile.Lifestyle lifestyleExtended;
    private com.shimpimilan.model.profile.PassportDetails passportDetails;
}
