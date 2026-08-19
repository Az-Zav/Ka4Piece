# **PHASE 7: IMPLEMENTATION SPEC (MASTER DOC)**

**ka4piece — Barangay-Level 4Ps Compliance & Job-Matching System**

This document replaces the original Phase 7 (and its MySQL addendum) as the single source of truth for implementation. It mirrors the layers defined in Phase 5 (Architectural Plan) and the class list from Phase 6 (Class Design) — Phase 5–6 are **not edited**, so any place this doc adds a field, a method, or changes a formula beyond what Phase 5–6 describe is marked:

> ✅ **GAP FIX** — what was wrong, and what changed.

If you see no gap-fix tag on a class or method, it matches Phase 5–6 exactly. Persistence is **MySQL only** — CSV is dropped entirely, it does not exist on any branch going forward.

---

## **0\. HOW TO USE THIS DOCUMENT**

Read top to bottom once, then use it as a reference while coding — it's ordered the way you should build: bottom layer first (DB → persistence → managers → controllers → app), same dependency direction as Phase 5's architecture.

```
Models → MySqlStore → Repositories → Managers → Controllers → App
```

Every method below has exact logic. Nothing should be invented while coding — if something's genuinely undecided, it's flagged as **TBD** and needs a decision before you write code around a guess.

---

## **1\. GLOBAL CONVENTIONS**

| Convention | Rule |
| ----- | ----- |
| ID formats | `HH-xxxxxxxx`, `OFF-xxxxxxxx`, `JS-xxxxxxxx`, `VAC-xxxxxxxx` — 8-char UUID substring, uppercase prefix \+ hyphen |
| Date format | `monthYear` always `"YYYY-MM"`, from `YearMonth.now().toString()` |
| List-in-column encoding | Lists (`skills`, `skillRequirements`, `withheldReasons`) stored as single `TEXT` columns, `;`\-joined |
| Map-in-column encoding | `appliedVacancies` stored as single `TEXT` column: `vacId:STATUS;vacId:STATUS` |
| Null convention | "Not found" methods return `null`. Callers must null-check. No `Optional<T>`. |
| SQL | All queries use `PreparedStatement` binding (`?`). No string-concatenated SQL, ever. |
| Package structure | Unchanged from Phase 5/6: `model`, `persistence`, `service`, `strategy`, `controller`, `app` |

---

## **2\. DATABASE SCHEMA (DDL)**

✅ **GAP FIX** — this schema is the *only* persistence layer now (CSV dropped). Columns below already include every model field added by the Issues-list fixes; each new column is commented with which gap it closes.

