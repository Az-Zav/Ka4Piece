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
        String sql = "INSERT INTO households (householdId, headName, address, barangay, email, password, status, exitReason, exitDate) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "headName = VALUES(headName), " +
                     "address = VALUES(address), " +
                     "barangay = VALUES(barangay), " +
                     "email = VALUES(email), " +
                     "password = VALUES(password), " +
                     "status = VALUES(status), " +
                     "exitReason = VALUES(exitReason), " +
                     "exitDate = VALUES(exitDate)";

        executeUpdate(sql,
            h.getHouseholdId(),
            h.getHeadName(),
            h.getAddress(),
            h.getBarangay(),
            h.getEmail(),
            h.getPassword(),
            h.getStatus(),
            h.getExitReason(),
            h.getExitDate() != null ? Date.valueOf(h.getExitDate()) : null
        );
    }

    public Household findHouseholdById(String householdId) {
        String sql = "SELECT * FROM households WHERE householdId = ?";
        List<Map<String, Object>> rows = executeQuery(sql, householdId); //returns a list of rows, each row is a map of column names to values
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToHousehold(rows.get(0)); //parses row to a household object
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

    // ----- Helper Methods -----
    private Household mapRowToHousehold(Map<String, Object> row) {
        Object exitDateObj = row.get("exitDate");
        LocalDate exitDate = null;
        if (exitDateObj instanceof java.sql.Date) {
            exitDate = ((java.sql.Date) exitDateObj).toLocalDate();
        } else if (exitDateObj instanceof java.util.Date) {
            exitDate = new java.sql.Date(((java.util.Date) exitDateObj).getTime()).toLocalDate();
        } else if (exitDateObj != null) {
            exitDate = LocalDate.parse(String.valueOf(exitDateObj));
        }

        String status = row.get("status") != null ? (String) row.get("status") : "ACTIVE";

        return new Household(
            (String) row.get("householdId"),
            (String) row.get("headName"),
            (String) row.get("address"),
            (String) row.get("barangay"),
            (String) row.get("email"),
            (String) row.get("password"),
            status,
            (String) row.get("exitReason"),
            exitDate
        );
    }

    private BarangayOfficial mapRowToOfficial(Map<String, Object> row) {
        boolean isAdmin = toBoolean(row.get("isAdmin"));

        return new BarangayOfficial(
            (String) row.get("officialId"),
            (String) row.get("name"),
            (String) row.get("email"),
            (String) row.get("password"),
            isAdmin
        );
    }

    private boolean toBoolean(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(val));
    }
}