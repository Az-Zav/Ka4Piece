package com.ka4piece.model;

import java.util.List;

public class GrantBreakdown {
    private String householdId, monthYear;
    private double healthGrantAmount, educationGrantAmount, riceSubsidyAmount, totalAmount;
    private List<String> withheldReasons;

    public GrantBreakdown(String householdId, String monthYear, double healthGrantAmount, double educationGrantAmount, double riceSubsidyAmount, double totalAmount, List<String> withheldReasons) {
        this.householdId = householdId;
        this.monthYear = monthYear;
        this.healthGrantAmount = healthGrantAmount;
        this.educationGrantAmount = educationGrantAmount;
        this.riceSubsidyAmount = riceSubsidyAmount;
        this.totalAmount = totalAmount;
        this.withheldReasons = withheldReasons;
    }

    // Getters and setters
    public String getHouseholdId() {return householdId;}
    public void setHouseholdId(String householdId) {this.householdId = householdId;}

    public String getMonthYear() {return monthYear;}
    public void setMonthYear(String monthYear) {this.monthYear = monthYear;}

    public double getHealthGrantAmount() {return healthGrantAmount;}
    public void setHealthGrantAmount(double healthGrantAmount) {this.healthGrantAmount = healthGrantAmount;}

    public double getEducationGrantAmount() {return educationGrantAmount;}
    public void setEducationGrantAmount(double educationGrantAmount) {this.educationGrantAmount = educationGrantAmount;}

    public double getRiceSubsidyAmount() {return riceSubsidyAmount;}
    public void setRiceSubsidyAmount(double riceSubsidyAmount) {this.riceSubsidyAmount = riceSubsidyAmount;}

    public double getTotalAmount() {return totalAmount;}
    public void setTotalAmount(double totalAmount) {this.totalAmount = totalAmount;}

    public List<String> getWithheldReasons() {return withheldReasons;}
    public void setWithheldReasons(List<String> withheldReasons) {this.withheldReasons = withheldReasons;}
}
