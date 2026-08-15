package com.ka4piece.model;

public class ComplianceRecord {
    private String householdID, monthYear;
    boolean pregnancyCareStatus, child0to5HealthStatus, dewormingStatus,
            daycareAttendanceStatus, schoolAttendanceStatus, fdsAttendanceStatus;
    int childrenMeetingAttendance;

    public ComplianceRecord(String householdID, String monthYear, boolean pregnancyCareStatus,
                            boolean child0to5HealthStatus, boolean dewormingStatus,
                            boolean daycareAttendanceStatus, boolean schoolAttendanceStatus,
                            boolean fdsAttendanceStatus, int childrenMeetingAttendance) {
        this.householdID = householdID;
        this.monthYear = monthYear;
        this.pregnancyCareStatus = pregnancyCareStatus;
        this.child0to5HealthStatus = child0to5HealthStatus;
        this.dewormingStatus = dewormingStatus;
        this.daycareAttendanceStatus = daycareAttendanceStatus;
        this.schoolAttendanceStatus = schoolAttendanceStatus;
        this.fdsAttendanceStatus = fdsAttendanceStatus;
        this.childrenMeetingAttendance = childrenMeetingAttendance;
    }

    //Getters and setters
    public String getHouseholdID() {return householdID;}
    public void setHouseholdID(String houseHoldID) {this.householdID = houseHoldID;}

    public String getMonthYear() {return monthYear;}
    public void setMonthYear(String monthYear) {this.monthYear = monthYear;}

    public boolean isPregnancyCareStatus() {return pregnancyCareStatus;}
    public void setPregnancyCareStatus(boolean pregnancyCareStatus) {this.pregnancyCareStatus = pregnancyCareStatus;}

    public boolean isChild0to5HealthStatus() {return child0to5HealthStatus;}
    public void setChild0to5HealthStatus(boolean child0to5HealthStatus) {this.child0to5HealthStatus = child0to5HealthStatus;}

    public boolean isDewormingStatus() {return dewormingStatus;}
    public void setDewormingStatus(boolean dewormingStatus) {this.dewormingStatus = dewormingStatus;}

    public boolean isDaycareAttendanceStatus() {return daycareAttendanceStatus;}
    public void setDaycareAttendanceStatus(boolean daycareAttendanceStatus) {this.daycareAttendanceStatus = daycareAttendanceStatus;}

    public boolean isSchoolAttendanceStatus() {return schoolAttendanceStatus;}
    public void setSchoolAttendanceStatus(boolean schoolAttendanceStatus) {this.schoolAttendanceStatus = schoolAttendanceStatus;}

    public boolean isFdsAttendanceStatus() {return fdsAttendanceStatus;}
    public void setFdsAttendanceStatus(boolean fdsAttendanceStatus) {this.fdsAttendanceStatus = fdsAttendanceStatus;}

    public int getChildrenMeetingAttendance() {return childrenMeetingAttendance;}
    public void setChildrenMeetingAttendance(int childrenMeetingAttendance) {this.childrenMeetingAttendance = childrenMeetingAttendance;}
}