```sql
CREATE TABLE households (
    householdId   VARCHAR(20) PRIMARY KEY,
    headName      VARCHAR(255) NOT NULL,
    address       VARCHAR(255) NOT NULL,
    barangay      VARCHAR(100) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    status        VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE|EXITED  [GAP FIX: exit visibility]
    exitReason    VARCHAR(255),                              -- [GAP FIX: exit visibility]
    exitDate      DATE                                        -- [GAP FIX: exit visibility]
);

CREATE TABLE officials (
    officialId    VARCHAR(20) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    isAdmin       BOOLEAN NOT NULL DEFAULT FALSE              -- [GAP FIX: admin bootstrapping]
);

CREATE TABLE compliance (
    householdId                 VARCHAR(20) NOT NULL,
    monthYear                   CHAR(7) NOT NULL,
    pregnancyCareStatus         BOOLEAN NOT NULL,
    child0to5HealthStatus       BOOLEAN NOT NULL,
    dewormingStatus             BOOLEAN NOT NULL,
    daycareAttendanceStatus     BOOLEAN NOT NULL,
    schoolAttendanceStatus      BOOLEAN NOT NULL,
    fdsAttendanceStatus         BOOLEAN NOT NULL,
    elementaryCount              INT NOT NULL DEFAULT 0,       -- [GAP FIX: tiered education grant]
    juniorHighCount               INT NOT NULL DEFAULT 0,      -- [GAP FIX: tiered education grant]
    seniorHighCount               INT NOT NULL DEFAULT 0,      -- [GAP FIX: tiered education grant]
    recordedByOfficialId        VARCHAR(20),                   -- [GAP FIX: accountability trail]
    PRIMARY KEY (householdId, monthYear)                       -- enables upsert
);

CREATE TABLE grants (
    id                    INT AUTO_INCREMENT PRIMARY KEY,     -- internal only, never exposed to app layer
    householdId           VARCHAR(20) NOT NULL,
    monthYear             CHAR(7) NOT NULL,
    healthGrantAmount     DOUBLE NOT NULL,
    educationGrantAmount  DOUBLE NOT NULL,
    riceSubsidyAmount     DOUBLE NOT NULL,
    totalAmount           DOUBLE NOT NULL,
    withheldReasons       TEXT,
    recordType            VARCHAR(12) NOT NULL DEFAULT 'INITIAL',  -- INITIAL|CORRECTION  [GAP FIX: duplicate-run ambiguity]
    correctionReason      VARCHAR(255)                              -- [GAP FIX: duplicate-run ambiguity]
);

CREATE TABLE jobseekers (
    jobseekerId       VARCHAR(20) PRIMARY KEY,
    householdId       VARCHAR(20) NOT NULL,
    memberName        VARCHAR(255) NOT NULL,
    education         VARCHAR(20) NOT NULL,
    skills            TEXT,
    experienceYears   INT NOT NULL,
    location          VARCHAR(100) NOT NULL,
    appliedVacancies  TEXT                                    -- values now include HIRED [GAP FIX: no hire record]
);

CREATE TABLE vacancies (
    vacancyId                 VARCHAR(20) PRIMARY KEY,
    title                     VARCHAR(255) NOT NULL,
    educationRequirement      VARCHAR(20) NOT NULL,
    skillRequirements         TEXT,
    experienceYearsRequired   INT NOT NULL,
    location                  VARCHAR(100) NOT NULL,
    compensation              VARCHAR(255),
    type                      VARCHAR(20) NOT NULL,
    enteredByOfficialId       VARCHAR(20) NOT NULL,
    status                    VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE|ARCHIVED  [GAP FIX: dead listings]
    archiveReason             VARCHAR(255),                            -- [GAP FIX: dead listings]
    hiredJobseekerId          VARCHAR(20)                              -- [GAP FIX: no hire record]
);
```

**Key design notes:**

* `compliance` composite primary key `(householdId, monthYear)` → enables `ON DUPLICATE KEY UPDATE` upsert.  
* `jobseekers.jobseekerId` alone as primary key → enables upsert keyed on ID only.  
* `grants` and `vacancies` are insert-only (ledger semantics) — no upsert clause on either.  
* `households.email` / `officials.email` are each unique **within their own table only** — see Section 5.1 for why this is now sufficient. *(✅ GAP FIX)*

---

## **3\. MODELS**

All fields private, getters/setters public. Mirrors Phase 6 Section A, with additions flagged.

