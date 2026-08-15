package com.ka4piece.repository;

import com.ka4piece.model.Household;
import com.ka4piece.model.BarangayOfficial;
import java.util.ArrayList;
import java.util.List;


public class AuthRepository extends CSVStore {
    private String householdFilePath, officialFilePath;

    public AuthRepository(String householdFilePath, String officialFilePath) {
        this.householdFilePath = householdFilePath;
        this.officialFilePath = officialFilePath;
    }


    // -----Household methods-----
    public void saveHousehold(Household h) {
        appendLine(householdFilePath, serializeHousehold(h));
    }

    //Used by managers to retrieve household record
    public Household findHouseholdById(String householdId) {
        for (String line : readLines(householdFilePath)) {
            Household h = parseHousehold(line);
            if (h.getHouseholdId().equals(householdId)) return h;
        }
        return null; //not found
    }

    //Used for login to retrieve by username
    public Household findHouseholdByUsername(String username) {
        for (String line : readLines(householdFilePath)) {
            Household h = parseHousehold(line);
            if (h.getUsername().equals(username)) return h;
        }
        return null; //not found
    }

    public List<Household> findAllHouseholds() {
        List<Household> households = new ArrayList<>();
        for (String line : readLines(householdFilePath)) {
            if (!line.isBlank()) households.add(parseHousehold(line));
        }
        return households;
    }

    // -----Official methods-----
    public void saveOfficial(BarangayOfficial o) {
        appendLine(officialFilePath, serializeOfficial(o));
    }

    //Used by managers to retrieve official record
    public BarangayOfficial findOfficialById(String officialId) {
        for (String line : readLines(officialFilePath)) {
            BarangayOfficial o = parseOfficial(line);
            if (o.getOfficialId().equals(officialId)) return o;
        }
        return null; //not found
    }

    //Used for login to retrieve by username
    public BarangayOfficial findOfficialByUsername(String username) {
        for (String line : readLines(officialFilePath)) {
            BarangayOfficial o = parseOfficial(line);
            if (o.getUsername().equals(username)) return o;
        }
        return null; //not found
    }

    public List<BarangayOfficial> findAllOfficials() {
        List<BarangayOfficial> officials = new ArrayList<>();
        for (String line : readLines(officialFilePath)) {
            if (!line.isBlank()) officials.add(parseOfficial(line));
        }
        return officials;
    }

    // -----Helper methods-----
    private Household parseHousehold(String line) {
        String[] r = line.split(",");
        return new Household(r[0], r[1], r[2], r[3], r[4], r[5]); //access householdId, headName, address, barangay, username, password
        }

    private String serializeHousehold(Household h) {
        return String.join(",", 
        h.getHouseholdId(), 
        h.getHeadName(), 
        h.getAddress(), 
        h.getBarangay(), 
        h.getUsername(), 
        h.getPassword());
    }

    private BarangayOfficial parseOfficial(String line) {
        String[] r = line.split(",");
        return new BarangayOfficial(r[0], r[1], r[2], r[3]); //access officialId, name, username, password
        }

    private String serializeOfficial(BarangayOfficial o) {
        return String.join(",", 
        o.getOfficialId(), 
        o.getName(), 
        o.getUsername(), 
        o.getPassword());
    }
}
