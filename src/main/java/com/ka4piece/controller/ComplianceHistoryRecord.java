package com.ka4piece.controller;

public class ComplianceHistoryRecord {
    private final String month;
    private final String health;
    private final String education;
    private final String fds;
    private final String status;

    public ComplianceHistoryRecord(String month, String health, String education, String fds, String status) {
        this.month = month;
        this.health = health;
        this.education = education;
        this.fds = fds;
        this.status = status;
    }

    public String getMonth() {
        return month;
    }

    public String getHealth() {
        return health;
    }

    public String getEducation() {
        return education;
    }

    public String getFds() {
        return fds;
    }

    public String getStatus() {
        return status;
    }
}