```java
class Household {
    String householdId, headName, address, barangay, email, password;
    String status = "ACTIVE";     // ACTIVE|EXITED         
    String exitReason;                                        
    java.time.LocalDate exitDate;                              
}

class BarangayOfficial {
    String officialId, name, email, password;
    boolean isAdmin;                                           
}

class ComplianceRecord {
    String householdId, monthYear;
    boolean pregnancyCareStatus, child0to5HealthStatus, dewormingStatus,
            daycareAttendanceStatus, schoolAttendanceStatus, fdsAttendanceStatus;
    int elementaryCount, juniorHighCount, seniorHighCount;     
    String recordedByOfficialId;                                
}

class GrantBreakdown {
    String householdId, monthYear;
    double healthGrantAmount, educationGrantAmount, riceSubsidyAmount, totalAmount;
    List<String> withheldReasons;
    String recordType = "INITIAL";   // INITIAL|CORRECTION     
    String correctionReason;                                    
}

class JobseekerProfile {
    String jobseekerId, householdId, memberName, education, location;
    List<String> skills;
    int experienceYears;
    Map<String,String> appliedVacancies;  // values: APPLIED | FOR_INTERVIEW | REJECTED | HIRED
}

class Vacancy {
    String vacancyId, title, educationRequirement, location, compensation, type, enteredByOfficialId;
    List<String> skillRequirements;
    int experienceYearsRequired;
    String status = "ACTIVE";        // ACTIVE|ARCHIVED         ✅ GAP FIX
    String archiveReason;                                        // ✅ GAP FIX
    String hiredJobseekerId;                                     // ✅ GAP FIX, nullable
}

class Session {
    String userId, role, displayName;   // role is "OFFICIAL" or "HOUSEHOLD"
    boolean isAdmin;                     // copied from BarangayOfficial at login  ✅ GAP FIX
}

// Unchanged from Phase 6 — read-only DTOs, constructor-only, no setters
class RankedVacancy { Vacancy vacancy; double score; }
class RankedCandidate { JobseekerProfile profile; double score; String status; }
```

---

## **4\. PERSISTENCE BASE — `MySqlStore` (abstract)**

Unchanged in shape from Phase 5's design intent (shared plumbing, zero domain knowledge). `CsvStore` no longer exists on any branch.

| Method | Description |
| ----- | ----- |
| `protected Connection getConnection()` | Builds a JDBC `Connection` from `jdbcUrl`/`dbUser`/`dbPassword` passed into the constructor. |
| `protected List<Map<String,Object>> executeQuery(String sql, Object... params)` | Runs a parameterized `SELECT`, returns rows as column-name → value maps. |
| `protected int executeUpdate(String sql, Object... params)` | Runs a parameterized `INSERT`/`UPDATE`, returns affected row count. |
| `public String generateId(String prefix)` | `prefix + "-" + UUID.randomUUID().toString().substring(0, 8)`. Kept `public` so Managers in another package can call it on a Repository reference. |

`DbConfig` holds `jdbcUrl`, `dbUser`, `dbPassword`; read once in `App.start()` before repository construction, same pattern as CSV file paths were passed in the original design.

---

## **5\. REPOSITORIES**

Same class names and same public signature *shape* as Phase 6 — every method below is annotated where its behavior changed from the original spec.

### **5.1 `AuthRepository extends MySqlStore`**

| Method | Behavior |
| ----- | ----- |
| `saveHousehold(Household h)` | `INSERT ... ON DUPLICATE KEY UPDATE` (upsert). ✅ **GAP FIX** — was insert-only; now supports editing typos/address changes and exit-status updates. Caller has already set `householdId` before calling. |
| `findHouseholdById(String householdId)` | `SELECT * WHERE householdId = ?` |
| `findHouseholdByemail(String email)` | `SELECT * WHERE email = ?` |
| `findAllHouseholds()` | `SELECT *` |
| `saveOfficial(BarangayOfficial o)` | `INSERT ... ON DUPLICATE KEY UPDATE` (upsert). ✅ **GAP FIX** — was insert-only; now supports password reset and `isAdmin` toggling. |
| `findOfficialByemail(String email)` | `SELECT * WHERE email = ?` |

✅ **GAP FIX — role-scoped login.** Original design had `login()` try households then officials on the same email, which meant a household and an official could collide on email with undefined behavior. Since login now takes an explicit `role` (Section 6.1), lookups are scoped to one table, and email uniqueness only needs to hold **within** `households` or **within** `officials` — not across both. No cross-table check needed anywhere.

### **5.2 `ComplianceRepository extends MySqlStore`**

