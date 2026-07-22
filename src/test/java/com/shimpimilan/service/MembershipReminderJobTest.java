package com.shimpimilan.service;

import com.shimpimilan.model.PlanType;
import com.shimpimilan.model.Profile;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.ProfileRepository;
import com.shimpimilan.repository.UserRepository;
import com.shimpimilan.service.notification.MembershipReminderJob;
import com.shimpimilan.service.notification.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class MembershipReminderJobTest {

    @Autowired
    private MembershipReminderJob membershipReminderJob;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private NotificationService notificationService;

    @Test
    public void testCheckExpiries() {
        User u = userRepository.save(User.builder().phone("1234567899").passwordHash("pass").role(com.shimpimilan.model.Role.USER)
                .status(com.shimpimilan.model.UserStatus.APPROVED).community(com.shimpimilan.model.Community.AHER_SHIMPI)
                .accountType(com.shimpimilan.model.AccountType.SELF).freeTrialUsed(false).referralCode("REMINDER_USER").build());

        // Set expiry to exactly 3 days from now
        Profile p = Profile.builder()
                .user(u)
                .fullName("Test Profile")
                .planType(PlanType.PREMIUM)
                .premiumExpiryDate(LocalDateTime.now().plusDays(3))
                .isVerifiedProfile(true)
                .build();
        profileRepository.save(p);

        // Run Job
        membershipReminderJob.checkExpiries();

        // Verify that notification service was called with template 'premium-expiry-reminder'
        verify(notificationService, times(1)).notifyUser(eq(u.getId()), eq("premium-expiry-reminder"), anyString(), anyString(), any());
    }
}
