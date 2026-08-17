package com.ka4piece.strategy;

import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.Vacancy;

public interface MatchStrategy {
    double score(JobseekerProfile profile, Vacancy vacancy);
}