| Method | Behavior |
| ----- | ----- |
| `saveCompliance(ComplianceRecord r)` | `INSERT ... ON DUPLICATE KEY UPDATE` on `(householdId, monthYear)` — unchanged from original upsert intent. |
| `findComplianceByHouseholdAndMonth(...)` | `SELECT * WHERE householdId=? AND monthYear=?` |
| `findComplianceHistory(String householdId)` | `SELECT * WHERE householdId=? ORDER BY monthYear` |
| `saveGrant(GrantBreakdown g)` | `INSERT` only — **never** `ON DUPLICATE KEY UPDATE`. Ledger semantics preserved even with the correction flow (Section 6.2) — a correction is a *new row*, not an overwrite. |
| `findGrantByHouseholdAndMonth(...)` | `SELECT * WHERE householdId=? AND monthYear=? ORDER BY id DESC LIMIT 1` — returns most recent (i.e. the latest correction if one exists). |
| `findGrantHistory(String householdId)` | `SELECT * WHERE householdId=?` |

### **5.3 `JobMatchRepository extends MySqlStore`**

| Method | Behavior |
| ----- | ----- |
| `saveJobseeker(JobseekerProfile p)` | `INSERT ... ON DUPLICATE KEY UPDATE` keyed on `jobseekerId` alone — unchanged from original; this is also what makes `recordApplication`/`updateApplicationStatus`/`removeApplication` work (whole row overwritten). |
| `findJobseekerById(...)` / `findJobseekersByHousehold(...)` / `findAllJobseekers()` | Straightforward `SELECT`s. |
| `saveVacancy(Vacancy v)` | `INSERT` only on creation. ✅ **GAP FIX** — archiving (Section 6.3) is now a separate `updateVacancyStatus()` call, not a full re-insert. |
| `updateVacancyStatus(String vacancyId, String status, String archiveReason, String hiredJobseekerId)` | ✅ **GAP FIX — new method.** `UPDATE vacancies SET status=?, archiveReason=?, hiredJobseekerId=? WHERE vacancyId=?`. |
| `findVacancyById(...)` / `findAllVacancies()` | Straightforward `SELECT`s. `findAllVacancies()` still returns everything (archived included) — filtering happens in the Manager, see 6.3. |

---

## **6\. MANAGERS**

### **6.1 `AuthManager`**

```java
class AuthManager {
    AuthRepository authRepository;

    // ✅ GAP FIX — role param added; login is now scoped to one table instead of
    // trying household-then-official on the same email.
    Session login(String role, String email, String password) {
        if (role.equals("HOUSEHOLD")) {
            Household h = authRepository.findHouseholdByemail(email);
            if (h == null || !h.getPassword().equals(password)) return null;
            if (h.getStatus().equals("EXITED")) return null;   // ✅ GAP FIX — exited households can't log in
            Session s = new Session(h.getHouseholdId(), "HOUSEHOLD", h.getHeadName());
            return s;
        } else {
            BarangayOfficial o = authRepository.findOfficialByemail(email);
            if (o == null || !o.getPassword().equals(password)) return null;
            Session s = new Session(o.getOfficialId(), "OFFICIAL", o.getName());
            s.setAdmin(o.isAdmin());   // ✅ GAP FIX
            return s;
        }
    }

    void logout() { /* no persistence action, clears in-memory session reference */ }

    void registerHousehold(Household h) {
        // ✅ GAP FIX — duplicate-email guard, scoped to households table only
        if (authRepository.findHouseholdByemail(h.getemail()) != null)
            throw new IllegalArgumentException("email already taken.");
        h.setHouseholdId(authRepository.generateId("HH"));
        h.setStatus("ACTIVE");
        authRepository.saveHousehold(h);
    }

    // ✅ GAP FIX — new method, admin-gated
    void registerOfficial(BarangayOfficial o, Session callerSession) {
        if (!callerSession.isAdmin())
            throw new SecurityException("Only an admin official can register new officials.");
        if (authRepository.findOfficialByemail(o.getemail()) != null)
            throw new IllegalArgumentException("email already taken.");
        o.setOfficialId(authRepository.generateId("OFF"));
        authRepository.saveOfficial(o);
    }

    List<Household> searchHouseholds(String query) {
        String q = query.toLowerCase().trim();
        return authRepository.findAllHouseholds().stream()
            .filter(h -> h.getHeadName().toLowerCase().contains(q)
                      || h.getAddress().toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    // ✅ GAP FIX — new method, official-assisted password reset (no email/token infra)
    void resetPassword(String userId, String role, String newPassword) {
        if (role.equals("HOUSEHOLD")) {
            Household h = authRepository.findHouseholdById(userId);
            h.setPassword(newPassword);
            authRepository.saveHousehold(h);
        } else {
            BarangayOfficial o = authRepository.findOfficialById(userId); // add this finder if missing
            o.setPassword(newPassword);
            authRepository.saveOfficial(o);
        }
    }

    // ✅ GAP FIX — new method, officer-toggled exit
    void setHouseholdExitStatus(String householdId, String status, String exitReason, LocalDate exitDate) {
        Household h = authRepository.findHouseholdById(householdId);
        h.setStatus(status);
        h.setExitReason(exitReason);
        h.setExitDate(exitDate);
        authRepository.saveHousehold(h);
    }
}
```

