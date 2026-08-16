package com.ka4piece.repository;

import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.Vacancy;

import java.util.*;
import java.util.stream.Collectors;

public class JobMatchRepository extends MySqlStore {

    public JobMatchRepository(String jdbcUrl, String dbUser, String dbPassword) {
        super(jdbcUrl, dbUser, dbPassword);
    }

    //-----Jobseeker methods-----
    public void saveJobseeker(JobseekerProfile p) {
        String skillsStr = (p.getSkills() == null || p.getSkills().isEmpty()) 
                           ? "" 
                           : String.join(";", p.getSkills());

        String appliedStr = serializeAppliedVacancies(p.getAppliedVacancies());

        String sql = "INSERT INTO jobseekers (jobseekerId, householdId, memberName, education, skills, experienceYears, location, appliedVacancies) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " + // if the same jobseekerId already exists, update the record instead of inserting a new one
                     "householdId = VALUES(householdId), " +
                     "memberName = VALUES(memberName), " +
                     "education = VALUES(education), " +
                     "skills = VALUES(skills), " +
                     "experienceYears = VALUES(experienceYears), " +
                     "location = VALUES(location), " +
                     "appliedVacancies = VALUES(appliedVacancies)";

        executeUpdate(sql,
            p.getJobseekerId(),
            p.getHouseholdId(),
            p.getMemberName(),
            p.getEducation(),
            skillsStr,
            p.getExperienceYears(),
            p.getLocation(),
            appliedStr
        );
    }

    public JobseekerProfile findJobseekerById(String jobseekerId) {
        String sql = "SELECT * FROM jobseekers WHERE jobseekerId = ?";
        List<Map<String, Object>> rows = executeQuery(sql, jobseekerId);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToJobseeker(rows.get(0));
    }

    public List<JobseekerProfile> findJobseekersByHousehold(String householdId) {
        String sql = "SELECT * FROM jobseekers WHERE householdId = ?";
        List<Map<String, Object>> rows = executeQuery(sql, householdId);
        return rows.stream()
                   .map(this::mapRowToJobseeker)
                   .collect(Collectors.toList());
    }

    public List<JobseekerProfile> findAllJobseekers() {
        String sql = "SELECT * FROM jobseekers";
        List<Map<String, Object>> rows = executeQuery(sql);
        return rows.stream()
                   .map(this::mapRowToJobseeker)
                   .collect(Collectors.toList());
    }

    //-----Vacancy methods-----
    public void saveVacancy(Vacancy v) {
        String skillsReqStr = (v.getSkillRequirements() == null || v.getSkillRequirements().isEmpty()) 
                              ? "" 
                              : String.join(";", v.getSkillRequirements());

        String sql = "INSERT INTO vacancies (vacancyId, title, educationRequirement, skillRequirements, " +
                     "experienceYearsRequired, location, compensation, type, enteredByOfficialId) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        executeUpdate(sql,
            v.getVacancyId(),
            v.getTitle(),
            v.getEducationRequirement(),
            skillsReqStr,
            v.getExperienceYearsRequired(),
            v.getLocation(),
            v.getCompensation(),
            v.getType(),
            v.getEnteredByOfficialId()
        );
    }

    public Vacancy findVacancyById(String vacancyId) {
        String sql = "SELECT * FROM vacancies WHERE vacancyId = ?";
        List<Map<String, Object>> rows = executeQuery(sql, vacancyId);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToVacancy(rows.get(0));
    }

    public List<Vacancy> findAllVacancies() {
        String sql = "SELECT * FROM vacancies";
        List<Map<String, Object>> rows = executeQuery(sql);
        return rows.stream()
                   .map(this::mapRowToVacancy)
                   .collect(Collectors.toList());
    }

    //-----Helper methods-----
    private JobseekerProfile mapRowToJobseeker(Map<String, Object> row) {
        String skillsText = (String) row.get("skills");
        List<String> skills = (skillsText == null || skillsText.trim().isEmpty())
                              ? new ArrayList<>()
                              : new ArrayList<>(Arrays.asList(skillsText.split(";")));

        String appliedText = (String) row.get("appliedVacancies");
        Map<String, String> appliedVacancies = parseAppliedVacancies(appliedText);

        return new JobseekerProfile(
            (String) row.get("jobseekerId"),
            (String) row.get("householdId"),
            (String) row.get("memberName"),
            (String) row.get("education"),
            (String) row.get("location"),
            skills,
            ((Number) row.get("experienceYears")).intValue(),
            appliedVacancies
        );
    }

    private Vacancy mapRowToVacancy(Map<String, Object> row) {
        String skillsReqText = (String) row.get("skillRequirements");
        List<String> skillRequirements = (skillsReqText == null || skillsReqText.trim().isEmpty())
                                         ? new ArrayList<>()
                                         : new ArrayList<>(Arrays.asList(skillsReqText.split(";")));

        return new Vacancy(
            (String) row.get("vacancyId"),
            (String) row.get("title"),
            (String) row.get("educationRequirement"),
            (String) row.get("location"),
            (String) row.get("compensation"),
            (String) row.get("type"),
            skillRequirements,
            ((Number) row.get("experienceYearsRequired")).intValue(),
            (String) row.get("enteredByOfficialId")
        );
    }

    private String serializeAppliedVacancies(Map<String, String> applied) {
        if (applied == null || applied.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : applied.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        return sb.toString();
    }

    private Map<String, String> parseAppliedVacancies(String text) {
        Map<String, String> map = new LinkedHashMap<>();
        if (text == null || text.trim().isEmpty()) {
            return map;
        }
        String[] pairs = text.split(";");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}