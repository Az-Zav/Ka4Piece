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

    // ----- Compliance methods -----
    public void saveCompliance(ComplianceRecord r) {
        String sql = "INSERT INTO compliance (householdId, monthYear, pregnancyCareStatus, child0to5HealthStatus, " +
                     "dewormingStatus, daycareAttendanceStatus, schoolAttendanceStatus, fdsAttendanceStatus, " +
                     "elementaryCount, juniorHighCount, seniorHighCount, recordedByOfficialId, recordedAt) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "pregnancyCareStatus = VALUES(pregnancyCareStatus), " +
                     "child0to5HealthStatus = VALUES(child0to5HealthStatus), " +
                     "dewormingStatus = VALUES(dewormingStatus), " +
                     "daycareAttendanceStatus = VALUES(daycareAttendanceStatus), " +
                     "schoolAttendanceStatus = VALUES(schoolAttendanceStatus), " +
                     "fdsAttendanceStatus = VALUES(fdsAttendanceStatus), " +
                     "elementaryCount = VALUES(elementaryCount), " +
                     "juniorHighCount = VALUES(juniorHighCount), " +
                     "seniorHighCount = VALUES(seniorHighCount), " +
                     "recordedByOfficialId = VALUES(recordedByOfficialId), " +
                     "recordedAt = NOW()";

        executeUpdate(sql,
            r.getHouseholdId(),
            r.getMonthYear(),
            r.isPregnancyCareStatus(),
            r.isChild0to5HealthStatus(),
            r.isDewormingStatus(),
            r.isDaycareAttendanceStatus(),
            r.isSchoolAttendanceStatus(),
            r.isFdsAttendanceStatus(),
            r.getElementaryCount(),
            r.getJuniorHighCount(),
            r.getSeniorHighCount(),
            r.getRecordedByOfficialId()
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

    // ----- Grant methods -----
    public void saveGrant(GrantBreakdown g) {
        String withheldStr = (g.getWithheldReasons() == null || g.getWithheldReasons().isEmpty())
                             ? ""
                             : String.join(";", g.getWithheldReasons());

        String sql = "INSERT INTO grants (householdId, monthYear, healthGrantAmount, educationGrantAmount, " +
                     "riceSubsidyAmount, totalAmount, withheldReasons, recordType, correctionReason) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        executeUpdate(sql,
            g.getHouseholdId(),
            g.getMonthYear(),
            g.getHealthGrantAmount(),
            g.getEducationGrantAmount(),
            g.getRiceSubsidyAmount(),
            g.getTotalAmount(),
            withheldStr,
            g.getRecordType() != null ? g.getRecordType() : "INITIAL",
            g.getCorrectionReason()
        );
    }

    public GrantBreakdown findGrantByHouseholdAndMonth(String householdId, String monthYear) {
        String sql = "SELECT * FROM grants WHERE householdId = ? AND monthYear = ? ORDER BY id DESC LIMIT 1";
        List<Map<String, Object>> rows = executeQuery(sql, householdId, monthYear);
        if (rows.isEmpty()) {
            return null;
        }
        return mapRowToGrant(rows.get(0));
    }

    public List<GrantBreakdown> findGrantHistory(String householdId) {
        // Join against a per-month MAX(id) subquery so only the most recent record
        // per monthYear is returned (CORRECTION rows supersede INITIAL rows).
        String sql = "SELECT g.* FROM grants g " +
                     "INNER JOIN (SELECT monthYear, MAX(id) AS maxId FROM grants " +
                     "            WHERE householdId = ? GROUP BY monthYear) latest " +
                     "ON g.monthYear = latest.monthYear AND g.id = latest.maxId " +
                     "WHERE g.householdId = ? " +
                     "ORDER BY g.monthYear";
        List<Map<String, Object>> rows = executeQuery(sql, householdId, householdId);
        return rows.stream()
                   .map(this::mapRowToGrant)
                   .collect(Collectors.toList());
    }

    // ----- Helper methods -----
    private ComplianceRecord mapRowToCompliance(Map<String, Object> row) {
        int elem = row.get("elementaryCount") != null ? ((Number) row.get("elementaryCount")).intValue() : 0;
        int jhs = row.get("juniorHighCount") != null ? ((Number) row.get("juniorHighCount")).intValue() : 0;
        int shs = row.get("seniorHighCount") != null ? ((Number) row.get("seniorHighCount")).intValue() : 0;

        ComplianceRecord cr = new ComplianceRecord(
            (String) row.get("householdId"),
            (String) row.get("monthYear"),
            toBoolean(row.get("pregnancyCareStatus")),
            toBoolean(row.get("child0to5HealthStatus")),
            toBoolean(row.get("dewormingStatus")),
            toBoolean(row.get("daycareAttendanceStatus")),
            toBoolean(row.get("schoolAttendanceStatus")),
            toBoolean(row.get("fdsAttendanceStatus")),
            elem,
            jhs,
            shs,
            (String) row.get("recordedByOfficialId")
        );
        java.sql.Timestamp recordedAt = (java.sql.Timestamp) row.get("recordedAt");
        cr.setRecordedAt(recordedAt);
        return cr;
    }

    private GrantBreakdown mapRowToGrant(Map<String, Object> row) {
        String withheldText = (String) row.get("withheldReasons");
        List<String> reasons = (withheldText == null || withheldText.trim().isEmpty())
                               ? Collections.emptyList()
                               : Arrays.asList(withheldText.split(";"));

        String recordType = row.get("recordType") != null ? (String) row.get("recordType") : "INITIAL";

        return new GrantBreakdown(
            (String) row.get("householdId"),
            (String) row.get("monthYear"),
            ((Number) row.get("healthGrantAmount")).doubleValue(),
            ((Number) row.get("educationGrantAmount")).doubleValue(),
            ((Number) row.get("riceSubsidyAmount")).doubleValue(),
            ((Number) row.get("totalAmount")).doubleValue(),
            reasons,
            recordType,
            (String) row.get("correctionReason")
        );
    }

    private boolean toBoolean(Object val) {
        if (val == null) return false;
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() != 0;
        }
        return Boolean.parseBoolean(String.valueOf(val));
    }
}