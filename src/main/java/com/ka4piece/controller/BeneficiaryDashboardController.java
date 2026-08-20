package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.manager.JobMatchManager;
import com.ka4piece.model.JobseekerProfile;
import com.ka4piece.model.Session;
import com.ka4piece.model.Vacancy;
import com.ka4piece.repository.AuthRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class BeneficiaryDashboardController {
    private AuthRepository authRepository;
    private ComplianceManager complianceManager;
    private JobMatchManager jobMatchManager;

    private JobseekerProfile selectedProfile;
    private Vacancy selectedVacancy;

    // FXML Control Injections
    @FXML private Button btnBackToCurrentMonth;
    @FXML private Button btnApplyNow;
    @FXML private VBox boxApplicationHistory;
    @FXML private Label lblJobDetailTitle;
    @FXML private Label lblJobDetailCompany;

    // History table container (rows built dynamically from DB)
    @FXML private VBox boxGrantHistory;

    // Grant Breakdown Dynamic Labels
    @FXML private Label lblHealthGrant;
    @FXML private Label lblEducationGrant;
    @FXML private Label lblRiceSubsidy;
    @FXML private Label lblTotalGrant;

    // Notice Box Controls
    @FXML private VBox boxNoticeCard;
    @FXML private Label lblNoticeText;

    // Compliance card injections
    @FXML private HBox cardPregnancy;
    @FXML private Label lblPregnancyCheck;
    @FXML private HBox cardDeworming;
    @FXML private Label lblDewormingCheck;
    @FXML private HBox cardSchool;
    @FXML private Label lblSchoolCheck;
    @FXML private HBox cardFds;
    @FXML private Label lblFdsCheck;
    @FXML private Label lblFdsTitle;
    @FXML private HBox cardNutrition;
    @FXML private Label lblNutritionCheck;
    @FXML private HBox cardDaycare;
    @FXML private Label lblDaycareCheck;

    // Job Matches card injections
    @FXML private ComboBox<JobseekerProfile> cmbProfiles;
    @FXML private VBox boxRankedVacancies;
    @FXML private Label lblJobDetailMatch;
    @FXML private Label lblJobDetailLocation;
    @FXML private Label lblJobDetailCompensation;
    @FXML private Label lblJobDetailType;
    @FXML private Label lblJobDetailDesc;
    @FXML private Label lblJobDetailEducation;
    @FXML private Label lblJobDetailSkills;
    @FXML private Label lblJobDetailExperience;

    public BeneficiaryDashboardController(AuthRepository authRepository, ComplianceManager complianceManager, JobMatchManager jobMatchManager) {
        this.authRepository = authRepository;
        this.complianceManager = complianceManager;
        this.jobMatchManager = jobMatchManager;
    }

    public BeneficiaryDashboardController() {
    }

    @FXML
    public void initialize() {
        // Initialize with current month data
        String currentMonth = java.time.YearMonth.now().toString();
        selectMonth(currentMonth);

        // Build the Grant Breakdown History table dynamically from the DB
        loadGrantHistory();

        // Setup Profile dropdown and load data
        if (cmbProfiles != null) {
            setupProfilesDropdown();
            loadProfilesDropdown();
        }
    }

    // --- GRANT HISTORY DYNAMIC LOADING ---

    /**
     * Fetches all grant breakdown records for the logged-in household from the database
     * and builds the history table rows dynamically.
     */
    private void loadGrantHistory() {
        if (boxGrantHistory == null || complianceManager == null) return;
        boxGrantHistory.getChildren().clear();

        String householdId = Session.getInstance().getUserId();
        if (householdId == null) return;

        java.util.List<com.ka4piece.model.GrantBreakdown> history;
        try {
            history = complianceManager.getGrantHistory(householdId);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (history.isEmpty()) {
            javafx.scene.control.Label emptyLabel = new javafx.scene.control.Label("No grant history recorded yet.");
            emptyLabel.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-padding: 15px 20px;");
            boxGrantHistory.getChildren().add(emptyLabel);
            return;
        }

        // Render newest first so the most recent month appears at the top
        java.util.List<com.ka4piece.model.GrantBreakdown> sorted = new java.util.ArrayList<>(history);
        sorted.sort((a, b) -> b.getMonthYear().compareTo(a.getMonthYear()));

        for (int i = 0; i < sorted.size(); i++) {
            com.ka4piece.model.GrantBreakdown g = sorted.get(i);

            javafx.scene.layout.GridPane row = new javafx.scene.layout.GridPane();
            row.getStyleClass().add("history-row");
            row.setStyle("-fx-cursor: hand;");

            javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
            col1.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
            col2.setHgrow(javafx.scene.layout.Priority.NEVER);
            col2.setPrefWidth(150.0);
            row.getColumnConstraints().addAll(col1, col2);

            // Format monthYear "YYYY-MM" → "MMM, YYYY" (e.g. "2026-08" → "AUG, 2026")
            String displayDate = formatMonthYear(g.getMonthYear());

            javafx.scene.control.Label dateLabel = new javafx.scene.control.Label(displayDate);
            dateLabel.getStyleClass().add("history-date");

            javafx.scene.control.Label amountLabel = new javafx.scene.control.Label(
                    String.format("\u20B1%,.2f", g.getTotalAmount()));
            amountLabel.getStyleClass().add("history-amount");

            javafx.geometry.Insets padding = new javafx.geometry.Insets(12, 20, 12, 20);
            row.setPadding(padding);
            row.add(dateLabel, 0, 0);
            row.add(amountLabel, 1, 0);

            // Store the monthYear on the row for highlight tracking
            row.setUserData(g.getMonthYear());

            String monthKey = g.getMonthYear();
            row.setOnMouseClicked(e -> selectMonth(monthKey));

            boxGrantHistory.getChildren().add(row);

            if (i < sorted.size() - 1) {
                boxGrantHistory.getChildren().add(new javafx.scene.control.Separator());
            }
        }

        // Re-highlight whichever month is currently selected
        highlightSelectedRow(java.time.YearMonth.now().toString());
    }

    /** Converts "YYYY-MM" to "MMM, YYYY" display format (e.g. "2026-08" → "AUG, 2026"). */
    private String formatMonthYear(String monthYear) {
        try {
            java.time.YearMonth ym = java.time.YearMonth.parse(monthYear);
            return ym.getMonth().getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH).toUpperCase()
                    + ", " + ym.getYear();
        } catch (Exception e) {
            return monthYear;
        }
    }

    // --- ROW SELECTION UX FLOW ---

    private String selectedMonth = null;

    private void selectMonth(String monthKey) {
        // Normalize to "YYYY-MM" format for DB lookups
        String dbMonth = monthKey; // already in YYYY-MM form from dynamic rows

        selectedMonth = dbMonth;

        String currentMonth = java.time.YearMonth.now().toString();
        if (currentMonth.equals(dbMonth)) {
            if (btnBackToCurrentMonth != null) btnBackToCurrentMonth.setVisible(false);
        } else {
            if (btnBackToCurrentMonth != null) {
                btnBackToCurrentMonth.setVisible(true);
                btnBackToCurrentMonth.setOnAction(e -> selectMonth(currentMonth));
            }
        }

        highlightSelectedRow(dbMonth);

        String householdId = Session.getInstance().getUserId();
        boolean loadedFromDb = false;

        if (householdId != null && complianceManager != null) {
            try {
                com.ka4piece.model.ComplianceRecord record = complianceManager.getComplianceHistory(householdId).stream()
                        .filter(r -> r.getMonthYear().equals(dbMonth))
                        .findFirst()
                        .orElse(null);

                com.ka4piece.model.GrantBreakdown breakdown = complianceManager.getGrantBreakdown(householdId, dbMonth);

                if (record != null || breakdown != null) {
                    updateComplianceCards(record);
                    updateGrantBreakdown(breakdown);
                    loadedFromDb = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!loadedFromDb) {
            // No record found — clear the compliance cards and grant amounts
            updateComplianceCards(null);
            updateGrantBreakdown(null);
        }
    }

    private void updateComplianceCards(com.ka4piece.model.ComplianceRecord record) {
        if (record == null) {
            setCardStatus(cardPregnancy, lblPregnancyCheck, null, false);
            setCardStatus(cardDeworming, lblDewormingCheck, null, false);
            setCardStatus(cardSchool, lblSchoolCheck, null, false);
            setCardStatus(cardFds, lblFdsCheck, lblFdsTitle, false);
            setCardStatus(cardNutrition, lblNutritionCheck, null, false);
            setCardStatus(cardDaycare, lblDaycareCheck, null, false);
            return;
        }

        setCardStatus(cardPregnancy, lblPregnancyCheck, null, record.isPregnancyCareStatus());
        setCardStatus(cardDeworming, lblDewormingCheck, null, record.isDewormingStatus());
        setCardStatus(cardSchool, lblSchoolCheck, null, record.isSchoolAttendanceStatus());
        setCardStatus(cardFds, lblFdsCheck, lblFdsTitle, record.isFdsAttendanceStatus());
        setCardStatus(cardNutrition, lblNutritionCheck, null, record.isChild0to5HealthStatus());
        setCardStatus(cardDaycare, lblDaycareCheck, null, record.isDaycareAttendanceStatus());
    }

    private void setCardStatus(HBox card, Label checkLabel, Label titleLabel, boolean passed) {
        if (card == null || checkLabel == null) return;
        card.getStyleClass().removeAll("pass-card", "fail-card");
        checkLabel.getStyleClass().removeAll("icon-check", "icon-cross");

        if (passed) {
            card.getStyleClass().add("pass-card");
            checkLabel.getStyleClass().add("icon-check");
            checkLabel.setText("✔");
            if (titleLabel != null) {
                titleLabel.getStyleClass().removeAll("condition-title-fail");
                titleLabel.getStyleClass().add("condition-title");
            }
        } else {
            card.getStyleClass().add("fail-card");
            checkLabel.getStyleClass().add("icon-cross");
            checkLabel.setText("✖");
            if (titleLabel != null) {
                titleLabel.getStyleClass().removeAll("condition-title");
                titleLabel.getStyleClass().add("condition-title-fail");
            }
        }
    }

    private void updateGrantBreakdown(com.ka4piece.model.GrantBreakdown breakdown) {
        if (breakdown == null) {
            if (lblHealthGrant != null) lblHealthGrant.setText("₱0.00");
            if (lblEducationGrant != null) lblEducationGrant.setText("₱0.00");
            if (lblRiceSubsidy != null) lblRiceSubsidy.setText("₱0.00");
            if (lblTotalGrant != null) lblTotalGrant.setText("₱0.00");
            if (boxNoticeCard != null) boxNoticeCard.setVisible(false);
            return;
        }

        if (lblHealthGrant != null) lblHealthGrant.setText(String.format("₱%,.2f", breakdown.getHealthGrantAmount()));
        if (lblEducationGrant != null) lblEducationGrant.setText(String.format("₱%,.2f", breakdown.getEducationGrantAmount()));
        if (lblRiceSubsidy != null) lblRiceSubsidy.setText(String.format("₱%,.2f", breakdown.getRiceSubsidyAmount()));
        if (lblTotalGrant != null) lblTotalGrant.setText(String.format("₱%,.2f", breakdown.getTotalAmount()));

        if (breakdown.getWithheldReasons() != null && !breakdown.getWithheldReasons().isEmpty()) {
            if (boxNoticeCard != null) boxNoticeCard.setVisible(true);
            if (lblNoticeText != null) {
                lblNoticeText.setText("Reason: " + String.join(", ", breakdown.getWithheldReasons()));
            }
        } else {
            if (boxNoticeCard != null) boxNoticeCard.setVisible(false);
        }
    }

    private void highlightSelectedRow(String monthYear) {
        if (boxGrantHistory == null) return;
        for (javafx.scene.Node child : boxGrantHistory.getChildren()) {
            if (child instanceof javafx.scene.layout.GridPane row) {
                row.getStyleClass().remove("selected-row");
                if (monthYear != null && monthYear.equals(row.getUserData())) {
                    row.getStyleClass().add("selected-row");
                }
            }
        }
    }

    // --- JOB MATCHES VIEW FLOW ---

    private void setupProfilesDropdown() {
        cmbProfiles.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(JobseekerProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMemberName() + " - " + item.getEducation());
                }
            }
        });
        cmbProfiles.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(JobseekerProfile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMemberName() + " - " + item.getEducation());
                }
            }
        });

        cmbProfiles.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedProfile = newVal;
                loadRankedVacancies(newVal);
                loadApplicationHistory(newVal);
            }
        });
    }

    private void loadProfilesDropdown() {
        if (cmbProfiles == null || jobMatchManager == null) return;
        String householdId = Session.getInstance().getUserId();
        if (householdId != null) {
            List<JobseekerProfile> profiles = jobMatchManager.getProfilesForHousehold(householdId);
            cmbProfiles.getItems().setAll(profiles);
            if (!profiles.isEmpty()) {
                cmbProfiles.getSelectionModel().select(0);
            }
        }
    }

    private void loadRankedVacancies(JobseekerProfile profile) {
        if (boxRankedVacancies == null || jobMatchManager == null) return;
        boxRankedVacancies.getChildren().clear();

        List<com.ka4piece.model.RankedVacancy> ranked = jobMatchManager.getRankedVacancies(profile.getJobseekerId());

        boolean isFirst = true;
        for (com.ka4piece.model.RankedVacancy rv : ranked) {
            Vacancy v = rv.getVacancy();
            double score = rv.getScore();

            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("job-row");
            row.setPadding(new Insets(12, 10, 12, 10));
            row.setStyle("-fx-cursor: hand;");

            VBox titleBox = new VBox();
            titleBox.setPrefWidth(150);
            Label lblTitle = new Label(v.getTitle());
            lblTitle.getStyleClass().add("job-row-title");
            Label lblCompany = new Label(v.getEnteredByOfficialId());
            lblCompany.getStyleClass().add("job-row-company");
            titleBox.getChildren().addAll(lblTitle, lblCompany);

            Label lblType = new Label(v.getType());
            lblType.setPrefWidth(90);
            lblType.getStyleClass().add("job-row-type");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label lblScore = new Label(String.format("%.0f%%", score * 100));
            lblScore.getStyleClass().add("match-badge");
            if (score >= 0.85) {
                lblScore.getStyleClass().add("badge-high");
            } else if (score >= 0.65) {
                lblScore.getStyleClass().add("badge-mid-high");
            } else if (score >= 0.45) {
                lblScore.getStyleClass().add("badge-mid");
            } else {
                lblScore.getStyleClass().add("badge-low");
            }

            row.getChildren().addAll(titleBox, lblType, spacer, lblScore);

            final HBox finalRow = row;
            row.setOnMouseClicked(e -> selectVacancyRow(finalRow, v, score));

            boxRankedVacancies.getChildren().add(row);

            if (isFirst) {
                selectVacancyRow(row, v, score);
                isFirst = false;
            }
        }

        if (isFirst) {
            clearDetailedJobView();
        }
    }

    private void selectVacancyRow(HBox row, Vacancy v, double score) {
        if (boxRankedVacancies != null) {
            for (Node child : boxRankedVacancies.getChildren()) {
                child.getStyleClass().remove("selected-job-row");
            }
        }
        row.getStyleClass().add("selected-job-row");

        selectedVacancy = v;

        if (lblJobDetailTitle != null) lblJobDetailTitle.setText(v.getTitle());
        if (lblJobDetailCompany != null) lblJobDetailCompany.setText(v.getEnteredByOfficialId());
        if (lblJobDetailMatch != null) lblJobDetailMatch.setText(String.format("%.0f%% Match", score * 100));
        if (lblJobDetailLocation != null) lblJobDetailLocation.setText("📍 " + v.getLocation());
        if (lblJobDetailCompensation != null) lblJobDetailCompensation.setText("💵 " + v.getCompensation());
        if (lblJobDetailType != null) lblJobDetailType.setText("🕒 " + v.getType());
        if (lblJobDetailDesc != null) lblJobDetailDesc.setText(v.getDescription());
        if (lblJobDetailEducation != null) lblJobDetailEducation.setText("• " + v.getEducationRequirement());
        if (lblJobDetailSkills != null) {
            lblJobDetailSkills.setText(v.getSkillRequirements() != null ? String.join(", ", v.getSkillRequirements()) : "None");
        }
        if (lblJobDetailExperience != null) {
            lblJobDetailExperience.setText("• " + v.getExperienceYearsRequired() + " year(s) required");
        }

        updateApplyButtonState();
    }

    private void clearDetailedJobView() {
        selectedVacancy = null;
        if (lblJobDetailTitle != null) lblJobDetailTitle.setText("Select a Job");
        if (lblJobDetailCompany != null) lblJobDetailCompany.setText("No job selected");
        if (lblJobDetailMatch != null) lblJobDetailMatch.setText("0% Match");
        if (lblJobDetailLocation != null) lblJobDetailLocation.setText("📍 Location");
        if (lblJobDetailCompensation != null) lblJobDetailCompensation.setText("💵 Compensation");
        if (lblJobDetailType != null) lblJobDetailType.setText("🕒 Type");
        if (lblJobDetailDesc != null) lblJobDetailDesc.setText("Select a vacancy from the list to view its details.");
        if (lblJobDetailEducation != null) lblJobDetailEducation.setText("No details available.");
        if (lblJobDetailSkills != null) lblJobDetailSkills.setText("No details available.");
        if (lblJobDetailExperience != null) lblJobDetailExperience.setText("No details available.");
        if (btnApplyNow != null) btnApplyNow.setDisable(true);
    }

    private void updateApplyButtonState() {
        if (btnApplyNow == null || selectedProfile == null || selectedVacancy == null) return;

        boolean alreadyApplied = selectedProfile.getAppliedVacancies() != null 
                && selectedProfile.getAppliedVacancies().containsKey(selectedVacancy.getVacancyId());

        if (alreadyApplied) {
            btnApplyNow.setText("APPLIED");
            btnApplyNow.setDisable(true);
        } else {
            btnApplyNow.setText("APPLY NOW");
            btnApplyNow.setDisable(false);
        }
    }

    private void loadApplicationHistory(JobseekerProfile profile) {
        if (boxApplicationHistory == null || jobMatchManager == null) return;
        boxApplicationHistory.getChildren().clear();

        Map<String, String> applications = profile.getAppliedVacancies();
        if (applications == null) return;

        for (Map.Entry<String, String> entry : applications.entrySet()) {
            String vacancyId = entry.getKey();
            String status = entry.getValue();

            Vacancy v = jobMatchManager.getVacancyDetail(vacancyId);
            if (v != null) {
                addApplicationToHistory(v, status);
            }
        }
    }

    private void addApplicationToHistory(Vacancy v, String status) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("job-row");
        row.setPadding(new Insets(12, 10, 12, 10));
        row.setStyle("-fx-cursor: hand;");

        VBox titleBox = new VBox();
        titleBox.setPrefWidth(150);

        Label lblTitle = new Label(v.getTitle() != null ? v.getTitle() : "Unnamed Job");
        lblTitle.getStyleClass().add("job-row-title");

        Label lblCompany = new Label(v.getEnteredByOfficialId() != null ? v.getEnteredByOfficialId() : "");
        lblCompany.getStyleClass().add("job-row-company");

        titleBox.getChildren().addAll(lblTitle, lblCompany);

        Label lblType = new Label(v.getType() != null ? v.getType() : "");
        lblType.setPrefWidth(90);
        lblType.getStyleClass().add("job-row-type");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatus = new Label(status);
        lblStatus.getStyleClass().add("match-badge");
        if ("HIRED".equalsIgnoreCase(status)) {
            lblStatus.getStyleClass().add("badge-high");
        } else if ("FOR_INTERVIEW".equalsIgnoreCase(status)) {
            lblStatus.getStyleClass().add("badge-mid-high");
        } else if ("APPLIED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) {
            lblStatus.getStyleClass().add("badge-mid");
        } else {
            lblStatus.getStyleClass().add("badge-low");
        }

        row.getChildren().addAll(titleBox, lblType, spacer, lblStatus);

        // Clicking a history row loads the vacancy details and adjusts the Apply button
        final HBox finalRow = row;
        row.setOnMouseClicked(e -> {
            // Deselect ranked vacancy highlights
            if (boxRankedVacancies != null) {
                for (Node child : boxRankedVacancies.getChildren()) {
                    child.getStyleClass().remove("selected-job-row");
                }
            }
            // Highlight this history row
            if (boxApplicationHistory != null) {
                for (Node child : boxApplicationHistory.getChildren()) {
                    child.getStyleClass().remove("selected-job-row");
                }
            }
            finalRow.getStyleClass().add("selected-job-row");
            selectVacancyFromHistory(v, status);
        });

        boxApplicationHistory.getChildren().add(row);
    }

    /**
     * Loads a vacancy into the job detail panel when selected from the application history.
     * The Apply Now button is disabled for any status other than REJECTED, since the
     * beneficiary is already in the pipeline for that vacancy.
     */
    private void selectVacancyFromHistory(Vacancy v, String status) {
        if (v == null) return;
        selectedVacancy = v;

        if (lblJobDetailTitle != null) lblJobDetailTitle.setText(v.getTitle());
        if (lblJobDetailCompany != null) lblJobDetailCompany.setText(v.getEnteredByOfficialId());
        // History rows have no match score — show current application status instead
        if (lblJobDetailMatch != null) lblJobDetailMatch.setText(status);
        if (lblJobDetailLocation != null) lblJobDetailLocation.setText("\uD83D\uDCCD " + (v.getLocation() != null ? v.getLocation() : "N/A"));
        if (lblJobDetailCompensation != null) lblJobDetailCompensation.setText("\uD83D\uDCB5 " + (v.getCompensation() != null ? v.getCompensation() : "N/A"));
        if (lblJobDetailType != null) lblJobDetailType.setText("\uD83D\uDD52 " + (v.getType() != null ? v.getType() : "N/A"));
        if (lblJobDetailDesc != null) lblJobDetailDesc.setText(v.getDescription() != null ? v.getDescription() : "No description provided.");
        if (lblJobDetailEducation != null) lblJobDetailEducation.setText("\u2022 " + (v.getEducationRequirement() != null ? v.getEducationRequirement() : "N/A"));
        if (lblJobDetailSkills != null) {
            lblJobDetailSkills.setText(v.getSkillRequirements() != null && !v.getSkillRequirements().isEmpty()
                    ? String.join(", ", v.getSkillRequirements()) : "None specified");
        }
        if (lblJobDetailExperience != null) {
            lblJobDetailExperience.setText("\u2022 " + v.getExperienceYearsRequired() + " year(s) required");
        }

        // Disable Apply Now for any status except REJECTED (user is already in the pipeline)
        if (btnApplyNow != null) {
            boolean alreadyActive = "HIRED".equalsIgnoreCase(status)
                    || "APPLIED".equalsIgnoreCase(status)
                    || "FOR_INTERVIEW".equalsIgnoreCase(status)
                    || "PENDING".equalsIgnoreCase(status);
            btnApplyNow.setDisable(alreadyActive);
            btnApplyNow.setText(alreadyActive ? status : "APPLY NOW");
        }
    }

    // --- APPLICATION LOGIC ---

    @FXML
    private void handleApplyNow(ActionEvent event) {
        if (btnApplyNow == null || selectedProfile == null || selectedVacancy == null || jobMatchManager == null) return;

        jobMatchManager.recordApplication(selectedProfile.getJobseekerId(), selectedVacancy.getVacancyId());

        String jobseekerId = selectedProfile.getJobseekerId();
        selectedProfile = jobMatchManager.getJobMatchRepository().findJobseekerById(jobseekerId);
        
        updateApplyButtonState();
        loadApplicationHistory(selectedProfile);
    }

    // --- NAVIGATION & MODAL HANDLERS ---

    @FXML
    private void goToCompliance(ActionEvent event) {
        App.switchScene(event, "/view/beneficiary_compliance.fxml");
    }

    @FXML
    private void goToJobMatches(ActionEvent event) {
        App.switchScene(event, "/view/beneficiary_job_matches.fxml");
    }

    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        App.switchScene(event, "/view/beneficiary_profile.fxml");
    }

    @FXML
    private void handleOpenAddProfileModal(ActionEvent event) {
        openModal(event, "/view/beneficiary_add_profile.fxml", "Add Jobseeker Profile");
        loadProfilesDropdown();
    }

    private void openModal(javafx.event.Event event, String path, String title) {
        try {
            Parent root = App.loadFXML(path);
            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (event != null && event.getSource() instanceof Node node) {
                stage.initOwner(node.getScene().getWindow());
            }
            stage.setTitle(title);
            stage.setScene(new javafx.scene.Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            Parent root = App.loadFXML("/view/logout_modal.fxml");

            // Fallback load if modal is directly in resources root
            if (root == null) {
                root = App.loadFXML("/logout_modal.fxml");
            }

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);

            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            modalStage.initOwner(currentStage);
            modalStage.setTitle("Confirm Logout");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);

            modalStage.showAndWait();

            // Perform logout only if user confirmed in modal
            if (LogoutModalController.isConfirmed()) {
                if (Session.getInstance() != null) {
                    Session.getInstance().clearSession();
                }
                App.switchScene(event, "/view/login.fxml");
            }
        } catch (Exception e) {
            // Direct fallback if modal loading fails
            if (Session.getInstance() != null) {
                Session.getInstance().clearSession();
            }
            App.switchScene(event, "/view/login.fxml");
        }
    }
}