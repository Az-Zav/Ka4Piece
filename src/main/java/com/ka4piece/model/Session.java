package com.ka4piece.model;

public class Session {
    private static Session instance;

    private String userId, role, displayName; // role as OFFICIAL or HOUSEHOLD
    private boolean isAdmin;

    public Session(String userId, String role, String displayName) {
        this.userId = userId;
        this.role = role;
        this.displayName = displayName;
    }

    public Session(String userId, String role, String displayName, boolean isAdmin) {
        this.userId = userId;
        this.role = role;
        this.displayName = displayName;
        this.isAdmin = isAdmin;
    }

    // --- ADDED GLOBAL SESSION METHODS ---

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session(null, null, null);
        }
        return instance;
    }

    public static void setInstance(Session session) {
        instance = session;
    }

    public void clearSession() {
        this.userId = null;
        this.role = null;
        this.displayName = null;
        this.isAdmin = false;
        instance = null;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean isAdmin) { this.isAdmin = isAdmin; }
}
