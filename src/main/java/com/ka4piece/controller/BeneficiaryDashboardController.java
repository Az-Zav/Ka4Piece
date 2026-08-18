package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.repository.AuthRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class BeneficiaryDashboardController {
    private AuthRepository authRepository;
    private ComplianceManager complianceManager;

    public BeneficiaryDashboardController(AuthRepository authRepository, ComplianceManager complianceManager) {
        this.authRepository = authRepository;
        this.complianceManager = complianceManager;
    }

    public BeneficiaryDashboardController() {
    }

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
    private void handleLogout(ActionEvent event) {
        if (com.ka4piece.model.Session.getInstance() != null) {
            com.ka4piece.model.Session.getInstance().clearSession();
        }
        App.switchScene(event, "/view/login.fxml");
    }
}
