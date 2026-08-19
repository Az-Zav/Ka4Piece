package com.ka4piece.model;

import java.time.LocalDate;

public class Household {
    private String householdId, headName, address, barangay, email, password;
    private String status = "ACTIVE"; // ACTIVE|EXITED
    private String exitReason;
    private LocalDate exitDate;

    // Composition / applicability fields (uncapped)
    private boolean hasPregnantMember;
    private boolean has0to5Member;
    private int elemCount;
    private int jhsCount;
    private int shsCount;

    //Used upon registration
    public Household(String householdId, String headName, String address, String barangay, String email, String password) {
        this.householdId = householdId;
        this.headName = headName;
        this.address = address;
        this.barangay = barangay;
        this.email = email;
        this.password = password;
    }

    //Used when fetching from db and mapping to object in memory
    public Household(String householdId, String headName, String address, String barangay, String email, String password, String status, String exitReason, LocalDate exitDate,
                     boolean hasPregnantMember, boolean has0to5Member, int elemCount, int jhsCount, int shsCount) {
        this.householdId = householdId;
        this.headName = headName;
        this.address = address;
        this.barangay = barangay;
        this.email = email;
        this.password = password;
        this.status = status;
        this.exitReason = exitReason;
        this.exitDate = exitDate;
        this.hasPregnantMember = hasPregnantMember;
        this.has0to5Member = has0to5Member;
        this.elemCount = elemCount;
        this.jhsCount = jhsCount;
        this.shsCount = shsCount;
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

    public boolean isHasPregnantMember() { return hasPregnantMember; }
    public void setHasPregnantMember(boolean hasPregnantMember) { this.hasPregnantMember = hasPregnantMember; }

    public boolean isHas0to5Member() { return has0to5Member; }
    public void setHas0to5Member(boolean has0to5Member) { this.has0to5Member = has0to5Member; }

    public int getElemCount() { return elemCount; }
    public void setElemCount(int elemCount) { this.elemCount = elemCount; }

    public int getJhsCount() { return jhsCount; }
    public void setJhsCount(int jhsCount) { this.jhsCount = jhsCount; }

    public int getShsCount() { return shsCount; }
    public void setShsCount(int shsCount) { this.shsCount = shsCount; }
}
