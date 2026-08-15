package com.ka4piece.model;

public class BarangayOfficial {
    private String officialID, name, username, password;

    public BarangayOfficial(String officialID, String name, String username, String password) {
        this.officialID = officialID;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    // Getters and setters

    public String getOfficialID() {return officialID;}
    public void setOfficialID(String officialID) {this.officialID = officialID;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
}
