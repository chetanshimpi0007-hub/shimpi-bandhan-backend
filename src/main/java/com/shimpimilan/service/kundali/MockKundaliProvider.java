package com.shimpimilan.service.kundali;

import com.shimpimilan.model.Kundali;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Component
@ConditionalOnProperty(name = "kundali.provider", havingValue = "mock", matchIfMissing = true)
public class MockKundaliProvider implements KundaliProvider {

    @Override
    public Map<String, Object> calculateMatch(Kundali k1, Kundali k2) {
        Random random = new Random((k1.getId() != null ? k1.getId() : 1L) + (k2.getId() != null ? k2.getId() : 1L));
        
        double score = 18.0 + (random.nextDouble() * 18.0);
        score = Math.round(score * 10.0) / 10.0;
        
        double percentage = (score / 36.0) * 100.0;
        percentage = Math.round(percentage * 10.0) / 10.0;

        boolean bothManglik = Boolean.TRUE.equals(k1.getIsManglik()) && Boolean.TRUE.equals(k2.getIsManglik());
        boolean noneManglik = Boolean.FALSE.equals(k1.getIsManglik()) && Boolean.FALSE.equals(k2.getIsManglik());
        
        String mangalDoshaStatus;
        if (bothManglik) {
            mangalDoshaStatus = "Both Manglik - Dosha Cancelled";
        } else if (noneManglik) {
            mangalDoshaStatus = "No Mangal Dosha";
        } else {
            mangalDoshaStatus = "One Manglik - Mangal Dosha Present";
        }

        String summary;
        if (score >= 18 && !mangalDoshaStatus.contains("Dosha Present")) {
            summary = "Excellent Compatibility. Suitable for marriage.";
        } else if (score >= 18) {
            summary = "Good Compatibility but Mangal Dosha present. Consult an astrologer.";
        } else {
            summary = "Below average compatibility.";
        }

        Map<String, Object> result = new HashMap<>();
        result.put("gunMilanScore", score);
        result.put("maxScore", 36.0);
        result.put("matchingPercentage", percentage);
        result.put("mangalDoshaStatus", mangalDoshaStatus);
        result.put("compatibilitySummary", summary);
        
        return result;
    }
}
