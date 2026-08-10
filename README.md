# 🧩 Ka4Piece — Barangay-Level 4Ps Compliance & Job-Matching System

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-3776AB?style=for-the-badge&logo=java&logoColor=white)
![CSV](https://img.shields.io/badge/Data-CSV%20Persistence-blue?style=for-the-badge)
![GitHub](https://img.shields.io/badge/Version%20Control-GitHub-black?style=for-the-badge&logo=github)
![VSCode](https://img.shields.io/badge/IDE-VS%20Code-007ACC?style=for-the-badge&logo=visualstudiocode)

---

## 🌍 Overview
**Ka4Piece** is a barangay-level digital system that modernizes **4Ps compliance tracking** and **local job matching**.  
It empowers **beneficiaries** to view their compliance status and grant breakdowns, while enabling **barangay officials** to record, verify, and manage compliance and job vacancies efficiently.

The system bridges the gap between manual CVF verification and local employment visibility — improving transparency, reducing bottlenecks, and promoting inclusive digital transformation at the barangay level.

---

## ✨ Key Features
- **Beneficiary Dashboard** — view compliance status, grant breakdowns, and matched job openings.  
- **Official Dashboard** — record household compliance, compute grants, and manage job vacancies.  
- **Automated Grant Computation** — applies the Phase 2 formula to determine eligibility and amounts.  
- **Job Matching Engine** — ranks vacancies and applicants using a weighted scoring strategy.  
- **Dual User Roles** — supports both barangay officials and beneficiaries with tailored interfaces.  
- **CSV‑Based Persistence** — lightweight, local data storage for offline‑ready operation.  
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
| **Repository (Persistence)** | CsvStore, AuthRepository, ComplianceRepository, JobMatchRepository | Handles CSV‑based data storage and retrieval. |
| **Strategy (Algorithm Layer)** | MatchStrategy, WeightedMatchStrategy | Defines and implements job‑matching scoring logic. |
| **Runner** | App | Entry point; wires repositories → managers → strategy → controllers; launches `Login.fxml`. |

---

## 🧰 Tech Stack
| Category | Technology | Purpose |
|-----------|-------------|----------|
| Language | **Java** | Core logic and MVC architecture |
| UI Framework | **JavaFX** | Interactive FXML‑based interface |
| Data Storage | **CSV** | Lightweight local persistence |
| IDE | **VS Code** | Development environment |
| Version Control | **Git + GitHub** | Repository management |

---

## 🚀 Getting Started
1. **Clone the repository**
   ```bash
   git clone https://github.com/Az-Zav/Ka4Piece.git
