package com.ka4piece.model;

public class BarangayOfficial {
    private String officialId, name, email, password;
    private boolean isAdmin = false;

    public BarangayOfficial(String officialId, String name, String email, String password) {
        this.officialId = officialId;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public BarangayOfficial(String officialId, String name, String email, String password, boolean isAdmin) {
        this.officialId = officialId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    // Getters and setters
    public String getOfficialId() { return officialId; }
    public void setOfficialId(String officialId) { this.officialId = officialId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
}