### **6.2 `ComplianceManager`**

```java
class ComplianceManager {
    ComplianceRepository complianceRepository;
    AuthRepository authRepository;   // ✅ GAP FIX — needed to check household exit status

    void recordConditionStatus(ComplianceRecord r) {
        // ✅ GAP FIX — refuse to record for an exited household
        Household h = authRepository.findHouseholdById(r.getHouseholdId());
        if (h.getStatus().equals("EXITED"))
            throw new IllegalStateException("Cannot record compliance for an exited household.");
        complianceRepository.saveCompliance(r);
    }

    // ✅ GAP FIX — three changes from the original formula:
    //   1. rice subsidy now OR-based (health OR education), not AND-based across all 6 flags
    //   2. education grant is tiered by school level, not flat ₱500/child
    //   3. accepts recordType + correctionReason for the duplicate-run ledger fix
    GrantBreakdown computeMonthlyGrant(ComplianceRecord r, String recordType, String correctionReason) {
        boolean healthCompliant = r.isPregnancyCareStatus() && r.isChild0to5HealthStatus()
                                   && r.isDewormingStatus();
        boolean educationCompliant = r.isDaycareAttendanceStatus() && r.isSchoolAttendanceStatus();
        boolean riceEligible = healthCompliant || educationCompliant;   // ✅ GAP FIX — was AND of all 3

        double healthGrantAmount = healthCompliant ? 750.0 : 0.0;

        // ✅ GAP FIX — tiered rate, capped at 3 children total across all levels
        int elem = r.getElementaryCount(), jhs = r.getJuniorHighCount(), shs = r.getSeniorHighCount();
        int totalChildren = elem + jhs + shs;
        double educationGrantAmount;
        if (totalChildren <= 3) {
            educationGrantAmount = elem * 300.0 + jhs * 500.0 + shs * 700.0;
        } else {
            // cap-of-3 enforced by trusting data entry order: elem counted first, then jhs, then shs,
            // until the 3-child cap is reached — flag with team if a different tie-break is preferred
            int remaining = 3;
            int e = Math.min(elem, remaining); remaining -= e;
            int j = Math.min(jhs, remaining);  remaining -= j;
            int s = Math.min(shs, remaining);
            educationGrantAmount = e * 300.0 + j * 500.0 + s * 700.0;
        }

        double riceSubsidyAmount = riceEligible ? 600.0 : 0.0;
        double totalAmount = healthGrantAmount + educationGrantAmount + riceSubsidyAmount;

        List<String> withheldReasons = new ArrayList<>();
        if (!healthCompliant) withheldReasons.add("Health condition not met");
        if (!educationCompliant) withheldReasons.add("Education attendance condition not met");
        if (!r.isFdsAttendanceStatus()) withheldReasons.add("FDS attendance not met");
        if (!riceEligible) withheldReasons.add("Rice subsidy withheld — neither health nor education condition met");

        GrantBreakdown g = new GrantBreakdown(r.getHouseholdId(), r.getMonthYear(),
            healthGrantAmount, educationGrantAmount, riceSubsidyAmount, totalAmount, withheldReasons);
        g.setRecordType(recordType);           // "INITIAL" or "CORRECTION"
        g.setCorrectionReason(correctionReason); // null if INITIAL
        complianceRepository.saveGrant(g);
        return g;
    }

    List<ComplianceRecord> getComplianceHistory(String householdId) {
        return complianceRepository.findComplianceHistory(householdId);
    }
    ComplianceRecord getCurrentStatus(String householdId) {
        String currentMonth = java.time.YearMonth.now().toString();
        return complianceRepository.findComplianceByHouseholdAndMonth(householdId, currentMonth);
    }
    GrantBreakdown getGrantBreakdown(String householdId, String monthYear) {
        return complianceRepository.findGrantByHouseholdAndMonth(householdId, monthYear);
        // returns most recent row for that month — self-explanatory via recordType, not just recency
    }
}
```

