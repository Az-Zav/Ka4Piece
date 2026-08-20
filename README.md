# 🧩 Ka4Piece — Barangay-Level 4Ps Compliance & Job-Matching System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-3776AB?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![GitHub](https://img.shields.io/badge/Version%20Control-GitHub-black?style=for-the-badge&logo=github)
![VSCode](https://img.shields.io/badge/IDE-VS%20Code-007ACC?style=for-the-badge&logo=visualstudiocode)

---

## 🌍 Overview
**Ka4Piece** is a barangay-level digital system that modernizes **4Ps compliance tracking** and **local job matching**.  
It empowers **beneficiaries** to view their compliance status and grant breakdowns, while enabling **barangay officials** to record and manage compliance and job vacancies efficiently.

The system bridges the gap between manual CVF verification and local visibility — improving transparency, reducing bottlenecks, and promoting inclusive digital transformation at the barangay level.

---

## ✨ Key Features
- **Beneficiary Dashboard** — view compliance status, grant breakdowns, and matched job openings.  
- **Official Dashboard** — record household compliance, compute grants, and manage job vacancies.  
- **Automated Grant Computation** — applies the official 4Ps compliance formulas on grant calculation to determine eligibility and amounts.  
- **Job Matching Engine** — ranks vacancies and applicants using a weighted scoring strategy.  
- **Dual User Roles** — supports both barangay officials and beneficiaries with tailored interfaces.  
- **MySQL Persistence** — full relational database storage via JDBC; connection details are externalized to `db.properties` for easy configuration.  
- **Transparent Session Handling** — secure login and dashboard routing via session objects.

---

## 🧠 Impact
### For Beneficiaries
- Real‑time visibility into compliance and grant reasoning.  
- Automated eligibility computation reduces manual delays and discrepancies.  
- Localized job matching reaches underserved populations beyond formal‑sector bias.  
- Direct access to relevant openings improves labor‑market awareness.

### For Officials
- Digital records replace manual CVF lookup.  
- Automated grant computation per household.  
- Faster candidate‑vacancy comparison and verification.

---

## 🧱 System Architecture
Ka4Piece follows a **layered MVC architecture**:

| Layer | Components | Description |
|-------|-------------|-------------|
| **Model** | Household, BarangayOfficial, ComplianceRecord, GrantBreakdown, JobseekerProfile, Vacancy, Session | Data holders flowing through all layers. |
| **Controller (View Layer)** | LoginController, OfficialDashboardController, BeneficiaryDashboardController | Handles UI logic and routes user actions to managers. |
| **Manager (Business Logic)** | AuthManager, ComplianceManager, JobMatchManager | Implements system rules and delegates persistence to repositories. |
| **Repository (Persistence)** | MySqlStore, AuthRepository, ComplianceRepository, JobMatchRepository | Handles MySQL‑based data storage and retrieval via JDBC. |
| **Strategy (Algorithm Layer)** | MatchStrategy, WeightedMatchStrategy | Defines and implements job‑matching scoring logic. |
| **Runner** | App | Entry point; reads `db.properties`, wires repositories → managers → strategy → controllers; launches `Login.fxml`. |

---

## 🗄️ Persistence — MySQL via JDBC

Ka4Piece uses **MySQL** as its relational database backend. All data access is handled through a shared `MySqlStore` base class that opens a `PreparedStatement`-based JDBC connection for every operation.

### How it works

| Class | Role |
|-------|------|
| `DbConfig` | Reads `jdbcUrl`, `dbUser`, and `dbPassword` from `db.properties` at startup. |
| `MySqlStore` | Abstract base class — provides `executeQuery()` (SELECT) and `executeUpdate()` (INSERT / UPDATE / DELETE) helpers. |
| `AuthRepository` | CRUD for `households` and `officials` tables. |
| `ComplianceRepository` | CRUD for `compliance` and `grants` tables. |
| `JobMatchRepository` | CRUD for `jobseekers` and `vacancies` tables. |

### Database tables

| Table | Primary Key | Description |
|-------|-------------|-------------|
| `households` | `householdId` | Registered 4Ps beneficiary households. |
| `officials` | `officialId` | Barangay officials with login access. |
| `compliance` | `(householdId, monthYear)` | Monthly compliance records per household. |
| `grants` | `id` (auto-increment) | Grant breakdown records; multiple rows per month support CORRECTION entries. |
| `jobseekers` | `jobseekerId` | Jobseeker profiles linked to a household. |
| `vacancies` | `vacancyId` | Job vacancies posted by officials. |

---

## 🧰 Tech Stack
| Category | Technology | Purpose |
|-----------|-------------|----------|
| Language | **Java** | Core logic and MVC architecture |
| UI Framework | **JavaFX** | Interactive FXML‑based interface |
| Database | **MySQL** | Relational persistence via JDBC |
| IDE | **VS Code** | Development environment |
| Version Control | **Git + GitHub** | Repository management |

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+** (with JavaFX SDK on the module path)
- **MySQL 8.0+** (local or remote instance)
- **VS Code** with the Extension Pack for Java

---

### 1. Clone the Repository

```bash
git clone https://github.com/Az-Zav/Ka4Piece.git
cd Ka4Piece
```

---

### 2. Set Up the MySQL Database

Open **MySQL Workbench** (or any MySQL client) and run the following SQL to create the database and all required tables:

```sql
-- Create and select the database
CREATE DATABASE IF NOT EXISTS ka4piece_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ka4piece_db;

-- ── Barangay Officials ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS officials (
    officialId  VARCHAR(50)  NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    isAdmin     BOOLEAN      NOT NULL DEFAULT FALSE
);

-- ── Beneficiary Households ──────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS households (
    householdId        VARCHAR(50)  NOT NULL PRIMARY KEY,
    headName           VARCHAR(100) NOT NULL,
    address            VARCHAR(255),
    barangay           VARCHAR(100),
    email              VARCHAR(100) NOT NULL UNIQUE,
    password           VARCHAR(255) NOT NULL,
    status             VARCHAR(50)  DEFAULT 'ACTIVE',
    exitReason         TEXT,
    exitDate           DATE,
    hasPregnantMember  BOOLEAN      NOT NULL DEFAULT FALSE,
    has0to5Member      BOOLEAN      NOT NULL DEFAULT FALSE,
    elemCount          INT          NOT NULL DEFAULT 0,
    jhsCount           INT          NOT NULL DEFAULT 0,
    shsCount           INT          NOT NULL DEFAULT 0
);

-- ── Monthly Compliance Records ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS compliance (
    householdId              VARCHAR(50)  NOT NULL,
    monthYear                VARCHAR(10)  NOT NULL,   -- e.g. '2025-06'
    pregnancyCareStatus      BOOLEAN      NOT NULL DEFAULT FALSE,
    child0to5HealthStatus    BOOLEAN      NOT NULL DEFAULT FALSE,
    dewormingStatus          BOOLEAN      NOT NULL DEFAULT FALSE,
    daycareAttendanceStatus  BOOLEAN      NOT NULL DEFAULT FALSE,
    schoolAttendanceStatus   BOOLEAN      NOT NULL DEFAULT FALSE,
    fdsAttendanceStatus      BOOLEAN      NOT NULL DEFAULT FALSE,
    elementaryCount          INT          NOT NULL DEFAULT 0,
    juniorHighCount          INT          NOT NULL DEFAULT 0,
    seniorHighCount          INT          NOT NULL DEFAULT 0,
    recordedByOfficialId     VARCHAR(50),
    recordedAt               DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (householdId, monthYear),
    FOREIGN KEY (householdId)          REFERENCES households(householdId),
    FOREIGN KEY (recordedByOfficialId) REFERENCES officials(officialId)
);

-- ── Grant Breakdowns ────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS grants (
    id                   INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    householdId          VARCHAR(50)  NOT NULL,
    monthYear            VARCHAR(10)  NOT NULL,
    healthGrantAmount    DOUBLE       NOT NULL DEFAULT 0,
    educationGrantAmount DOUBLE       NOT NULL DEFAULT 0,
    riceSubsidyAmount    DOUBLE       NOT NULL DEFAULT 0,
    totalAmount          DOUBLE       NOT NULL DEFAULT 0,
    withheldReasons      TEXT,
    recordType           VARCHAR(20)  NOT NULL DEFAULT 'INITIAL',  -- 'INITIAL' or 'CORRECTION'
    correctionReason     TEXT,
    FOREIGN KEY (householdId) REFERENCES households(householdId)
);

-- ── Jobseeker Profiles ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS jobseekers (
    jobseekerId      VARCHAR(50)  NOT NULL PRIMARY KEY,
    householdId      VARCHAR(50)  NOT NULL,
    memberName       VARCHAR(100) NOT NULL,
    education        VARCHAR(100),
    skills           TEXT,                -- semicolon-separated list
    experienceYears  INT          NOT NULL DEFAULT 0,
    location         VARCHAR(100),
    appliedVacancies TEXT,                -- serialized as "vacancyId:status;..."
    FOREIGN KEY (householdId) REFERENCES households(householdId)
);

-- ── Job Vacancies ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vacancies (
    vacancyId              VARCHAR(50)  NOT NULL PRIMARY KEY,
    title                  VARCHAR(150) NOT NULL,
    description            TEXT,
    educationRequirement   VARCHAR(100),
    skillRequirements      TEXT,                -- semicolon-separated list
    experienceYearsRequired INT          NOT NULL DEFAULT 0,
    location               VARCHAR(100),
    compensation           VARCHAR(100),
    type                   VARCHAR(50),         -- e.g. 'Full-time', 'Part-time'
    enteredByOfficialId    VARCHAR(50),
    status                 VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- 'ACTIVE', 'ARCHIVED', 'FILLED'
    archiveReason          TEXT,
    hiredJobseekerId       VARCHAR(50),
    FOREIGN KEY (enteredByOfficialId) REFERENCES officials(officialId)
);
```

> **Tip:** If you prefer to import from a file, save the SQL above as `ka4piece_schema.sql` and run:
> ```bash
> mysql -u root -p < ka4piece_schema.sql
> ```

---

### 3. Seed an Admin Official (Optional but Recommended)

Insert at least one barangay official so you can log in immediately:

```sql
USE ka4piece_db;

INSERT INTO officials (officialId, name, email, password, isAdmin)
VALUES ('OFF-001', 'Admin Official', 'admin@barangay.gov', 'admin123', TRUE);
```

> **Note:** Ka4Piece stores passwords as plain text by default during development. In production, integrate a hashing utility (see `PasswordUtil.java`) before seeding.

---

### 4. Create `db.properties`

In the **project root** (same level as `src/`), create a file named `db.properties`:

```
db.properties          ← place it here (next to src/, bin/, lib/)
```

Paste the following content and update the values to match your MySQL setup:

```properties
# ── Database connection ─────────────────────────────────────────────────────
jdbcUrl=jdbc:mysql://localhost:3306/ka4piece_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
dbUser=root
dbPassword=YOUR_MYSQL_PASSWORD

# ── (Optional) Service email credentials ────────────────────────────────────
# mailUser=your_email@gmail.com
# mailPassword=your_app_password
```

| Key | Description |
|-----|-------------|
| `jdbcUrl` | Full JDBC connection string. Change the host/port if MySQL is not on localhost. |
| `dbUser` | MySQL username (default: `root`). |
| `dbPassword` | The password for the MySQL user above. |

> ⚠️ **`db.properties` is listed in `.gitignore` and will never be committed.** Do not share or expose this file.

---

### 5. Build & Run

Open a terminal in the project root and compile + run via VS Code's Java runner, or use the classpath already configured in `.vscode/`:

```bash
# Example — adjust paths to your local JavaFX SDK
javac -cp "lib/*" -d bin $(find src -name "*.java")
java  -cp "bin:lib/*" --module-path /path/to/javafx-sdk/lib \
      --add-modules javafx.controls,javafx.fxml \
      com.ka4piece.app.App
```

On startup, `App.init()` reads `db.properties`, instantiates `DbConfig`, and wires all repositories before the login screen appears. If the database is unreachable, a `RuntimeException` will be thrown with the JDBC error message.

---

## 📁 Project Structure (Key Paths)

```
Ka4Piece/
├── db.properties                          ← DB credentials (not committed)
├── src/main/java/com/ka4piece/
│   ├── app/          App.java             ← Entry point & DI wiring
│   ├── controller/                        ← JavaFX FXML controllers
│   ├── manager/                           ← Business logic layer
│   ├── model/                             ← Plain data models
│   ├── repository/
│   │   ├── MySqlStore.java                ← Base JDBC helper
│   │   ├── DbConfig.java                  ← Reads db.properties
│   │   ├── AuthRepository.java            ← households & officials
│   │   ├── ComplianceRepository.java      ← compliance & grants
│   │   └── JobMatchRepository.java        ← jobseekers & vacancies
│   ├── strategy/                          ← Job-match scoring
│   └── utilities/                         ← PasswordUtil, IdUtil, email
└── src/main/java/view/                    ← FXML layout files
```
