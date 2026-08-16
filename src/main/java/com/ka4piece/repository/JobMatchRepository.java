package com.ka4piece.repository;

import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.Vacancy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JobMatchRepository extends CSVStore {
    private String jobseekerFilePath, vacancyFilePath;

    public JobMatchRepository(String jobseekerFilePath, String vacancyFilePath) {
        this.jobseekerFilePath = jobseekerFilePath;
        this.vacancyFilePath = vacancyFilePath;
    }

    //-----Jobseeker methods-----

    public void saveJobseeker(JobseekerProfile p) {
        List<String> lines = readLines(jobseekerFilePath);
        boolean replaced = false;

        // keyed on jobseekerId alone -- this is what makes recordApplication/updateApplicationStatus
        // work, since the whole row (including appliedVacancies) gets overwritten on every save
        for (int i = 0; i < lines.size(); i++) {
            JobseekerProfile existing = parseJobseeker(lines.get(i));
            if (existing.getJobseekerId().equals(p.getJobseekerId())) {
                lines.set(i, serializeJobseeker(p));
                replaced = true;
                break;
            }
        }

        if (replaced) writeLines(jobseekerFilePath, lines);
        else appendLine(jobseekerFilePath, serializeJobseeker(p));
    }

    public JobseekerProfile findJobseekerById(String jobseekerId) {
        for (String line : readLines(jobseekerFilePath)) {
            JobseekerProfile p = parseJobseeker(line);
            if (p.getJobseekerId().equals(jobseekerId)) return p;
        }
        return null; //not found
    }

    public List<JobseekerProfile> findJobseekersByHousehold(String householdId) {
        List<JobseekerProfile> profiles = new ArrayList<>();
        for (String line : readLines(jobseekerFilePath)) {
            JobseekerProfile p = parseJobseeker(line);
            if (p.getHouseholdId().equals(householdId)) profiles.add(p);
        }
        return profiles;
    }

    public List<JobseekerProfile> findAllJobseekers() {
        List<JobseekerProfile> profiles = new ArrayList<>();
        for (String line : readLines(jobseekerFilePath)) {
            profiles.add(parseJobseeker(line));
        }
        return profiles;
    }

    //-----Vacancy methods-----

    public void saveVacancy(Vacancy v) {
        // vacancies are never edited post-creation in MVP scope -- always append, never overwrite
        appendLine(vacancyFilePath, serializeVacancy(v));
    }

    public Vacancy findVacancyById(String vacancyId) {
        for (String line : readLines(vacancyFilePath)) {
            Vacancy v = parseVacancy(line);
            if (v.getVacancyId().equals(vacancyId)) return v;
        }
        return null; //not found
    }

    public List<Vacancy> findAllVacancies() {
        List<Vacancy> vacancies = new ArrayList<>();
        for (String line : readLines(vacancyFilePath)) {
            vacancies.add(parseVacancy(line));
        }
        return vacancies;
    }

    //-----Helper methods-----

    private JobseekerProfile parseJobseeker(String row) {
        String[] f = row.split(",", -1);
        // column order: jobseekerId, householdId, memberName, education, skills,
        //               experienceYears, location, appliedVacancies

        String jobseekerId = f[0];
        String householdId = f[1];
        String memberName = f[2];
        String education = f[3];

        List<String> skills = f[4].isEmpty()
            ? new ArrayList<>()
            : Arrays.asList(f[4].split(";"));

        int experienceYears = Integer.parseInt(f[5]);
        String location = f[6];

        Map<String, String> appliedVacancies = new HashMap<>();
        if (!f[7].isEmpty()) {
            for (String entry : f[7].split(";")) {
                String[] kv = entry.split(":", 2); // limit 2 in case status ever contains ":"
                appliedVacancies.put(kv[0], kv[1]);
            }
        }

        return new JobseekerProfile(jobseekerId, householdId, memberName, education, location,
                skills, experienceYears, appliedVacancies);
    }

    private String serializeJobseeker(JobseekerProfile p) {
        String skillsJoined = String.join(";", p.getSkills());

        String appliedJoined = p.getAppliedVacancies().entrySet().stream()
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(";"));

        return String.join(",", p.getJobseekerId(), p.getHouseholdId(), p.getMemberName(),
                p.getEducation(), skillsJoined, Integer.toString(p.getExperienceYears()),
                p.getLocation(), appliedJoined);
    }

    private Vacancy parseVacancy(String row) {
        String[] f = row.split(",", -1);
        // column order: vacancyId, title, educationRequirement, skillRequirements,
        //               experienceYearsRequired, location, compensation, type, enteredByOfficialId

        String vacancyId = f[0];
        String title = f[1];
        String educationRequirement = f[2];

        List<String> skillRequirements = f[3].isEmpty()
            ? new ArrayList<>()
            : Arrays.asList(f[3].split(";"));

        int experienceYearsRequired = Integer.parseInt(f[4]);
        String location = f[5];
        String compensation = f[6];
        String type = f[7];
        String enteredByOfficialId = f[8];

        return new Vacancy(vacancyId, title, educationRequirement, location, compensation, type, skillRequirements,
                experienceYearsRequired, enteredByOfficialId);
    }

    private String serializeVacancy(Vacancy v) {
        String skillsJoined = String.join(";", v.getSkillRequirements());

        return String.join(",", v.getVacancyId(), v.getTitle(), v.getEducationRequirement(),
                skillsJoined, Integer.toString(v.getExperienceYearsRequired()), v.getLocation(),
                v.getCompensation(), v.getType(), v.getEnteredByOfficialId());
    }
}