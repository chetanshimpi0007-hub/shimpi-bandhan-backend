package com.shimpimilan.service;

import com.shimpimilan.model.Kundali;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class KundaliServiceTest {

    @Autowired
    private KundaliService kundaliService;

    @Test
    public void testCalculateGunMilan() {
        Kundali k1 = Kundali.builder().id(1L).isManglik(false).build();
        Kundali k2 = Kundali.builder().id(2L).isManglik(false).build();

        Map<String, Object> result = kundaliService.calculateGunMilan(1L, k1, k2);

        assertNotNull(result);
        assertTrue(result.containsKey("gunMilanScore"));
        assertTrue(result.containsKey("matchingPercentage"));
        assertTrue(result.containsKey("mangalDoshaStatus"));
        assertTrue(result.containsKey("compatibilitySummary"));
        
        Double score = (Double) result.get("gunMilanScore");
        assertTrue(score >= 18.0 && score <= 36.0);
    }

    @Test
    public void testRateLimit() {
        Kundali k1 = Kundali.builder().id(10L).isManglik(false).build();
        Kundali k2 = Kundali.builder().id(20L).isManglik(false).build();
        
        Long userId = 999L; // Isolated user ID for rate limiting
        
        // Consume 4 more matches (1 already might have been consumed if we share state, but we use a distinct user)
        for (int i = 0; i < 5; i++) {
            kundaliService.calculateGunMilan(userId, k1, k2);
        }
        
        // The 6th should fail
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
            kundaliService.calculateGunMilan(userId, k1, k2);
        });
        
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    }
}
