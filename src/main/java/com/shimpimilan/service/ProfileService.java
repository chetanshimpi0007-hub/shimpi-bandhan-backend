package com.shimpimilan.service;

import com.shimpimilan.dto.ProfileRequest;
import com.shimpimilan.dto.ProfileResponse;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.repository.ProfilePhotoRepository;
import com.shimpimilan.model.photo.ProfilePhoto;
import com.shimpimilan.model.photo.PhotoStatus;
import com.shimpimilan.model.photo.PhotoType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final ContactUnlockService contactUnlockService;
    private final ProfilePhotoRepository profilePhotoRepository;

    public ProfileResponse createOrUpdateProfile(Long userId, ProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Profile profile = profileRepository.findByUserId(userId).orElse(new Profile());
        profile.setUser(user);
        profile.setFullName(request.getFullName());
        profile.setEmail(request.getEmail());
        profile.setGender(request.getGender());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setHeight(request.getHeight());
        profile.setWeight(request.getWeight());
        profile.setBloodGroup(request.getBloodGroup());
        profile.setReligion(request.getReligion());
        profile.setCommunity(user.getCommunity()); // Enforce registered community
        profile.setGotra(request.getGotra());
        profile.setManglik(request.getManglik());
        profile.setMaritalStatus(request.getMaritalStatus());
        profile.setEducation(request.getEducation());
        profile.setOccupation(request.getOccupation());
        profile.setCompany(request.getCompany());
        profile.setAnnualIncome(request.getAnnualIncome());
        profile.setCity(request.getCity());
        profile.setDistrict(request.getDistrict());
        profile.setState(request.getState());
        profile.setCountry(request.getCountry());
        profile.setVillage(request.getVillage());
        profile.setPincode(request.getPincode());
        profile.setFamilyDetails(request.getFamilyDetails());
        profile.setFamilyType(request.getFamilyType());
        profile.setLifestyle(request.getLifestyle());
        profile.setAboutMe(request.getAboutMe());
        profile.setPartnerPreference(request.getPartnerPreference());
        
        // Extended Details
        profile.setBirthDetails(request.getBirthDetails());
        profile.setHoroscopeDetails(request.getHoroscopeDetails());
        profile.setPhysicalDetails(request.getPhysicalDetails());
        profile.setEducationCareerDetails(request.getEducationCareerDetails());
        profile.setFamilyDetailsExtended(request.getFamilyDetailsExtended());
        profile.setPartnerPreferenceExtended(request.getPartnerPreferenceExtended());
        profile.setLifestyleExtended(request.getLifestyleExtended());
        profile.setPassportDetails(request.getPassportDetails());
        
        profile.setProfileType(request.getProfileType());
        profile.setAlternateMobile(request.getAlternateMobile());
        profile.setFamilyContact(request.getFamilyContact());
        
        // Basic completion calculation (can be improved)
        int completion = calculateCompletion(profile);
        profile.setProfileCompletionPercentage(completion);
        
        // Initial setup for flags
        if (profile.getId() == null) {
            profile.setIsPremiumMember(false);
            profile.setIsVerifiedProfile(false);
            profile.setIsMobileVerified(false);
            profile.setIsEmailVerified(false);
            profile.setIsAdminVerified(false);
        }

        Profile savedProfile = profileRepository.save(profile);
        return mapToResponse(savedProfile, user); // User views their own profile with contact details
    }

    public ProfileResponse getProfileByUserId(Long targetUserId, User requestingUser) {
        Profile profile = profileRepository.findByUserId(targetUserId)
                .orElseGet(() -> {
                    if (targetUserId.equals(requestingUser.getId())) {
                        Profile newProfile = new Profile();
                        newProfile.setUser(requestingUser);
                        return newProfile;
                    }
                    throw new RuntimeException("Profile not found");
                });
        return mapToResponse(profile, requestingUser);
    }

    public Page<ProfileResponse> searchProfiles(User loggedInUser,
                                                Integer minAge, Integer maxAge,
                                                Double minHeight, Double maxHeight,
                                                String maritalStatus, String education,
                                                String occupation, String city, String district, String state, Boolean manglik,
                                                Double minIncome, String gotra, String familyType, String lifestyle,
                                                Boolean isPremiumMember, Boolean isVerifiedProfile,
                                                Pageable pageable) {

        Specification<Profile> spec = ProfileSpecification.getMatchingProfiles(
                loggedInUser, minAge, maxAge, minHeight, maxHeight, maritalStatus,
                education, occupation, city, district, state, manglik,
                minIncome, gotra, familyType, lifestyle, isPremiumMember, isVerifiedProfile
        );

        Page<Profile> profiles = profileRepository.findAll(spec, pageable);
        return profiles.map(profile -> mapToResponse(profile, loggedInUser));
    }

    private int calculateCompletion(Profile profile) {
        int totalWeight = 100;
        int score = 0;
        
        // Core (20%)
        if (profile.getFullName() != null) score += 5;
        
        // Photo scores based on repository
        List<ProfilePhoto> photos = profilePhotoRepository.findByUserIdAndStatus(profile.getUser().getId(), PhotoStatus.APPROVED);
        if (photos.stream().anyMatch(ProfilePhoto::getIsPrimary)) score += 10;
        if (photos.stream().filter(p -> !p.getIsPrimary()).count() >= 2) score += 5;
        
        // Personal & Birth (20%)
        if (profile.getDateOfBirth() != null) score += 5;
        if (profile.getBirthDetails() != null && profile.getBirthDetails().getBirthTime() != null) score += 5;
        if (profile.getBirthDetails() != null && profile.getBirthDetails().getBirthPlace() != null) score += 5;
        if (profile.getHeight() != null) score += 5;
        
        // Education & Career (20%)
        if (profile.getEducation() != null) score += 10;
        if (profile.getOccupation() != null) score += 10;
        
        // Family (20%)
        if (profile.getFamilyDetailsExtended() != null && profile.getFamilyDetailsExtended().getFatherName() != null) score += 10;
        if (profile.getFamilyDetailsExtended() != null && profile.getFamilyDetailsExtended().getMotherName() != null) score += 10;
        
        // Partner Preference (20%)
        if (profile.getPartnerPreferenceExtended() != null && profile.getPartnerPreferenceExtended().getPartnerMarriagePreference() != null) score += 20;

        return Math.min(100, score);
    }

    public ProfileResponse submitForVerification(Long targetUserId, User requestingUser) {
        if (!targetUserId.equals(requestingUser.getId())) {
            throw new RuntimeException("You can only submit your own profile for verification");
        }
        Profile profile = profileRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        if (profile.getFullName() == null || profile.getFullName().trim().isEmpty() ||
            profile.getDateOfBirth() == null ||
            profile.getBirthDetails() == null || profile.getBirthDetails().getBirthTime() == null || profile.getBirthDetails().getBirthPlace() == null ||
            profile.getHeight() == null ||
            profile.getEducation() == null || profile.getEducation().trim().isEmpty() ||
            profile.getOccupation() == null || profile.getOccupation().trim().isEmpty() ||
            (profile.getUser().getPhone() == null && profile.getWhatsappNumber() == null) ||
            profile.getExactAddress() == null || profile.getExactAddress().trim().isEmpty() ||
            profile.getFamilyDetailsExtended() == null || profile.getFamilyDetailsExtended().getFatherName() == null || profile.getFamilyDetailsExtended().getFatherName().trim().isEmpty() ||
            profile.getFamilyDetailsExtended().getMotherName() == null || profile.getFamilyDetailsExtended().getMotherName().trim().isEmpty() ||
            ((profile.getPartnerPreference() == null || profile.getPartnerPreference().trim().isEmpty()) &&
             (profile.getPartnerPreferenceExtended() == null || profile.getPartnerPreferenceExtended().getPartnerMarriagePreference() == null))) {
            throw new IllegalArgumentException("Missing mandatory fields for verification");
        }
        
        List<ProfilePhoto> photos = profilePhotoRepository.findByUserId(targetUserId).stream()
                .filter(p -> p.getStatus() == PhotoStatus.APPROVED || p.getStatus() == PhotoStatus.PENDING)
                .collect(Collectors.toList());
        if (photos.stream().filter(p -> !p.getIsPrimary()).count() < 2) {
             throw new IllegalArgumentException("Missing mandatory gallery photos for verification");
        }

        profile.setVerificationStatus(com.shimpimilan.model.profile.VerificationStatus.SUBMITTED_FOR_VERIFICATION);
        Profile savedProfile = profileRepository.save(profile);
        return mapToResponse(savedProfile, requestingUser);
    }

    public Page<ProfileResponse> getPendingVerificationProfiles(User adminUser, Pageable pageable) {
        Specification<Profile> spec = (root, query, cb) -> cb.equal(root.get("verificationStatus"), com.shimpimilan.model.profile.VerificationStatus.SUBMITTED_FOR_VERIFICATION);
        return profileRepository.findAll(spec, pageable).map(p -> mapToResponse(p, adminUser));
    }

    public Page<ProfileResponse> getProfilesByVerificationStatus(com.shimpimilan.model.profile.VerificationStatus status, User adminUser, Pageable pageable) {
        Specification<Profile> spec = (root, query, cb) -> cb.equal(root.get("verificationStatus"), status);
        return profileRepository.findAll(spec, pageable).map(p -> mapToResponse(p, adminUser));
    }

    public ProfileResponse approveRejectProfile(Long profileId, boolean approve, String rejectionReason, User adminUser) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        if (approve) {
            profile.setVerificationStatus(com.shimpimilan.model.profile.VerificationStatus.APPROVED);
            profile.setIsVerifiedProfile(true);
            profile.setIsAdminVerified(true);
        } else {
            profile.setVerificationStatus(com.shimpimilan.model.profile.VerificationStatus.REJECTED);
            profile.setRejectionReason(rejectionReason);
        }
        return mapToResponse(profileRepository.save(profile), adminUser);
    }

    private ProfileResponse mapToResponse(Profile profile, User requestingUser) {
        boolean isOwner = requestingUser != null && requestingUser.getId().equals(profile.getUser().getId());
        List<ProfilePhoto> allPhotos = profilePhotoRepository.findByUserId(profile.getUser().getId());

        String photoUrlVal = null;
        if (isOwner) {
            photoUrlVal = allPhotos.stream()
                    .filter(photo -> photo.getPhotoType() == PhotoType.PRIMARY)
                    .sorted((p1, p2) -> p2.getId().compareTo(p1.getId()))
                    .map(ProfilePhoto::getPhotoUrl)
                    .findFirst()
                    .orElse(
                        allPhotos.stream()
                            .filter(ProfilePhoto::getIsPrimary)
                            .map(ProfilePhoto::getPhotoUrl)
                            .findFirst()
                            .orElse(null)
                    );
        } else {
            photoUrlVal = allPhotos.stream()
                    .filter(ProfilePhoto::getIsPrimary)
                    .map(ProfilePhoto::getPhotoUrl)
                    .findFirst()
                    .orElse(null);
        }

        List<String> galleryPhotosVal = null;
        if (isOwner) {
            galleryPhotosVal = allPhotos.stream()
                    .filter(photo -> photo.getPhotoType() == PhotoType.GALLERY)
                    .map(ProfilePhoto::getPhotoUrl)
                    .collect(Collectors.toList());
        } else {
            galleryPhotosVal = allPhotos.stream()
                    .filter(photo -> photo.getPhotoType() == PhotoType.GALLERY && photo.getStatus() == PhotoStatus.APPROVED)
                    .map(ProfilePhoto::getPhotoUrl)
                    .collect(Collectors.toList());
        }

        ProfileResponse response = ProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .fullName(profile.getFullName())
                .email(profile.getEmail())
                .gender(profile.getGender())
                .community(profile.getCommunity())
                .dateOfBirth(profile.getDateOfBirth())
                .age(profile.getAge())
                .height(profile.getHeight())
                .weight(profile.getWeight())
                .bloodGroup(profile.getBloodGroup())
                .religion(profile.getReligion())
                .gotra(profile.getGotra())
                .manglik(profile.getManglik())
                .maritalStatus(profile.getMaritalStatus())
                .education(profile.getEducation())
                .occupation(profile.getOccupation())
                .company(profile.getCompany())
                .annualIncome(profile.getAnnualIncome())
                .city(profile.getCity())
                .district(profile.getDistrict())
                .state(profile.getState())
                .country(profile.getCountry())
                .village(profile.getVillage())
                .pincode(profile.getPincode())
                .familyDetails(profile.getFamilyDetails())
                .familyType(profile.getFamilyType())
                .lifestyle(profile.getLifestyle())
                .aboutMe(profile.getAboutMe())
                .partnerPreference(profile.getPartnerPreference())
                .profilePhotoUrl(photoUrlVal)
                .galleryPhotos(galleryPhotosVal)
                .profileType(profile.getProfileType())
                .alternateMobile(profile.getAlternateMobile())
                .birthDetails(profile.getBirthDetails())
                .horoscopeDetails(profile.getHoroscopeDetails())
                .physicalDetails(profile.getPhysicalDetails())
                .educationCareerDetails(profile.getEducationCareerDetails())
                .familyDetailsExtended(profile.getFamilyDetailsExtended())
                .partnerPreferenceExtended(profile.getPartnerPreferenceExtended())
                .lifestyleExtended(profile.getLifestyleExtended())
                .passportDetails(profile.getPassportDetails())
                .verificationStatus(profile.getVerificationStatus())
                .rejectionReason(profile.getRejectionReason())
                .profileCompletionPercentage(profile.getProfileCompletionPercentage())
                .isPremiumMember(profile.getIsPremiumMember())
                .isVerifiedProfile(profile.getIsVerifiedProfile())
                .isMobileVerified(profile.getIsMobileVerified())
                .isEmailVerified(profile.getIsEmailVerified())
                .isAdminVerified(profile.getIsAdminVerified())
                .createdAt(profile.getCreatedAt())
                .build();

        // Privacy rules
        contactUnlockService.applyMasking(response, profile, requestingUser);

        return response;
    }
}
