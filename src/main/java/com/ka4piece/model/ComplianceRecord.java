package com.ka4piece.model;

public class ComplianceRecord {
    private String householdId, monthYear;
    private boolean pregnancyCareStatus, child0to5HealthStatus, dewormingStatus,
                    daycareAttendanceStatus, schoolAttendanceStatus, fdsAttendanceStatus;
    private int elementaryCount, juniorHighCount, seniorHighCount;
    private String recordedByOfficialId;

    public ComplianceRecord(String householdId, String monthYear, boolean pregnancyCareStatus,
                            boolean child0to5HealthStatus, boolean dewormingStatus,
                            boolean daycareAttendanceStatus, boolean schoolAttendanceStatus,
                            boolean fdsAttendanceStatus, int elementaryCount,
                            int juniorHighCount, int seniorHighCount,
                            String recordedByOfficialId) {
        this.householdId = householdId;
        this.monthYear = monthYear;
        this.pregnancyCareStatus = pregnancyCareStatus;
        this.child0to5HealthStatus = child0to5HealthStatus;
        this.dewormingStatus = dewormingStatus;
        this.daycareAttendanceStatus = daycareAttendanceStatus;
        this.schoolAttendanceStatus = schoolAttendanceStatus;
        this.fdsAttendanceStatus = fdsAttendanceStatus;
        this.elementaryCount = elementaryCount;
        this.juniorHighCount = juniorHighCount;
        this.seniorHighCount = seniorHighCount;
        this.recordedByOfficialId = recordedByOfficialId;
    }

    // Getters and setters
    public String getHouseholdId() { return householdId; }
    public void setHouseholdId(String householdId) { this.householdId = householdId; }

    public String getMonthYear() { return monthYear; }
    public void setMonthYear(String monthYear) { this.monthYear = monthYear; }

    public boolean isPregnancyCareStatus() { return pregnancyCareStatus; }
    public void setPregnancyCareStatus(boolean pregnancyCareStatus) { this.pregnancyCareStatus = pregnancyCareStatus; }

    public boolean isChild0to5HealthStatus() { return child0to5HealthStatus; }
    public void setChild0to5HealthStatus(boolean child0to5HealthStatus) { this.child0to5HealthStatus = child0to5HealthStatus; }

    public boolean isDewormingStatus() { return dewormingStatus; }
    public void setDewormingStatus(boolean dewormingStatus) { this.dewormingStatus = dewormingStatus; }

    public boolean isDaycareAttendanceStatus() { return daycareAttendanceStatus; }
    public void setDaycareAttendanceStatus(boolean daycareAttendanceStatus) { this.daycareAttendanceStatus = daycareAttendanceStatus; }

    public boolean isSchoolAttendanceStatus() { return schoolAttendanceStatus; }
    public void setSchoolAttendanceStatus(boolean schoolAttendanceStatus) { this.schoolAttendanceStatus = schoolAttendanceStatus; }

    public boolean isFdsAttendanceStatus() { return fdsAttendanceStatus; }
    public void setFdsAttendanceStatus(boolean fdsAttendanceStatus) { this.fdsAttendanceStatus = fdsAttendanceStatus; }

    public int getElementaryCount() { return elementaryCount; }
    public void setElementaryCount(int elementaryCount) { this.elementaryCount = elementaryCount; }

    public int getJuniorHighCount() { return juniorHighCount; }
    public void setJuniorHighCount(int juniorHighCount) { this.juniorHighCount = juniorHighCount; }

    public int getSeniorHighCount() { return seniorHighCount; }
    public void setSeniorHighCount(int seniorHighCount) { this.seniorHighCount = seniorHighCount; }

    public String getRecordedByOfficialId() { return recordedByOfficialId; }
    public void setRecordedByOfficialId(String recordedByOfficialId) { this.recordedByOfficialId = recordedByOfficialId; }
}
