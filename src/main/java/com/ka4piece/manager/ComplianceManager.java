package com.ka4piece.manager;

import com.ka4piece.model.ComplianceRecord;
import com.ka4piece.model.GrantBreakdown;
import com.ka4piece.model.Household;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.repository.ComplianceRepository;

import java.util.ArrayList;
import java.util.List;

public class ComplianceManager {
    ComplianceRepository complianceRepository;
    AuthRepository authRepository;   // needed to check household exit status

    public ComplianceManager(ComplianceRepository complianceRepository, AuthRepository authRepository) {
        this.complianceRepository = complianceRepository;
        this.authRepository = authRepository;
    }

    /**
     * Persists a compliance record. Rejects submissions for exited households.
     */
    public void recordConditionStatus(ComplianceRecord r) {
        Household h = authRepository.findHouseholdById(r.getHouseholdId());
        if (h.getStatus().equals("EXITED"))
            throw new IllegalStateException("Cannot record compliance for an exited household.");
        complianceRepository.saveCompliance(r);
    }

    /**
     * Computes the monthly grant breakdown from a compliance record and persists it.
     *
     * <p>Caller contract: OfficialDashboardController.recordMonthlyStatus() must call
     * getGrantBreakdown() first. If a row already exists for that household+month, the UI
     * prompts for a correction reason and passes recordType="CORRECTION"; otherwise
     * recordType="INITIAL", correctionReason=null.
     *
     * <p>Grant rules (per RA 11310):
     * <ul>
     *   <li>Health grant ₱750 — requires all three health conditions met.</li>
     *   <li>Education grant — ₱300/elem + ₱500/JHS + ₱700/SHS, combined cap of 3 children
     *       (counted elem first, then JHS, then SHS until cap is reached).</li>
     *   <li>Rice subsidy ₱600 — health OR education compliance; FDS no longer gates it.</li>
     * </ul>
     */
    public GrantBreakdown computeMonthlyGrant(ComplianceRecord r, String recordType, String correctionReason) {
        boolean healthCompliant = r.isPregnancyCareStatus() && r.isChild0to5HealthStatus()
                                   && r.isDewormingStatus();
        boolean educationCompliant = r.isDaycareAttendanceStatus() && r.isSchoolAttendanceStatus();
        boolean riceEligible = healthCompliant || educationCompliant; // OR-based, not AND of all 6

        double healthGrantAmount = healthCompliant ? 750.0 : 0.0;

        // Tiered rate, capped at 3 children total across all levels.
        // Cap-of-3 enforced by counting elem first, then jhs, then shs.
        int elem = r.getElementaryCount(), jhs = r.getJuniorHighCount(), shs = r.getSeniorHighCount();
        int totalChildren = elem + jhs + shs;
        double educationGrantAmount;
        if (totalChildren <= 3) {
            educationGrantAmount = elem * 300.0 + jhs * 500.0 + shs * 700.0;
        } else {
            int remaining = 3;
            int e = Math.min(elem, remaining); remaining -= e;
            int j = Math.min(jhs, remaining);  remaining -= j;
            int s = Math.min(shs, remaining);
            educationGrantAmount = e * 300.0 + j * 500.0 + s * 700.0;
        }

        double riceSubsidyAmount = riceEligible ? 600.0 : 0.0;
        double totalAmount = healthGrantAmount + educationGrantAmount + riceSubsidyAmount;

        List<String> withheldReasons = new ArrayList<>();
        if (!healthCompliant)     withheldReasons.add("Health condition not met");
        if (!educationCompliant)  withheldReasons.add("Education attendance condition not met");
        if (!r.isFdsAttendanceStatus()) withheldReasons.add("FDS attendance not met");
        if (!riceEligible)        withheldReasons.add("Rice subsidy withheld — neither health nor education condition met");

        GrantBreakdown g = new GrantBreakdown(r.getHouseholdId(), r.getMonthYear(),
            healthGrantAmount, educationGrantAmount, riceSubsidyAmount, totalAmount, withheldReasons);
        g.setRecordType(recordType);             // "INITIAL" or "CORRECTION"
        g.setCorrectionReason(correctionReason); // null if INITIAL
        complianceRepository.saveGrant(g);
        return g;
    }

    /** Returns the full compliance history for a household, ordered by month. */
    public List<ComplianceRecord> getComplianceHistory(String householdId) {
        return complianceRepository.findComplianceHistory(householdId);
    }

    /** Returns the compliance record for the current calendar month, or null if not yet recorded. */
    public ComplianceRecord getCurrentStatus(String householdId) {
        String currentMonth = java.time.YearMonth.now().toString();
        return complianceRepository.findComplianceByHouseholdAndMonth(householdId, currentMonth);
    }

    /**
     * Returns the most recent grant breakdown for the given household and month.
     * Readable via recordType field — not just recency.
     */
    public GrantBreakdown getGrantBreakdown(String householdId, String monthYear) {
        return complianceRepository.findGrantByHouseholdAndMonth(householdId, monthYear);
    }
}
