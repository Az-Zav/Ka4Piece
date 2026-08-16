package com.ka4piece.repository;

import com.ka4piece.model.ComplianceRecord;
import com.ka4piece.model.GrantBreakdown;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComplianceRepository extends MySqlStore {

    public ComplianceRepository(String jdbcUrl, String dbUser, String dbPassword) {
        super(jdbcUrl, dbUser, dbPassword);
    }

    //-----Compliance methods-----
    public void saveCompliance(ComplianceRecord r) {
        String sql = "INSERT INTO compliance (householdId, monthYear, pregnancyCareStatus, child0to5HealthStatus, " +
                     "dewormingStatus, daycareAttendanceStatus, schoolAttendanceStatus, fdsAttendanceStatus, childrenMeetingAttendance) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " + // if the same householdId and monthYear already exists, update the record instead of inserting a new one
                     "pregnancyCareStatus = VALUES(pregnancyCareStatus), " +
                     "child0to5HealthStatus = VALUES(child0to5HealthStatus), " +
                     "dewormingStatus = VALUES(dewormingStatus), " +
                     "daycareAttendanceStatus = VALUES(daycareAttendanceStatus), " +
                     "schoolAttendanceStatus = VALUES(schoolAttendanceStatus), " +
                     "fdsAttendanceStatus = VALUES(fdsAttendanceStatus), " +
                     "childrenMeetingAttendance = VALUES(childrenMeetingAttendance)";
        
        executeUpdate(sql,
            r.getHouseholdId(),
            r.getMonthYear(),
            r.isPregnancyCareStatus(),
            r.isChild0to5HealthStatus(),
            r.isDewormingStatus(),
            r.isDaycareAttendanceStatus(),
            r.isSchoolAttendanceStatus(),
            r.isFdsAttendanceStatus(),
            r.getChildrenMeetingAttendance()
        );
    }

    public ComplianceRecord findComplianceByHouseholdAndMonth(String householdId, String monthYear) {
        String sql = "SELECT * FROM compliance WHERE householdId = ? AND monthYear = ?";
        List<Map<String, Object>> rows = executeQuery(sql, householdId, monthYear);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToCompliance(rows.get(0));
    }

    public List<ComplianceRecord> findComplianceHistory(String householdId) {
        String sql = "SELECT * FROM compliance WHERE householdId = ? ORDER BY monthYear";
        List<Map<String, Object>> rows = executeQuery(sql, householdId);
        return rows.stream()
                   .map(this::mapRowToCompliance)
                   .collect(Collectors.toList());
    }

    //-----Grant methods-----

    public void saveGrant(GrantBreakdown g) {
        String withheldStr = (g.getWithheldReasons() == null || g.getWithheldReasons().isEmpty()) 
                             ? "" 
                             : String.join(";", g.getWithheldReasons());

        String sql = "INSERT INTO grants (householdId, monthYear, healthGrantAmount, educationGrantAmount, " +
                     "riceSubsidyAmount, totalAmount, withheldReasons) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        executeUpdate(sql,
            g.getHouseholdId(),
            g.getMonthYear(),
            g.getHealthGrantAmount(),
            g.getEducationGrantAmount(),
            g.getRiceSubsidyAmount(),
            g.getTotalAmount(),
            withheldStr
        );
    }

    public GrantBreakdown findGrantByHouseholdAndMonth(String householdId, String monthYear) {
        String sql = "SELECT * FROM grants WHERE householdId = ? AND monthYear = ?";
        List<Map<String, Object>> rows = executeQuery(sql, householdId, monthYear);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToGrant(rows.get(0));
    }

    public List<GrantBreakdown> findGrantHistory(String householdId) {
        String sql = "SELECT * FROM grants WHERE householdId = ? ORDER BY monthYear";
        List<Map<String, Object>> rows = executeQuery(sql, householdId);
        return rows.stream()
                   .map(this::mapRowToGrant)
                   .collect(Collectors.toList());
    }

    //-----Helper methods-----

    private ComplianceRecord mapRowToCompliance(Map<String, Object> row) { //parses a row from the database into a ComplianceRecord object
        return new ComplianceRecord(
            (String) row.get("householdId"),
            (String) row.get("monthYear"),
            toBoolean(row.get("pregnancyCareStatus")),
            toBoolean(row.get("child0to5HealthStatus")),
            toBoolean(row.get("dewormingStatus")),
            toBoolean(row.get("daycareAttendanceStatus")),
            toBoolean(row.get("schoolAttendanceStatus")),
            toBoolean(row.get("fdsAttendanceStatus")),
            ((Number) row.get("childrenMeetingAttendance")).intValue()
        );
    }

    private GrantBreakdown mapRowToGrant(Map<String, Object> row) { //parses a row from the database into a GrantBreakdown object
        String withheldText = (String) row.get("withheldReasons");
        List<String> reasons = (withheldText == null || withheldText.trim().isEmpty())
                               ? Collections.emptyList()
                               : Arrays.asList(withheldText.split(";"));

        return new GrantBreakdown(
            (String) row.get("householdId"),
            (String) row.get("monthYear"),
            ((Number) row.get("healthGrantAmount")).doubleValue(),
            ((Number) row.get("educationGrantAmount")).doubleValue(),
            ((Number) row.get("riceSubsidyAmount")).doubleValue(),
            ((Number) row.get("totalAmount")).doubleValue(),
            reasons
        );
    }

    private boolean toBoolean(Object val) {
        if (val instanceof Boolean) { //main routing where all values are caught as boolean from db
            return (Boolean) val;
        }
        if (val instanceof Number) { // defensive fallback if boolean is stored as tiny int in db
            return ((Number) val).intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(val));
    }


}