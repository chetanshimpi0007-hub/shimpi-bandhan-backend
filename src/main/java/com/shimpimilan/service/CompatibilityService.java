package com.shimpimilan.service;

import com.shimpimilan.dto.CompatibilityResultDTO;
import com.shimpimilan.model.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompatibilityService {

    public CompatibilityResultDTO calculateCompatibility(Profile userProfile, Profile targetProfile) {
        Map<String, Integer> breakdown = new HashMap<>();
        List<String> strengths = new ArrayList<>();
        List<String> differences = new ArrayList<>();

        int totalScore = 0;

        // 1. Age Match (10%)
        int ageScore = calculateAgeMatch(userProfile.getAge(), targetProfile.getAge());
        breakdown.put("Age Match", ageScore);
        totalScore += (ageScore * 10 / 100);
        addReason(ageScore, "Age", strengths, differences);

        // 2. Education Match (10%)
        int eduScore = calculateStringMatch(userProfile.getEducation(), targetProfile.getEducation());
        breakdown.put("Education Match", eduScore);
        totalScore += (eduScore * 10 / 100);
        addReason(eduScore, "Education", strengths, differences);

        // 3. Occupation Match (10%)
        int occScore = calculateStringMatch(userProfile.getOccupation(), targetProfile.getOccupation());
        breakdown.put("Occupation Match", occScore);
        totalScore += (occScore * 10 / 100);
        addReason(occScore, "Occupation", strengths, differences);

        // 4. Annual Income (10%)
        int incScore = calculateIncomeMatch(userProfile.getAnnualIncome(), targetProfile.getAnnualIncome());
        breakdown.put("Annual Income Match", incScore);
        totalScore += (incScore * 10 / 100);
        addReason(incScore, "Income", strengths, differences);

        // 5. Height Match (5%)
        int heightScore = calculateHeightMatch(userProfile.getHeight(), targetProfile.getHeight());
        breakdown.put("Height Match", heightScore);
        totalScore += (heightScore * 5 / 100);
        addReason(heightScore, "Height", strengths, differences);

        // 6. Religion (10%)
        int relScore = calculateStringMatch(userProfile.getReligion(), targetProfile.getReligion());
        breakdown.put("Religion Match", relScore);
        totalScore += (relScore * 10 / 100);
        addReason(relScore, "Religion", strengths, differences);

        // 7. Community (15%)
        int commScore = calculateEnumMatch(userProfile.getCommunity(), targetProfile.getCommunity());
        breakdown.put("Community Match", commScore);
        totalScore += (commScore * 15 / 100);
        addReason(commScore, "Community", strengths, differences);

        // 8. Gotra (5%) - Usually Gotra should NOT match for Hindu marriages
        int gotraScore = calculateGotraMatch(userProfile.getGotra(), targetProfile.getGotra());
        breakdown.put("Gotra Match", gotraScore);
        totalScore += (gotraScore * 5 / 100);
        if (gotraScore > 80) strengths.add("Different Gotras (Good)");
        else differences.add("Same Gotra (May require astrological advice)");

        // 9. Manglik (5%)
        int manglikScore = calculateBooleanMatch(userProfile.getManglik(), targetProfile.getManglik());
        breakdown.put("Manglik Match", manglikScore);
        totalScore += (manglikScore * 5 / 100);
        addReason(manglikScore, "Manglik status", strengths, differences);

        // 10. Location (10%)
        int locScore = calculateLocationMatch(userProfile, targetProfile);
        breakdown.put("Location Match", locScore);
        totalScore += (locScore * 10 / 100);
        addReason(locScore, "Location", strengths, differences);

        // 11. Family Type (5%)
        int famScore = calculateStringMatch(userProfile.getFamilyType(), targetProfile.getFamilyType());
        breakdown.put("Family Type Match", famScore);
        totalScore += (famScore * 5 / 100);
        addReason(famScore, "Family values", strengths, differences);

        // 12. Lifestyle (5%)
        int lifeScore = calculateStringMatch(userProfile.getLifestyle(), targetProfile.getLifestyle());
        breakdown.put("Lifestyle Match", lifeScore);
        totalScore += (lifeScore * 5 / 100);
        addReason(lifeScore, "Lifestyle habits", strengths, differences);

        return CompatibilityResultDTO.builder()
                .overallCompatibilityPercentage(totalScore)
                .detailedBreakdown(breakdown)
                .strengths(strengths)
                .possibleDifferences(differences)
                .build();
    }

    private void addReason(int score, String trait, List<String> strengths, List<String> differences) {
        if (score >= 80) strengths.add(trait + " is a strong match.");
        else if (score <= 50) differences.add(trait + " differs.");
    }

    private int calculateAgeMatch(Integer age1, Integer age2) {
        if (age1 == null || age2 == null) return 50;
        int diff = Math.abs(age1 - age2);
        if (diff <= 3) return 100;
        if (diff <= 5) return 80;
        if (diff <= 8) return 60;
        return 40;
    }

    private int calculateStringMatch(String s1, String s2) {
        if (s1 == null || s2 == null) return 50;
        return s1.equalsIgnoreCase(s2) ? 100 : 40;
    }

    private int calculateEnumMatch(Enum<?> e1, Enum<?> e2) {
        if (e1 == null || e2 == null) return 50;
        return e1.equals(e2) ? 100 : 0;
    }

    private int calculateIncomeMatch(Double i1, Double i2) {
        if (i1 == null || i2 == null) return 50;
        double diff = Math.abs(i1 - i2);
        if (diff <= 300000) return 100;
        if (diff <= 600000) return 80;
        return 60;
    }

    private int calculateHeightMatch(Double h1, Double h2) {
        if (h1 == null || h2 == null) return 50;
        double diff = Math.abs(h1 - h2);
        if (diff <= 5.0) return 100; // cm diff
        if (diff <= 10.0) return 80;
        return 60;
    }

    private int calculateGotraMatch(String g1, String g2) {
        if (g1 == null || g2 == null) return 100;
        return g1.equalsIgnoreCase(g2) ? 20 : 100; // Same gotra = bad
    }

    private int calculateBooleanMatch(Boolean b1, Boolean b2) {
        if (b1 == null || b2 == null) return 50;
        return b1.equals(b2) ? 100 : 20;
    }

    private int calculateLocationMatch(Profile p1, Profile p2) {
        if (p1.getCity() != null && p1.getCity().equalsIgnoreCase(p2.getCity())) return 100;
        if (p1.getDistrict() != null && p1.getDistrict().equalsIgnoreCase(p2.getDistrict())) return 80;
        if (p1.getState() != null && p1.getState().equalsIgnoreCase(p2.getState())) return 60;
        return 40;
    }
}