**Caller contract for the correction flow:** `OfficialDashboardController.recordMonthlyStatus()` calls `getGrantBreakdown()` first — if a row already exists for that household+month, the UI must prompt for a `correctionReason` and pass `recordType="CORRECTION"`; otherwise `recordType="INITIAL"` and `correctionReason=null`.

### **6.3 `JobMatchManager`**

```java
class JobMatchManager {
    JobMatchRepository jobMatchRepository;
    MatchStrategy matchStrategy;

    void createVacancy(Vacancy v) {
        v.setVacancyId(jobMatchRepository.generateId("VAC"));
        v.setStatus("ACTIVE");
        jobMatchRepository.saveVacancy(v);
    }
    void createProfile(JobseekerProfile p) {
        p.setJobseekerId(jobMatchRepository.generateId("JS"));
        p.setAppliedVacancies(new HashMap<>());
        jobMatchRepository.saveJobseeker(p);
    }

    // ✅ GAP FIX — new method, thin wrapper over existing upsert
    void updateProfile(JobseekerProfile p) {
        jobMatchRepository.saveJobseeker(p);
    }

    List<JobseekerProfile> getProfilesForHousehold(String householdId) {
        return jobMatchRepository.findJobseekersByHousehold(householdId);
    }

    List<RankedVacancy> getRankedVacancies(String jobseekerId) {
        JobseekerProfile profile = jobMatchRepository.findJobseekerById(jobseekerId);
        return jobMatchRepository.findAllVacancies().stream()
            .filter(v -> v.getStatus().equals("ACTIVE"))   // ✅ GAP FIX — hide archived/filled vacancies
            .map(v -> new RankedVacancy(v, matchStrategy.score(profile, v)))
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    List<RankedCandidate> getRankedApplicants(String vacancyId) {
        Vacancy vacancy = jobMatchRepository.findVacancyById(vacancyId);
        return jobMatchRepository.findAllJobseekers().stream()
            .filter(p -> p.getAppliedVacancies().containsKey(vacancyId))
            .map(p -> new RankedCandidate(p, matchStrategy.score(p, vacancy),
                                           p.getAppliedVacancies().get(vacancyId)))
            .sorted((a, b) -> Double.compare(b.getScore(), a.getScore()))
            .collect(Collectors.toList());
    }

    // ✅ GAP FIX — guard narrowed so REJECTED (or no prior application) can (re)apply;
    // only FOR_INTERVIEW and HIRED are protected from being reset back to APPLIED.
    void recordApplication(String jobseekerId, String vacancyId) {
        JobseekerProfile p = jobMatchRepository.findJobseekerById(jobseekerId);
        String current = p.getAppliedVacancies().get(vacancyId);
        if (current != null && (current.equals("FOR_INTERVIEW") || current.equals("HIRED"))) {
            return;   // protected — do not overwrite official's progress
        }
        p.getAppliedVacancies().put(vacancyId, "APPLIED");
        jobMatchRepository.saveJobseeker(p);
    }

    // ✅ GAP FIX — new method, mirrors recordApplication
    void removeApplication(String jobseekerId, String vacancyId) {
        JobseekerProfile p = jobMatchRepository.findJobseekerById(jobseekerId);
        p.getAppliedVacancies().remove(vacancyId);
        jobMatchRepository.saveJobseeker(p);
    }

    // ✅ GAP FIX — HIRED is now a valid status; setting it auto-archives the vacancy
    void updateApplicationStatus(String jobseekerId, String vacancyId, String status) {
        // status is "FOR_INTERVIEW", "REJECTED", or "HIRED" — caller validates
        JobseekerProfile p = jobMatchRepository.findJobseekerById(jobseekerId);
        p.getAppliedVacancies().put(vacancyId, status);
        jobMatchRepository.saveJobseeker(p);

        if (status.equals("HIRED")) {
            jobMatchRepository.updateVacancyStatus(vacancyId, "ARCHIVED", "Filled", jobseekerId);
        }
    }

    Vacancy getVacancyDetail(String vacancyId) {
        return jobMatchRepository.findVacancyById(vacancyId);
    }
}
```

