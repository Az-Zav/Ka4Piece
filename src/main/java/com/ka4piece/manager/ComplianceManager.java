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
        if (h == null) {
            throw new IllegalArgumentException("Household not found.");
        }
        if (h.getStatus().equals("EXITED"))
            throw new IllegalStateException("Cannot record compliance for an exited household.");

        // Auto-fill non-applicable conditions
        if (!h.isHasPregnantMember()) {
            r.setPregnancyCareStatus(true);
        }
        if (!h.isHas0to5Member()) {
            r.setChild0to5HealthStatus(true);
        }
        if (h.getElemCount() == 0 && h.getJhsCount() == 0 && h.getShsCount() == 0) {
            r.setDewormingStatus(true);
        }

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
    public GrantBreakdown calculateGrantBreakdown(ComplianceRecord r, Household household) {
        // Apply auto-fill logic for calculations
        boolean pregnancyCare = household.isHasPregnantMember() ? r.isPregnancyCareStatus() : true;
        boolean child0to5 = household.isHas0to5Member() ? r.isChild0to5HealthStatus() : true;
        boolean hasChildren = (household.getElemCount() > 0 || household.getJhsCount() > 0 || household.getShsCount() > 0);
        boolean deworming = (hasChildren || household.isHas0to5Member()) ? r.isDewormingStatus() : true;
        boolean dayCare = household.isHas0to5Member() ? r.isDaycareAttendanceStatus() : true;
        boolean schoolAttendance = hasChildren ? r.isSchoolAttendanceStatus() : true;

        boolean healthCompliant = pregnancyCare && child0to5 && deworming;
        boolean educationCompliant = dayCare && schoolAttendance;
        boolean riceEligible = healthCompliant || educationCompliant; // OR-based

        double healthGrantAmount = healthCompliant ? 750.0 : 0.0;

        // Clamp monthly met-attendance to household composition
        int elemMet = Math.min(r.getElementaryCount(), household.getElemCount());
        int jhsMet  = Math.min(r.getJuniorHighCount(), household.getJhsCount());
        int shsMet  = Math.min(r.getSeniorHighCount(), household.getShsCount());

        // Highest-tier-first precedence, capped at 3 children total
        int remaining = 3;
        int countedShs = Math.min(shsMet, remaining);
        remaining -= countedShs;
        int countedJhs = Math.min(jhsMet, remaining);
        remaining -= countedJhs;
        int countedElem = Math.min(elemMet, remaining);

        double educationGrantAmount = (countedShs * 700.0) + (countedJhs * 500.0) + (countedElem * 300.0);

        double riceSubsidyAmount = riceEligible ? 600.0 : 0.0;
        double totalAmount = healthGrantAmount + educationGrantAmount + riceSubsidyAmount;

        List<String> withheldReasons = new ArrayList<>();
        if (!healthCompliant)     withheldReasons.add("Health condition not met");
        if (!educationCompliant)  withheldReasons.add("Education attendance condition not met");
        if (!r.isFdsAttendanceStatus()) withheldReasons.add("FDS attendance not met");
        if (!riceEligible)        withheldReasons.add("Rice subsidy withheld — neither health nor education condition met");

        if (elemMet + jhsMet + shsMet > 3) {
            withheldReasons.add("Household has more than 3 eligible children meeting attendance this month — grant computed for the 3 highest-tier children (Senior High prioritized, then Junior High, then Elementary), per system policy since DSWD guidance does not specify precedence.");
        }

        return new GrantBreakdown(r.getHouseholdId(), r.getMonthYear(),
            healthGrantAmount, educationGrantAmount, riceSubsidyAmount, totalAmount, withheldReasons);
    }

    public GrantBreakdown computeMonthlyGrant(ComplianceRecord r, String recordType, String correctionReason) {
        Household household = authRepository.findHouseholdById(r.getHouseholdId());
        if (household == null) {
            throw new IllegalArgumentException("Household not found: " + r.getHouseholdId());
        }

        // Apply auto-fill logic to persistent record as well
        if (!household.isHasPregnantMember()) {
            r.setPregnancyCareStatus(true);
        }
        if (!household.isHas0to5Member()) {
            r.setChild0to5HealthStatus(true);
        }
        if (household.getElemCount() == 0 && household.getJhsCount() == 0 && household.getShsCount() == 0) {
            r.setDewormingStatus(true);
        }

        GrantBreakdown g = calculateGrantBreakdown(r, household);
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

    /**
     * Returns all grant breakdown records for the given household, ordered by month ascending.
     * Used to dynamically populate the Grant Breakdown History table on the beneficiary view.
     */
    public List<GrantBreakdown> getGrantHistory(String householdId) {
        return complianceRepository.findGrantHistory(householdId);
    }
}
