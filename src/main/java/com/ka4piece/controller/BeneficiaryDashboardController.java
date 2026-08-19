package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.util.NavigationUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class BeneficiaryDashboardController {
    private AuthRepository authRepository;
    private ComplianceManager complianceManager;

    // FXML Control Injections
    @FXML private Button btnApplyNow;
    @FXML private VBox boxApplicationHistory;
    @FXML private Label lblJobDetailTitle;
    @FXML private Label lblJobDetailCompany;

    public BeneficiaryDashboardController(AuthRepository authRepository, ComplianceManager complianceManager) {
        this.authRepository = authRepository;
        this.complianceManager = complianceManager;
    }

    public BeneficiaryDashboardController() {
    }

    // --- APPLICATION LOGIC ---

    @FXML
    private void handleApplyNow(ActionEvent event) {
        if (btnApplyNow == null) return;

        // Retrieve current selected job text dynamically or fallback
        String jobTitle = (lblJobDetailTitle != null) ? lblJobDetailTitle.getText() : "Warehouse Assistant";
        String companyName = (lblJobDetailCompany != null) ? lblJobDetailCompany.getText() : "Logistics Corp Inc.";

        // 1. Update Apply Button state
        btnApplyNow.setText("APPLIED");
        btnApplyNow.setDisable(true);

        // 2. Add entry dynamically to History of Application card
        if (boxApplicationHistory != null) {
            addApplicationToHistory(jobTitle, companyName, "Full-Time", "Pending");
        }
    }

    private void addApplicationToHistory(String title, String company, String type, String status) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("job-row");
        row.setPadding(new Insets(12, 10, 12, 10));

        // Job Title & Company
        VBox titleBox = new VBox();
        titleBox.setPrefWidth(150);

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("job-row-title");

        Label lblCompany = new Label(company);
        lblCompany.getStyleClass().add("job-row-company");

        titleBox.getChildren().addAll(lblTitle, lblCompany);

        // Job Type
        Label lblType = new Label(type);
        lblType.setPrefWidth(90);
        lblType.getStyleClass().add("job-row-type");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Application Status Badge
        Label lblStatus = new Label(status);
        lblStatus.getStyleClass().addAll("match-badge", "badge-high");

        row.getChildren().addAll(titleBox, lblType, spacer, lblStatus);

        // Append to history list
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
        NavigationUtils.showLogoutModal(event);
    }
}