package com.ka4piece.model;

public class Session {
    private String userId, role, displayName; //role as OFFICIAL or HOUSEHOLD

    public Session(String userId, String role, String displayName) {
        this.userId = userId;
        this.role = role;
        this.displayName = displayName;
    }

    // Getters and setters
    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}

    public String getDisplayName() {return displayName;}
    public void setDisplayName(String displayName) {this.displayName = displayName;}
}
