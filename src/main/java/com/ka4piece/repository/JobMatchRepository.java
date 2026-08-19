package com.ka4piece.repository;

import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.Vacancy;

import java.util.*;
import java.util.stream.Collectors;

public class JobMatchRepository extends MySqlStore {

    public JobMatchRepository(String jdbcUrl, String dbUser, String dbPassword) {
        super(jdbcUrl, dbUser, dbPassword);
    }

    // ----- Jobseeker methods -----
    public void saveJobseeker(JobseekerProfile p) {
        String skillsStr = (p.getSkills() == null || p.getSkills().isEmpty())
                ? ""
                : String.join(";", p.getSkills());

        String appliedStr = serializeAppliedVacancies(p.getAppliedVacancies());

        String sql = "INSERT INTO jobseekers (jobseekerId, householdId, memberName, education, skills, experienceYears, location, appliedVacancies) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
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

    public void updateJobseekerProfile(String jobseekerId, String memberName, String education, List<String> skills, int experienceYears, String location) {
        JobseekerProfile p = findJobseekerById(jobseekerId);
        if (p == null) throw new IllegalArgumentException("Jobseeker profile not found: " + jobseekerId);
        p.setMemberName(memberName);
        p.setEducation(education);
        p.setSkills(skills);
        p.setExperienceYears(experienceYears);
        p.setLocation(location);
        saveJobseeker(p);
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

    // ----- Vacancy methods -----
    public void saveVacancy(Vacancy v) {
        String skillsReqStr = (v.getSkillRequirements() == null || v.getSkillRequirements().isEmpty())
                ? ""
                : String.join(";", v.getSkillRequirements());

        String sql = "INSERT INTO vacancies (vacancyId, title, description, educationRequirement, skillRequirements, " +
                "experienceYearsRequired, location, compensation, type, enteredByOfficialId, status, archiveReason, hiredJobseekerId) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "title = VALUES(title), " +
                "description = VALUES(description), " +
                "educationRequirement = VALUES(educationRequirement), " +
                "skillRequirements = VALUES(skillRequirements), " +
                "experienceYearsRequired = VALUES(experienceYearsRequired), " +
                "location = VALUES(location), " +
                "compensation = VALUES(compensation), " +
                "type = VALUES(type), " +
                "enteredByOfficialId = VALUES(enteredByOfficialId), " +
                "status = VALUES(status), " +
                "archiveReason = VALUES(archiveReason), " +
                "hiredJobseekerId = VALUES(hiredJobseekerId)";

        executeUpdate(sql,
                v.getVacancyId(),
                v.getTitle(),
                v.getDescription(),
                v.getEducationRequirement(),
                skillsReqStr,
                v.getExperienceYearsRequired(),
                v.getLocation(),
                v.getCompensation(),
                v.getType(),
                v.getEnteredByOfficialId(),
                v.getStatus() != null ? v.getStatus() : "ACTIVE",
                v.getArchiveReason(),
                v.getHiredJobseekerId()
        );
    }

    public void updateVacancyDetails(String vacancyId, String title, String description, String educationRequirement, List<String> skillRequirements, int experienceYearsRequired, String location, String compensation, String type) {
        Vacancy v = findVacancyById(vacancyId);
        if (v == null) throw new IllegalArgumentException("Vacancy not found: " + vacancyId);
        v.setTitle(title);
        v.setDescription(description);
        v.setEducationRequirement(educationRequirement);
        v.setSkillRequirements(skillRequirements);
        v.setExperienceYearsRequired(experienceYearsRequired);
        v.setLocation(location);
        v.setCompensation(compensation);
        v.setType(type);
        saveVacancy(v);
    }

    public void updateVacancyStatus(String vacancyId, String status, String archiveReason, String hiredJobseekerId) {
        Vacancy v = findVacancyById(vacancyId);
        if (v != null) {
            v.setStatus(status);
            v.setArchiveReason(archiveReason);
            v.setHiredJobseekerId(hiredJobseekerId);
            saveVacancy(v);
        } else {
            String sql = "UPDATE vacancies SET status = ?, archiveReason = ?, hiredJobseekerId = ? WHERE vacancyId = ?";
            executeUpdate(sql, status, archiveReason, hiredJobseekerId, vacancyId);
        }
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

    // ----- Helper methods -----
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

    //updated mapRowtoVacancy
    private Vacancy mapRowToVacancy(Map<String, Object> row) {
        String skillsReqText = (String) row.get("skillRequirements");
        List<String> skillRequirements = parseSkills(skillsReqText);

        String status = row.get("status") != null ? (String) row.get("status") : "ACTIVE";

        return new Vacancy(
                (String) row.get("vacancyId"),
                (String) row.get("title"),
                (String) row.get("description"),
                (String) row.get("educationRequirement"),
                (String) row.get("location"),
                (String) row.get("compensation"),
                (String) row.get("type"),
                skillRequirements,
                ((Number) row.get("experienceYearsRequired")).intValue(),
                (String) row.get("enteredByOfficialId"),
                status,
                (String) row.get("archiveReason"),
                (String) row.get("hiredJobseekerId")
        );
    }

    private List<String> parseSkills(String skillsText) {
        if (skillsText == null || skillsText.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(skillsText.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
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