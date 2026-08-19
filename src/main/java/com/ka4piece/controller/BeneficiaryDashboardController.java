package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.repository.AuthRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class BeneficiaryDashboardController {
    private AuthRepository authRepository;
    private ComplianceManager complianceManager;

    // FXML Control Injections
    @FXML private Button btnBackToCurrentMonth;
    @FXML private Button btnApplyNow;
    @FXML private VBox boxApplicationHistory;
    @FXML private Label lblJobDetailTitle;
    @FXML private Label lblJobDetailCompany;

    // History Rows
    @FXML private GridPane rowAug2026;
    @FXML private GridPane rowJul2026;
    @FXML private GridPane rowJun2026;

    // Grant Breakdown Dynamic Labels
    @FXML private Label lblHealthGrant;
    @FXML private Label lblEducationGrant;
    @FXML private Label lblRiceSubsidy;
    @FXML private Label lblTotalGrant;

    // Notice Box Controls
    @FXML private VBox boxNoticeCard;
    @FXML private Label lblNoticeText;

    public BeneficiaryDashboardController(AuthRepository authRepository, ComplianceManager complianceManager) {
        this.authRepository = authRepository;
        this.complianceManager = complianceManager;
    }

    public BeneficiaryDashboardController() {
    }

    @FXML
    public void initialize() {
        // Wire history row click handlers
        if (rowAug2026 != null) rowAug2026.setOnMouseClicked(e -> selectMonth("AUG_2026"));
        if (rowJul2026 != null) rowJul2026.setOnMouseClicked(e -> selectMonth("JUL_2026"));
        if (rowJun2026 != null) rowJun2026.setOnMouseClicked(e -> selectMonth("JUN_2026"));

        // "Back to Current Month" click handler
        if (btnBackToCurrentMonth != null) {
            btnBackToCurrentMonth.setOnAction(e -> selectMonth("AUG_2026"));
        }
    }

    // --- ROW SELECTION UX FLOW ---

    private void selectMonth(String monthKey) {
        if ("AUG_2026".equals(monthKey)) {
            // CURRENT MONTH
            if (btnBackToCurrentMonth != null) btnBackToCurrentMonth.setVisible(false);
            loadAugustData();
        } else {
            // PAST MONTHS
            if (btnBackToCurrentMonth != null) btnBackToCurrentMonth.setVisible(true);
            if ("JUL_2026".equals(monthKey)) {
                loadJulyData();
            } else if ("JUN_2026".equals(monthKey)) {
                loadJuneData();
            }
        }

        highlightSelectedRow(monthKey);
    }

    private void loadAugustData() {
        if (lblHealthGrant != null) lblHealthGrant.setText("₱750.00");
        if (lblEducationGrant != null) lblEducationGrant.setText("₱1,000.00");
        if (lblRiceSubsidy != null) lblRiceSubsidy.setText("₱600.00");
        if (lblTotalGrant != null) lblTotalGrant.setText("₱2,350.00");

        if (boxNoticeCard != null) boxNoticeCard.setVisible(true);
        if (lblNoticeText != null) {
            lblNoticeText.setText("Reason: Non-compliance with Family Development Session (FDS) attendance for August 2026. A portion of the grant associated with FDS compliance (₱300.00) has been temporarily withheld pending review or make-up session.");
        }
    }

    private void loadJulyData() {
        if (lblHealthGrant != null) lblHealthGrant.setText("₱750.00");
        if (lblEducationGrant != null) lblEducationGrant.setText("₱850.00");
        if (lblRiceSubsidy != null) lblRiceSubsidy.setText("₱600.00");
        if (lblTotalGrant != null) lblTotalGrant.setText("₱2,200.00");

        if (boxNoticeCard != null) boxNoticeCard.setVisible(false);
    }

    private void loadJuneData() {
        if (lblHealthGrant != null) lblHealthGrant.setText("₱1,000.00");
        if (lblEducationGrant != null) lblEducationGrant.setText("₱1,400.00");
        if (lblRiceSubsidy != null) lblRiceSubsidy.setText("₱600.00");
        if (lblTotalGrant != null) lblTotalGrant.setText("₱3,000.00");

        if (boxNoticeCard != null) boxNoticeCard.setVisible(false);
    }

    private void highlightSelectedRow(String monthKey) {
        if (rowAug2026 != null) rowAug2026.getStyleClass().remove("selected-row");
        if (rowJul2026 != null) rowJul2026.getStyleClass().remove("selected-row");
        if (rowJun2026 != null) rowJun2026.getStyleClass().remove("selected-row");

        switch (monthKey) {
            case "AUG_2026" -> { if (rowAug2026 != null) rowAug2026.getStyleClass().add("selected-row"); }
            case "JUL_2026" -> { if (rowJul2026 != null) rowJul2026.getStyleClass().add("selected-row"); }
            case "JUN_2026" -> { if (rowJun2026 != null) rowJun2026.getStyleClass().add("selected-row"); }
        }
    }

    // --- APPLICATION LOGIC ---

    @FXML
    private void handleApplyNow(ActionEvent event) {
        if (btnApplyNow == null) return;

        String jobTitle = (lblJobDetailTitle != null) ? lblJobDetailTitle.getText() : "Warehouse Assistant";
        String companyName = (lblJobDetailCompany != null) ? lblJobDetailCompany.getText() : "Logistics Corp Inc.";

        btnApplyNow.setText("APPLIED");
        btnApplyNow.setDisable(true);

        if (boxApplicationHistory != null) {
            addApplicationToHistory(jobTitle, companyName, "Full-Time", "Pending");
        }
    }

    private void addApplicationToHistory(String title, String company, String type, String status) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("job-row");
        row.setPadding(new Insets(12, 10, 12, 10));

        VBox titleBox = new VBox();
        titleBox.setPrefWidth(150);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("job-row-title");

        Label lblCompany = new Label(company);
        lblCompany.getStyleClass().add("job-row-company");

        titleBox.getChildren().addAll(lblTitle, lblCompany);

        Label lblType = new Label(type);
        lblType.setPrefWidth(90);
        lblType.getStyleClass().add("job-row-type");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblStatus = new Label(status);
        lblStatus.getStyleClass().addAll("match-badge", "badge-high");

        row.getChildren().addAll(titleBox, lblType, spacer, lblStatus);

        boxApplicationHistory.getChildren().add(row);
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
        // Modal hook for profile creation
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        App.switchScene(event, "/view/login.fxml");
    }
}