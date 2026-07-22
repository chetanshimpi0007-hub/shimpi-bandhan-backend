package com.shimpimilan.service;

import com.shimpimilan.dto.PublicStatsDTO;
import com.shimpimilan.model.PlanType;
import com.shimpimilan.model.UserStatus;
import com.shimpimilan.model.profile.VerificationStatus;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.SuccessStoryRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicStatsService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SuccessStoryRepository successStoryRepository;

    @Cacheable(value = "publicStats", key = "'homePageStats'")
    public PublicStatsDTO getPublicStats() {
        try {
            long totalMembers = userRepository.countByStatus(UserStatus.APPROVED);
            long verifiedProfiles = profileRepository.countByVerificationStatus(VerificationStatus.APPROVED);
            long premiumMembers = profileRepository.countByPlanType(PlanType.PREMIUM);
            long successStories = successStoryRepository.count();

            return PublicStatsDTO.builder()
                    .totalMembers(totalMembers > 0 ? totalMembers : 0)
                    .verifiedProfiles(verifiedProfiles > 0 ? verifiedProfiles : 0)
                    .premiumMembers(premiumMembers > 0 ? premiumMembers : 0)
                    .successStories(successStories > 0 ? successStories : 0)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching public stats, returning zeros", e);
            return PublicStatsDTO.builder().build(); // all zeros
        }
    }
}
