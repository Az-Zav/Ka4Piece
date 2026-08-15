package com.ka4piece.repository;

import com.ka4piece.model.ComplianceRecord;
import com.ka4piece.model.GrantBreakdown;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComplianceRepository extends CSVStore {
    private String complianceFilePath, grantFilePath;

    public ComplianceRepository(String complianceFilePath, String grantFilePath) {
        this.complianceFilePath = complianceFilePath;
        this.grantFilePath = grantFilePath;
    }

    //-----Compliance methods-----

    public void saveCompliance(ComplianceRecord r) {
    List<String> lines = readLines(complianceFilePath);
    boolean replaced = false;

    // if a record for the same household and monthYear exists, replace it; otherwise, append
    for (int i = 0; i < lines.size(); i++) {
        ComplianceRecord existing = parseCompliance(lines.get(i));
        if (existing.getHouseholdId().equals(r.getHouseholdId()) &&
            existing.getMonthYear().equals(r.getMonthYear())) {
            lines.set(i, serializeCompliance(r));
            replaced = true;
            break;
        }
    }

    if (replaced) writeLines(complianceFilePath, lines);
    else appendLine(complianceFilePath, serializeCompliance(r));
    }

    public ComplianceRecord findComplianceByHouseholdAndMonth(String householdId, String monthYear) {
        for (String line : readLines(complianceFilePath)) {
            ComplianceRecord r = parseCompliance(line);
            if (r.getHouseholdId().equals(householdId) && r.getMonthYear().equals(monthYear)) return r;
        }
        return null; //not found
    }

    public List<ComplianceRecord> findComplianceHistory(String householdId) {
        List<ComplianceRecord> history = new ArrayList<>();
        for (String line : readLines(complianceFilePath)) {
            ComplianceRecord r = parseCompliance(line);
            if (r.getHouseholdId().equals(householdId)) history.add(r);
        }
        return history;
    }
    
    //-----Grant methods-----

    public void saveGrant(GrantBreakdown g) {
          appendLine(grantFilePath, serializeGrant(g));
    }

    public GrantBreakdown findGrantByHouseholdAndMonth(String householdId, String monthYear) {
        for (String line : readLines(grantFilePath)) {
            GrantBreakdown g = parseGrant(line);
            if (g.getHouseholdId().equals(householdId) && g.getMonthYear().equals(monthYear)) return g;
        }
        return null; //not found
    }

    public List<GrantBreakdown> findGrantHistory(String householdId) {
        List<GrantBreakdown> history = new ArrayList<>();
        for (String line : readLines(grantFilePath)) {
            GrantBreakdown g = parseGrant(line);
            if (g.getHouseholdId().equals(householdId)) history.add(g);
        }
        return history;
    }

    //-----Helper methods-----
    private ComplianceRecord parseCompliance(String row) {
        String[] r = row.split(",", -1);
        return new ComplianceRecord(r[0], r[1], Boolean.parseBoolean(r[2]), Boolean.parseBoolean(r[3]),
                Boolean.parseBoolean(r[4]), Boolean.parseBoolean(r[5]), Boolean.parseBoolean(r[6]),
                Boolean.parseBoolean(r[7]), Integer.parseInt(r[8]));
    }

    private String serializeCompliance(ComplianceRecord r) {
        return String.join(",", r.getHouseholdId(), r.getMonthYear(),
                Boolean.toString(r.isPregnancyCareStatus()), Boolean.toString(r.isChild0to5HealthStatus()),
                Boolean.toString(r.isDewormingStatus()), Boolean.toString(r.isDaycareAttendanceStatus()),
                Boolean.toString(r.isSchoolAttendanceStatus()), Boolean.toString(r.isFdsAttendanceStatus()),
                Integer.toString(r.getChildrenMeetingAttendance()));
    }

    private GrantBreakdown parseGrant(String row) {
    String[] r = row.split(",", -1);
    // column order: householdId, monthYear, healthGrantAmount, educationGrantAmount,
    //               riceSubsidyAmount, totalAmount, withheldReasons

    String householdId = r[0];
    String monthYear = r[1];
    double healthGrantAmount = Double.parseDouble(r[2]);
    double educationGrantAmount = Double.parseDouble(r[3]);
    double riceSubsidyAmount = Double.parseDouble(r[4]);
    double totalAmount = Double.parseDouble(r[5]);

    List<String> withheldReasons = r[6].isEmpty()
        ? new ArrayList<>()
        : Arrays.asList(r[6].split(";"));

    return new GrantBreakdown(householdId, monthYear, healthGrantAmount,
        educationGrantAmount, riceSubsidyAmount, totalAmount, withheldReasons);
    }

    private String serializeGrant(GrantBreakdown g) {
        String withheldReasons = String.join(";", g.getWithheldReasons());
        return String.join(",", g.getHouseholdId(), g.getMonthYear(),
                Double.toString(g.getHealthGrantAmount()), Double.toString(g.getEducationGrantAmount()),
                Double.toString(g.getRiceSubsidyAmount()), Double.toString(g.getTotalAmount()),
                withheldReasons);
    }
}
