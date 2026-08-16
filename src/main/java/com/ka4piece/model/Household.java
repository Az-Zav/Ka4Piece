package com.ka4piece.model;

import java.time.LocalDate;

public class Household {
    private String householdId, headName, address, barangay, email, password;
    private String status = "ACTIVE"; // ACTIVE|EXITED
    private String exitReason;
    private LocalDate exitDate;

    public Household(String householdId, String headName, String address, String barangay, String email, String password) {
        this.householdId = householdId;
        this.headName = headName;
        this.address = address;
        this.barangay = barangay;
        this.email = email;
        this.password = password;
    }

    public Household(String householdId, String headName, String address, String barangay, String email, String password, String status, String exitReason, LocalDate exitDate) {
        this.householdId = householdId;
        this.headName = headName;
        this.address = address;
        this.barangay = barangay;
        this.email = email;
        this.password = password;
        this.status = status;
        this.exitReason = exitReason;
        this.exitDate = exitDate;
    }

    // Getters and setters
    public String getHouseholdId() { return householdId; }
    public void setHouseholdId(String householdId) { this.householdId = householdId; }

    public String getHeadName() { return headName; }
    public void setHeadName(String headName) { this.headName = headName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExitReason() { return exitReason; }
    public void setExitReason(String exitReason) { this.exitReason = exitReason; }

    public LocalDate getExitDate() { return exitDate; }
    public void setExitDate(LocalDate exitDate) { this.exitDate = exitDate; }
}