### **6.4 `WeightedMatchStrategy` — unchanged**

Formula, weights (0.30/0.35/0.20/0.15), and scoring logic are identical to Phase 6 Section E / Phase 7 Section 5.4. No gaps found here.

---

## **7\. CONTROLLERS**

### **7.1 `LoginController`**

✅ **GAP FIX — explicit role toggle.** UI now has two RadioButtons (or a ToggleGroup) for "Beneficiary" / "Official"; the selected value is passed into `login()`.

```java
class LoginController {
    AuthManager authManager;
    ComplianceManager complianceManager;
    JobMatchManager matchManager;
    @FXML TextField emailField;
    @FXML PasswordField passwordField;
    @FXML RadioButton beneficiaryToggle, officialToggle;   // ✅ GAP FIX
    @FXML Label errorLabel;

    @FXML void handleLogin() {
        String role = beneficiaryToggle.isSelected() ? "HOUSEHOLD" : "OFFICIAL";   // ✅ GAP FIX
        Session session = authManager.login(role, emailField.getText(), passwordField.getText());
        if (session == null) {
            showError("Invalid email or password.");
            return;
        }
        // ... FXML load + inject Managers + loadSession(session), same pattern as before
    }
    void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
```

### **7.2 `OfficialDashboardController` — additions only**

Everything from the original Phase 6/7 spec stands. New pieces:

| Addition | Why |
| ----- | ----- |
| Admin-only "Register Official" form, calls `authManager.registerOfficial(o, session)` | ✅ GAP FIX — admin bootstrapping |
| `recordMonthlyStatus()` now checks `complianceManager.getGrantBreakdown()` first; if non-null, shows a reason text field and passes `recordType="CORRECTION"` | ✅ GAP FIX — duplicate-run ledger |
| Household exit toggle (status/reason/date fields) calling `authManager.setHouseholdExitStatus()` | ✅ GAP FIX — exit visibility |
| Applicant status dropdown now includes `"HIRED"` as a 4th option, in addition to `FOR_INTERVIEW`/`REJECTED` | ✅ GAP FIX — hire record |
| "Reset Password" action per household/official row, calling `authManager.resetPassword()` | ✅ GAP FIX — no reset path |

### **7.3 `BeneficiaryDashboardController` — additions only**

