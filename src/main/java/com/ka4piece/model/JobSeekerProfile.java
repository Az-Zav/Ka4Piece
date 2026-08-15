package com.ka4piece.model;

import java.util.List;
import java.util.Map;

public class JobSeekerProfile {
    private String jobseekerID, householdId, memberName, education, location;
    private List<String> skills;
    private int experienceYears;
    private Map<String, String> appliedVacancies; //vacancyId -> status
    
    public JobSeekerProfile(String jobseekerID, String householdId, String memberName, String education, String location, List<String> skills, int experienceYears, Map<String, String> appliedVacancies) {
        this.jobseekerID = jobseekerID;
        this.householdId = householdId;
        this.memberName = memberName;
        this.education = education;
        this.location = location;
        this.skills = skills;
        this.experienceYears = experienceYears;
        this.appliedVacancies = appliedVacancies;
    }

    // Getters and setters
    public String getJobseekerID() {return jobseekerID;}
    public void setJobseekerID(String jobseekerID) {this.jobseekerID = jobseekerID;}
    
    public String getHouseholdId() {return householdId;}
    public void setHouseholdId(String householdId) {this.householdId = householdId;}
    
    public String getMemberName() {return memberName;}
    public void setMemberName(String memberName) {this.memberName = memberName;}
    
    public String getEducation() {return education;}
    public void setEducation(String education) {this.education = education;}
    
    public String getLocation() {return location;}
    public void setLocation(String location) {this.location = location;}
    
    public List<String> getSkills() {return skills;}
    public void setSkills(List<String> skills) {this.skills = skills;}
    
    public int getExperienceYears() {return experienceYears;}
    public void setExperienceYears(int experienceYears) {this.experienceYears = experienceYears;}
    
    public Map<String, String> getAppliedVacancies() {return appliedVacancies;}
    public void setAppliedVacancies(Map<String, String> appliedVacancies) {this.appliedVacancies = appliedVacancies;}
}
