package com.ka4piece.strategy;

import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.Vacancy;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class WeightedMatchStrategy implements MatchStrategy {
    double w1 = 0.30;  // education
    double w2 = 0.35;  // skills
    double w3 = 0.20;  // experience
    double w4 = 0.15;  // location

    @Override
    public double score(JobseekerProfile profile, Vacancy vacancy) {
        Map<String, Integer> educationRank = Map.of("HighSchool", 1, "Vocational", 2, "College", 3);
        double educationScore =
            educationRank.getOrDefault(profile.getEducation(), 0)
                >= educationRank.getOrDefault(vacancy.getEducationRequirement(), 0)
                ? 1.0 : 0.0;

        Set<String> profileSkills = profile.getSkills().stream()
            .map(s -> s.trim().toLowerCase()).collect(Collectors.toSet());
        Set<String> requiredSkills = vacancy.getSkillRequirements().stream()
            .map(s -> s.trim().toLowerCase()).collect(Collectors.toSet());
        double skillScore;
        if (requiredSkills.isEmpty()) {
            skillScore = 1.0;
        } else {
            long overlap = requiredSkills.stream().filter(profileSkills::contains).count();
            skillScore = (double) overlap / requiredSkills.size();
        }

        double experienceScore = vacancy.getExperienceYearsRequired() == 0
            ? 1.0
            : Math.min((double) profile.getExperienceYears() / vacancy.getExperienceYearsRequired(), 1.0);

        double locationScore =
            profile.getLocation().trim().equalsIgnoreCase(vacancy.getLocation().trim())
                ? 1.0 : 0.0;

        return (w1 * educationScore) + (w2 * skillScore) + (w3 * experienceScore) + (w4 * locationScore);
    }
}
