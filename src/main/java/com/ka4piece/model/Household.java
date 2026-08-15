package com.ka4piece.model;

public class Household {
    private String householdId, headName, address, barangay, username, password;

    public Household(String householdId, String headName, String address, String barangay, String username, String password) {
        this.householdId = householdId;
        this.headName = headName;
        this.address = address;
        this.barangay = barangay;
        this.username = username;
        this.password = password;
    }

    //Getters and setters for each field
    public String getHouseholdId() {return householdId;}
    public void setHouseholdId(String householdId) {this.householdId = householdId;}

    public String getHeadName() {return headName;}
    public void setHeadName(String headName) {this.headName = headName;}

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;}

    public String getBarangay() {return barangay;}
    public void setBarangay(String barangay) {this.barangay = barangay;}   

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}
        
    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

}


