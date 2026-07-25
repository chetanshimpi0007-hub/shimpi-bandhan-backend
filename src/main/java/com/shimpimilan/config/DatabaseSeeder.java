package com.shimpimilan.config;

import com.shimpimilan.model.*;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final com.shimpimilan.repository.SuccessStoryRepository successStoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdmin();
        seedMalePremium();
        seedFemalePremium();
        seedMaleFree();
        seedFemaleFree();
        seedSuccessStories();
    }

    private void seedAdmin() {
        java.util.Optional<User> adminOpt = userRepository.findByPhone("0000000000");
        User admin;
        if (adminOpt.isPresent()) {
            admin = adminOpt.get();
            admin.setPasswordHash(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setStatus(UserStatus.APPROVED);
            userRepository.save(admin);
        } else {
            admin = User.builder()
                    .phone("0000000000")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .role(Role.ADMIN)
                    .status(UserStatus.APPROVED)
                    .community(Community.AHER_SHIMPI)
                    .build();
            userRepository.save(admin);

            Profile profile = Profile.builder()
                    .user(admin)
                    .email("admin@shimpibandhan.com")
                    .fullName("Super Admin")
                    .isPremiumMember(true)
                    .isVerifiedProfile(true)
                    .gender(Gender.MALE)
                    .build();
            profileRepository.save(profile);
        }

        // Ensure all ADMIN role users in database have APPROVED status
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ADMIN)
                .forEach(u -> {
                    if (u.getStatus() != UserStatus.APPROVED) {
                        u.setStatus(UserStatus.APPROVED);
                        userRepository.save(u);
                    }
                });
    }

    private void seedMalePremium() {
        if (!userRepository.existsByPhone("1111111111")) {
            User user = User.builder()
                    .phone("1111111111")
                    .passwordHash(passwordEncoder.encode("Test@123"))
                    .role(Role.USER)
                    .status(UserStatus.APPROVED)
                    .community(Community.AHER_SHIMPI)
                    .build();
            userRepository.save(user);

            Profile profile = Profile.builder()
                    .user(user)
                    .email("male@test.com")
                    .fullName("Test Male Premium")
                    .isPremiumMember(true)
                    .isVerifiedProfile(true)
                    .gender(Gender.MALE)
                    .dateOfBirth(LocalDate.of(1995, 1, 1))
                    .height(5.8)
                    .education("B.Tech")
                    .occupation("Software Engineer")
                    .city("Pune")
                    .state("Maharashtra")
                    .build();
            profileRepository.save(profile);
        }
    }

    private void seedFemalePremium() {
        if (!userRepository.existsByPhone("2222222222")) {
            User user = User.builder()
                    .phone("2222222222")
                    .passwordHash(passwordEncoder.encode("Test@123"))
                    .role(Role.USER)
                    .status(UserStatus.APPROVED)
                    .community(Community.AHER_SHIMPI)
                    .build();
            userRepository.save(user);

            Profile profile = Profile.builder()
                    .user(user)
                    .email("female@test.com")
                    .fullName("Test Female Premium")
                    .isPremiumMember(true)
                    .isVerifiedProfile(true)
                    .gender(Gender.FEMALE)
                    .dateOfBirth(LocalDate.of(1996, 5, 15))
                    .height(5.4)
                    .education("MBA")
                    .occupation("HR Manager")
                    .city("Mumbai")
                    .state("Maharashtra")
                    .build();
            profileRepository.save(profile);
        }
    }

    private void seedMaleFree() {
        if (!userRepository.existsByPhone("3333333333")) {
            User user = User.builder()
                    .phone("3333333333")
                    .passwordHash(passwordEncoder.encode("Test@123"))
                    .role(Role.USER)
                    .status(UserStatus.APPROVED)
                    .community(Community.NAMDEV_SHIMPI)
                    .build();
            userRepository.save(user);

            Profile profile = Profile.builder()
                    .user(user)
                    .email("malefree@test.com")
                    .fullName("Test Male Free")
                    .isPremiumMember(false)
                    .isVerifiedProfile(false)
                    .gender(Gender.MALE)
                    .dateOfBirth(LocalDate.of(1998, 3, 10))
                    .height(5.6)
                    .education("B.Com")
                    .occupation("Accountant")
                    .city("Nashik")
                    .state("Maharashtra")
                    .build();
            profileRepository.save(profile);
        }
    }

    private void seedFemaleFree() {
        if (!userRepository.existsByPhone("4444444444")) {
            User user = User.builder()
                    .phone("4444444444")
                    .passwordHash(passwordEncoder.encode("Test@123"))
                    .role(Role.USER)
                    .status(UserStatus.APPROVED)
                    .community(Community.NAMDEV_SHIMPI)
                    .build();
            userRepository.save(user);

            Profile profile = Profile.builder()
                    .user(user)
                    .email("femalefree@test.com")
                    .fullName("Test Female Free")
                    .isPremiumMember(false)
                    .isVerifiedProfile(false)
                    .gender(Gender.FEMALE)
                    .dateOfBirth(LocalDate.of(1999, 8, 20))
                    .height(5.2)
                    .education("B.Sc")
                    .occupation("Teacher")
                    .city("Nagpur")
                    .state("Maharashtra")
                    .build();
            profileRepository.save(profile);
        }
    }

    private void seedSuccessStories() {
        if (successStoryRepository.count() == 0) {
            successStoryRepository.save(SuccessStory.builder()
                    .brideName("Priya Shimpi")
                    .groomName("Ramesh Shimpi")
                    .weddingDate(LocalDate.of(2026, 5, 18))
                    .city("Nashik")
                    .shortStory("The compatibility scoring on Shimpi Bandhan matched us based on our exact sub-caste and lifestyle choices.")
                    .story("We met on Shimpi Bandhan in November 2025. Our families connected over a family discussion room call, and after meeting in person at a community gathering in Nashik, we decided to get married in May 2026. The platform's verified badges and privacy features gave us complete peace of mind.")
                    .photoUrl("/priya-ramesh.jpg")
                    .galleryImages("/priya-ramesh.jpg,/wedding-couple.jpg,/shadi-couple.jpg")
                    .displayOrder(1)
                    .isFeatured(true)
                    .isPublished(true)
                    .build());

            successStoryRepository.save(SuccessStory.builder()
                    .brideName("Sonal Sankpal")
                    .groomName("Shubham Sankpal")
                    .weddingDate(LocalDate.of(2026, 2, 14))
                    .city("Pune")
                    .shortStory("We met on Shimpi Bandhan in November and got married in February. Verified badges made us feel completely secure.")
                    .story("Finding a partner within the Namdev Shimpi community was very important for both our families. Shimpi Bandhan made it so effortless with precise filters. From our first chat to our engagement, everything felt seamless and trustworthy.")
                    .photoUrl("/sonal-shubham-new.jpg")
                    .galleryImages("/sonal-shubham-new.jpg,/hero-wedding.jpg,/newest-hero.jpg")
                    .displayOrder(2)
                    .isFeatured(true)
                    .isPublished(true)
                    .build());

            successStoryRepository.save(SuccessStory.builder()
                    .brideName("Pooja Shimpi")
                    .groomName("Aniket Shimpi")
                    .weddingDate(LocalDate.of(2025, 12, 10))
                    .city("Mumbai")
                    .shortStory("A traditional match powered by modern AI compatibility scoring. Grateful to Shimpi Bandhan for bringing us together!")
                    .story("Aniket sent me an interest request on Shimpi Bandhan after seeing my verified profile. Our parents spoke the next day and arranged a family meet in Mumbai. Within two months, our wedding was fixed!")
                    .photoUrl("/shadi-couple.jpg")
                    .galleryImages("/shadi-couple.jpg,/priya-ramesh.jpg,/sonal-shubham-new.jpg")
                    .displayOrder(3)
                    .isFeatured(true)
                    .isPublished(true)
                    .build());

            successStoryRepository.save(SuccessStory.builder()
                    .brideName("Neha Shimpi")
                    .groomName("Vikram Shimpi")
                    .weddingDate(LocalDate.of(2025, 11, 25))
                    .city("Aurangabad")
                    .shortStory("Connected during the Pune Melava event! Shimpi Bandhan's digital platform made our communication smooth and secure.")
                    .story("We first noticed each other's profiles on Shimpi Bandhan before attending the regional Shimpi Melava. Having full family approval and verified details beforehand made the meeting relaxed and memorable.")
                    .photoUrl("/wedding-couple.jpg")
                    .galleryImages("/wedding-couple.jpg,/shadi-couple.jpg,/hero-wedding.jpg")
                    .displayOrder(4)
                    .isFeatured(true)
                    .isPublished(true)
                    .build());

            successStoryRepository.save(SuccessStory.builder()
                    .brideName("Smita Shimpi")
                    .groomName("Rajesh Shimpi")
                    .weddingDate(LocalDate.of(2025, 9, 15))
                    .city("Nagpur")
                    .shortStory("Two Shimpi familiesunited across cities. Shimpi Bandhan made remote profile viewing and video calls super easy.")
                    .story("Living in different cities made traditional matchmaking challenging until we created profiles on Shimpi Bandhan. The instant chat and video verification features helped us build trust quickly.")
                    .photoUrl("/hero-wedding.jpg")
                    .galleryImages("/hero-wedding.jpg,/newest-hero.jpg,/priya-ramesh.jpg")
                    .displayOrder(5)
                    .isFeatured(true)
                    .isPublished(true)
                    .build());
        }
    }
}
