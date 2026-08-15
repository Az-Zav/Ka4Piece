package com.ka4piece.model;

import java.util.List;

public class Vacancy {
    private String vacancyId, title, educationRequirement, location, compensation, type, enteredByOfficialId;
    private List<String> skillRequirements;
    private int experienceYearsRequired;

    public Vacancy(String vacancyId, String title, String educationRequirement, String location, String compensation, String type, List<String> skillRequirements, int experienceYearsRequired, String enteredByOfficialId) {
        this.vacancyId = vacancyId;
        this.title = title;
        this.educationRequirement = educationRequirement;
        this.location = location;
        this.compensation = compensation;
        this.type = type;
        this.skillRequirements = skillRequirements;
        this.experienceYearsRequired = experienceYearsRequired;
        this.enteredByOfficialId = enteredByOfficialId;
    }

    //Getters and setters
    public String getVacancyId() {return vacancyId;}
    public void setVacancyId(String vacancyId) {this.vacancyId = vacancyId;}

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getEducationRequirement() {return educationRequirement;}
    public void setEducationRequirement(String educationRequirement) {this.educationRequirement = educationRequirement;}

    public String getLocation() {return location;}
    public void setLocation(String location) {this.location = location;}

    public String getCompensation() {return compensation;}
    public void setCompensation(String compensation) {this.compensation = compensation;}

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public String getEnteredByOfficialId() {return enteredByOfficialId;}
    public void setEnteredByOfficialId(String enteredByOfficialId) {this.enteredByOfficialId = enteredByOfficialId;}

    public List<String> getSkillRequirements() {return skillRequirements;}
    public void setSkillRequirements(List<String> skillRequirements) {this.skillRequirements = skillRequirements;}

    public int getExperienceYearsRequired() {return experienceYearsRequired;}
    public void setExperienceYearsRequired(int experienceYearsRequired) {this.experienceYearsRequired = experienceYearsRequired;}
}