| Addition | Why |
| ----- | ----- |
| Household status banner ("Active" / "Exited — \[reason\], \[date\]") on the My Compliance tab | ✅ GAP FIX — exit visibility |
| "Withdraw Application" button next to `applyButton`, calling `matchManager.removeApplication(...)` | ✅ GAP FIX — withdraw path |
| "Edit Profile" action on the selected `JobseekerProfile`, calling `matchManager.updateProfile(...)` | ✅ GAP FIX — profile edit path |
| `markApplied()` unaffected in signature — now correctly allowed to re-fire after a REJECTED status thanks to the manager-level guard fix | ✅ GAP FIX — reapply after rejection |

---

## **8\. `App` — WIRING**

✅ **GAP FIX** — single repository-construction path, MySQL only. No CSV branch, no toggle.

```java
class App extends Application {
    public void start(Stage primaryStage) throws Exception {
        DbConfig config = DbConfig.load();   // reads jdbcUrl/dbUser/dbPassword once

        AuthRepository authRepository = new AuthRepository(config.jdbcUrl, config.dbUser, config.dbPassword);
        ComplianceRepository complianceRepository = new ComplianceRepository(config.jdbcUrl, config.dbUser, config.dbPassword);
        JobMatchRepository jobMatchRepository = new JobMatchRepository(config.jdbcUrl, config.dbUser, config.dbPassword);

        AuthManager authManager = new AuthManager(authRepository);
        ComplianceManager complianceManager = new ComplianceManager(complianceRepository, authRepository);
        WeightedMatchStrategy matchStrategy = new WeightedMatchStrategy();
        JobMatchManager matchManager = new JobMatchManager(jobMatchRepository, matchStrategy);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        LoginController loginController = loader.getController();
        loginController.authManager = authManager;
        loginController.complianceManager = complianceManager;
        loginController.matchManager = matchManager;

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
```

---

## **9\. INTEGRATION CHECKLIST**

Run in order once all layers compile:

1. `App.start()` launches to login screen with the role toggle visible.  
2. Seed one admin official manually in `officials` table (`isAdmin=TRUE`) — this is the one-time manual bootstrap step, not a UI feature.  
3. Log in as that admin official → register a second official through the UI → confirm `isAdmin` defaults false and the row appears.  
4. Register a household → confirm row in `households`, `status='ACTIVE'`.  
5. Log in as that household using the Beneficiary toggle → confirm correct dashboard loads.  
6. Official records compliance with health conditions met, education conditions unmet → confirm `riceSubsidyAmount = 600` (OR-logic: health alone qualifies) and `educationGrantAmount` reflects tiered rates for whatever child counts were entered.  
7. Official re-submits the same household/month → UI prompts for a correction reason → confirm two rows exist in `grants` (`INITIAL` then `CORRECTION`), `getGrantBreakdown()` returns the newer one.  
8. Official enters a vacancy → beneficiary applies → official marks `FOR_INTERVIEW` → beneficiary re-applies → confirm status stays `FOR_INTERVIEW` (not reset).  
9. Official marks the same applicant `REJECTED` → beneficiary re-applies → confirm status returns to `APPLIED`. **(gap-fix verification)**  
10. Official marks an applicant `HIRED` → confirm the vacancy's `status` becomes `ARCHIVED`, `hiredJobseekerId` is set, and it disappears from other beneficiaries' ranked vacancy lists.  
11. Official toggles a household to `EXITED` with a reason → confirm the beneficiary dashboard shows the exit banner and `recordConditionStatus()` throws/blocks further entries for that household.  
12. Restart the app entirely → confirm all of the above persisted (real DB test, not in-memory).

---

## **10\. TRACEABILITY — ISSUES LIST → THIS DOC**

Every row of the Issues list maps to a component above. Nothing in the Issues list was left unaddressed; nothing was added beyond it except where explicitly needed for wiring (e.g. `findOfficialById` as a helper for `resetPassword`).

