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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedAdmin();
        seedMalePremium();
        seedFemalePremium();
        seedMaleFree();
        seedFemaleFree();
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
}
