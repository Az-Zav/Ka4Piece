package com.ka4piece.manager;

import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.RankedCandidate;
import com.ka4piece.model.RankedVacancy;
import com.ka4piece.model.Vacancy;
import com.ka4piece.repository.JobMatchRepository;
import com.ka4piece.strategy.MatchStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class JobMatchManager {
    JobMatchRepository jobMatchRepository;
    MatchStrategy matchStrategy;

    public JobMatchManager(JobMatchRepository jobMatchRepository, MatchStrategy matchStrategy) {
        this.jobMatchRepository = jobMatchRepository;
        this.matchStrategy = matchStrategy;
    }

    /** Creates a new vacancy with a generated ID and sets its status to ACTIVE. */
    public void createVacancy(Vacancy v) {
        v.setVacancyId(jobMatchRepository.generateId("VAC"));
        v.setStatus("ACTIVE");
        jobMatchRepository.saveVacancy(v);
    }

    /** Creates a new jobseeker profile with a generated ID and an empty application map. */
    public void createProfile(JobseekerProfile p) {
        p.setJobseekerId(jobMatchRepository.generateId("JS"));
        p.setAppliedVacancies(new HashMap<>());
        jobMatchRepository.saveJobseeker(p);
    }

    /**
     * Updates an existing jobseeker profile.
     * Thin wrapper -- p.jobseekerId must already be set (editing an existing profile).
     */
    public void updateProfile(JobseekerProfile p) {
        jobMatchRepository.saveJobseeker(p);
    }

    /** Returns all jobseeker profiles that belong to the given household. */
    public List<JobseekerProfile> getProfilesForHousehold(String householdId) {
        return jobMatchRepository.findJobseekersByHousehold(householdId);
    }

    /**
     * Returns all ACTIVE vacancies ranked by match score (descending) for the given jobseeker.
     * Archived/filled vacancies are hidden.
     */
    public List<RankedVacancy> getRankedVacancies(String jobseekerId) {
        JobseekerProfile profile = jobMatchRepository.findJobseekerById(jobseekerId);
        return jobMatchRepository.findAllVacancies().stream()
            .filter(v -> v.getStatus().equals("ACTIVE"))
            .map(v -> new RankedVacancy(v, matchStrategy.score(profile, v)))
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    /**
     * Returns all applicants for the given vacancy, ranked by match score (descending).
     * Only jobseekers who have applied (have the vacancyId key) are included.
     */
    public List<RankedCandidate> getRankedApplicants(String vacancyId) {
        Vacancy vacancy = jobMatchRepository.findVacancyById(vacancyId);
        return jobMatchRepository.findAllJobseekers().stream()
            .filter(p -> p.getAppliedVacancies().containsKey(vacancyId))
            .map(p -> new RankedCandidate(p, matchStrategy.score(p, vacancy),
                                           p.getAppliedVacancies().get(vacancyId)))
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    /**
     * Records an application from a jobseeker for a vacancy.
     * FOR_INTERVIEW and HIRED statuses are protected from being reset.
     * REJECTED or absent -> APPLIED (fixes the reapply-after-rejection gap).
     */
    public void recordApplication(String jobseekerId, String vacancyId) {
        JobseekerProfile p = jobMatchRepository.findJobseekerById(jobseekerId);
        String current = p.getAppliedVacancies().get(vacancyId);
        if (current != null && (current.equals("FOR_INTERVIEW") || current.equals("HIRED"))) {
            return;   // protected -- do not overwrite official's progress
        }
        p.getAppliedVacancies().put(vacancyId, "APPLIED");
        jobMatchRepository.saveJobseeker(p);
    }

    /** Withdraws a jobseeker's application from a vacancy. */
    public void removeApplication(String jobseekerId, String vacancyId) {
        JobseekerProfile p = jobMatchRepository.findJobseekerById(jobseekerId);
        p.getAppliedVacancies().remove(vacancyId);
        jobMatchRepository.saveJobseeker(p);
    }

    /**
     * Updates the application status for a jobseeker on a vacancy.
     * status must be one of: "FOR_INTERVIEW", "REJECTED", or "HIRED" -- caller validates.
     * If HIRED, the vacancy is automatically archived with reason "Filled".
     */
    public void updateApplicationStatus(String jobseekerId, String vacancyId, String status) {
        JobseekerProfile p = jobMatchRepository.findJobseekerById(jobseekerId);
        p.getAppliedVacancies().put(vacancyId, status);
        jobMatchRepository.saveJobseeker(p);

        if (status.equals("HIRED")) {
            jobMatchRepository.updateVacancyStatus(vacancyId, "ARCHIVED", "Filled", jobseekerId);
        }
    }

    /** Returns the full details of a single vacancy. */
    public Vacancy getVacancyDetail(String vacancyId) {
        return jobMatchRepository.findVacancyById(vacancyId);
    }
}
