package com.shimpimilan.service;

import com.shimpimilan.model.Interest;
import com.shimpimilan.model.InterestStatus;
import com.shimpimilan.model.User;
import com.shimpimilan.repository.InterestRepository;
import com.shimpimilan.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class InterestServiceTest {

    @Autowired
    private InterestService interestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Test
    public void testMutualInterest() {
        User u1 = userRepository.save(User.builder().phone("1919191919").passwordHash("pass").role(com.shimpimilan.model.Role.USER)
                .status(com.shimpimilan.model.UserStatus.APPROVED).community(com.shimpimilan.model.Community.AHER_SHIMPI)
                .accountType(com.shimpimilan.model.AccountType.SELF).freeTrialUsed(false).referralCode("REF19").build());
        User u2 = userRepository.save(User.builder().phone("2828282828").passwordHash("pass").role(com.shimpimilan.model.Role.USER)
                .status(com.shimpimilan.model.UserStatus.APPROVED).community(com.shimpimilan.model.Community.AHER_SHIMPI)
                .accountType(com.shimpimilan.model.AccountType.SELF).freeTrialUsed(false).referralCode("REF28").build());

        // Not established initially
        assertFalse(interestService.isMutualInterestEstablished(u1.getId(), u2.getId()));

        // Send interest
        Interest interest = interestService.sendInterest(u1, u2.getId());
        assertEquals(InterestStatus.PENDING, interest.getStatus());
        
        // Still not established
        assertFalse(interestService.isMutualInterestEstablished(u1.getId(), u2.getId()));

        // Accept interest
        interestService.updateInterestStatus(u2, interest.getId(), InterestStatus.ACCEPTED);

        // Now Mutual Interest should be true
        assertTrue(interestService.isMutualInterestEstablished(u1.getId(), u2.getId()));
        assertTrue(interestService.isMutualInterestEstablished(u2.getId(), u1.getId()));
    }
}
