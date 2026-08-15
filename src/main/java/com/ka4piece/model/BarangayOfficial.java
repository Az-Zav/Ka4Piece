package com.ka4piece.model;

public class BarangayOfficial {
    private String officialId, name, username, password;

    public BarangayOfficial(String officialId, String name, String username, String password) {
        this.officialId = officialId;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    // Getters and setters
    public String getOfficialId() {return officialId;}
    public void setOfficialId(String officialId) {this.officialId = officialId;}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}
}
