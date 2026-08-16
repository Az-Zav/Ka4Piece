package com.ka4piece.repository;

import com.ka4piece.model.Household;
import com.ka4piece.model.BarangayOfficial;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuthRepository extends MySqlStore {

    public AuthRepository(String jdbcUrl, String dbUser, String dbPassword) {
        super(jdbcUrl, dbUser, dbPassword);
    }

    //-----Household methods-----
    public void saveHousehold(Household h) {
        String sql = "INSERT INTO households (householdId, headName, address, barangay, username, password) VALUES (?, ?, ?, ?, ?, ?)";
        executeUpdate(sql,
                h.getHouseholdId(),
                h.getHeadName(),
                h.getAddress(),
                h.getBarangay(),
                h.getUsername(),
                h.getPassword()
        );
    }

    public Household findHouseholdById(String householdId) {
        String sql = "SELECT * FROM households WHERE householdId = ?";
        List<Map<String, Object>> rows = executeQuery(sql, householdId);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToHousehold(rows.get(0));
    }

    public Household findHouseholdByUsername(String username) {
        String sql = "SELECT * FROM households WHERE username = ?";
        List<Map<String, Object>> rows = executeQuery(sql, username);
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

    public void updateHousehold(Household h) {
        String sql = "UPDATE households SET headName = ?, address = ?, barangay = ?, password = ? WHERE householdId = ?";
        executeUpdate(sql,
                h.getHeadName(),
                h.getAddress(),
                h.getBarangay(),
                h.getPassword(),
                h.getHouseholdId()
        );
    }

    //-----BarangayOfficial methods-----
    public void saveOfficial(BarangayOfficial o) {
        String sql = "INSERT INTO officials (officialId, name, username, password) VALUES (?, ?, ?, ?)";
        executeUpdate(sql,
                o.getOfficialId(),
                o.getName(),
                o.getUsername(),
                o.getPassword()
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

    public BarangayOfficial findOfficialByUsername(String username) {
        String sql = "SELECT * FROM officials WHERE username = ?";
        List<Map<String, Object>> rows = executeQuery(sql, username);
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

    public void updateOfficial(BarangayOfficial o) {
        String sql = "UPDATE officials SET name = ?, password = ? WHERE officialId = ?";
        executeUpdate(sql,
                o.getName(),
                o.getPassword(),
                o.getOfficialId()
        );
    }

    //-----Helper Methods-----
    private Household mapRowToHousehold(Map<String, Object> row) {
        return new Household(
                (String) row.get("householdId"),
                (String) row.get("headName"),
                (String) row.get("address"),
                (String) row.get("barangay"),
                (String) row.get("username"),
                (String) row.get("password")
        );
    }

    private BarangayOfficial mapRowToOfficial(Map<String, Object> row) {
        return new BarangayOfficial(
                (String) row.get("officialId"),
                (String) row.get("name"),
                (String) row.get("username"),
                (String) row.get("password")
        );
    }
}