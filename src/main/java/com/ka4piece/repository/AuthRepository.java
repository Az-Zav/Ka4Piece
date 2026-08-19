package com.ka4piece.repository;

import com.ka4piece.model.Household;
import com.ka4piece.model.BarangayOfficial;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthRepository extends MySqlStore {

    public AuthRepository(String jdbcUrl, String dbUser, String dbPassword) {
        super(jdbcUrl, dbUser, dbPassword);
    }

    // ----- Household methods -----
    public void saveHousehold(Household h) {
        String sql = "INSERT INTO households (householdId, headName, address, barangay, email, password, status, exitReason, exitDate, " +
                "hasPregnantMember, has0to5Member, elemCount, jhsCount, shsCount) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "headName = VALUES(headName), " +
                "address = VALUES(address), " +
                "barangay = VALUES(barangay), " +
                "email = VALUES(email), " +
                "password = VALUES(password), " +
                "status = VALUES(status), " +
                "exitReason = VALUES(exitReason), " +
                "exitDate = VALUES(exitDate), " +
                "hasPregnantMember = VALUES(hasPregnantMember), " +
                "has0to5Member = VALUES(has0to5Member), " +
                "elemCount = VALUES(elemCount), " +
                "jhsCount = VALUES(jhsCount), " +
                "shsCount = VALUES(shsCount)";

        executeUpdate(sql,
                h.getHouseholdId(),
                h.getHeadName(),
                h.getAddress(),
                h.getBarangay(),
                h.getEmail(),
                h.getPassword(),
                h.getStatus(),
                h.getExitReason(),
                h.getExitDate() != null ? Date.valueOf(h.getExitDate()) : null,
                h.isHasPregnantMember(),
                h.isHas0to5Member(),
                h.getElemCount(),
                h.getJhsCount(),
                h.getShsCount()
        );
    }

    public void updateHouseholdProfile(String householdId, String headName, String address, String barangay, String email) {
        Household h = findHouseholdById(householdId);
        if (h == null) throw new IllegalArgumentException("Household not found: " + householdId);
        h.setHeadName(headName);
        h.setAddress(address);
        h.setBarangay(barangay);
        h.setEmail(email);
        saveHousehold(h);
    }

    public boolean updateHouseholdPassword(String householdId, String newPassword) {
        Household h = findHouseholdById(householdId);
        if (h == null) return false;
        h.setPassword(newPassword);
        saveHousehold(h);
        return true;
    }

    public boolean verifyCurrentPassword(String householdId, String currentPassword) {
        Household h = findHouseholdById(householdId);
        if (h == null || h.getPassword() == null) return false;
        return h.getPassword().equals(currentPassword);
    }

    public void updateHouseholdExitStatus(String householdId, String status, String exitReason, LocalDate exitDate) {
        Household h = findHouseholdById(householdId);
        if (h == null) throw new IllegalArgumentException("Household not found: " + householdId);
        h.setStatus(status);
        h.setExitReason(exitReason);
        h.setExitDate(exitDate);
        saveHousehold(h);
    }

    public Household findHouseholdById(String householdId) {
        String sql = "SELECT * FROM households WHERE householdId = ?";
        List<Map<String, Object>> rows = executeQuery(sql, householdId);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToHousehold(rows.get(0));
    }

    public Household findHouseholdByEmail(String email) {
        String sql = "SELECT * FROM households WHERE email = ?";
        List<Map<String, Object>> rows = executeQuery(sql, email);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToHousehold(rows.get(0));
    }

    public List<Household> findAllHouseholds() {
        String sql = "SELECT * FROM households";
        List<Map<String, Object>> rows = executeQuery(sql);
        return rows.stream()
                .map(this::mapRowToHousehold)
                .collect(Collectors.toList());
    }

    // ----- BarangayOfficial methods -----
    public void saveOfficial(BarangayOfficial o) {
        String sql = "INSERT INTO officials (officialId, name, email, password, isAdmin) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), " +
                "email = VALUES(email), " +
                "password = VALUES(password), " +
                "isAdmin = VALUES(isAdmin)";

        executeUpdate(sql,
                o.getOfficialId(),
                o.getName(),
                o.getEmail(),
                o.getPassword(),
                o.isAdmin()
        );
    }

    public void updateOfficialProfile(String officialId, String name, String email) {
        BarangayOfficial o = findOfficialById(officialId);
        if (o == null) throw new IllegalArgumentException("Official not found: " + officialId);
        o.setName(name);
        o.setEmail(email);
        saveOfficial(o);
    }

    public boolean updateOfficialPassword(String officialId, String newPassword) {
        BarangayOfficial o = findOfficialById(officialId);
        if (o == null) {
            return false;
        }
        o.setPassword(newPassword);
        saveOfficial(o);
        return true;
    }

    public BarangayOfficial findOfficialById(String officialId) {
        String sql = "SELECT * FROM officials WHERE officialId = ?";
        List<Map<String, Object>> rows = executeQuery(sql, officialId);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToOfficial(rows.get(0));
    }

    public BarangayOfficial findOfficialByEmail(String email) {
        String sql = "SELECT * FROM officials WHERE email = ?";
        List<Map<String, Object>> rows = executeQuery(sql, email);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToOfficial(rows.get(0));
    }

    public List<BarangayOfficial> findAllOfficials() {
        String sql = "SELECT * FROM officials";
        List<Map<String, Object>> rows = executeQuery(sql);
        return rows.stream()
                .map(this::mapRowToOfficial)
                .collect(Collectors.toList());
    }

    // ----- Helper Mapping Methods -----
    private Household mapRowToHousehold(Map<String, Object> row) {
        java.sql.Date sqlDate = (java.sql.Date) row.get("exitDate");
        LocalDate exitDate = sqlDate != null ? sqlDate.toLocalDate() : null;

        Boolean hasPregnant = (Boolean) row.get("hasPregnantMember");
        Boolean has0to5 = (Boolean) row.get("has0to5Member");
        Number elemVal = (Number) row.get("elemCount");
        Number jhsVal = (Number) row.get("jhsCount");
        Number shsVal = (Number) row.get("shsCount");

        return new Household(
                (String) row.get("householdId"),
                (String) row.get("headName"),
                (String) row.get("address"),
                (String) row.get("barangay"),
                (String) row.get("email"),
                (String) row.get("password"),
                (String) row.get("status"),
                (String) row.get("exitReason"),
                exitDate,
                hasPregnant != null ? hasPregnant : false,
                has0to5 != null ? has0to5 : false,
                elemVal != null ? elemVal.intValue() : 0,
                jhsVal != null ? jhsVal.intValue() : 0,
                shsVal != null ? shsVal.intValue() : 0
        );
    }

    private BarangayOfficial mapRowToOfficial(Map<String, Object> row) {
        Boolean isAdminObj = (Boolean) row.get("isAdmin");
        boolean isAdmin = isAdminObj != null ? isAdminObj : false;

        return new BarangayOfficial(
                (String) row.get("officialId"),
                (String) row.get("name"),
                (String) row.get("email"),
                (String) row.get("password"),
                isAdmin
        );
    }
}