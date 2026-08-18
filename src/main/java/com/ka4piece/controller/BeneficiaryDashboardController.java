package com.ka4piece.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class BeneficiaryDashboardController {

    @FXML
    private Label lblHouseholdName;

    @FXML
    private Label lblHouseholdId;

    @FXML
    private ComboBox<String> cmbAssessmentPeriod;

    @FXML
    public void initialize() {
        // Populate assessment period combo box options if present
        if (cmbAssessmentPeriod != null) {
            cmbAssessmentPeriod.getItems().addAll(
                    "August 2026",
                    "July 2026",
                    "June 2026"
            );
            cmbAssessmentPeriod.getSelectionModel().selectFirst();
        }
    }

    /**
     * Handles navigation to the Compliance view.
     */
    @FXML
    private void goToCompliance(ActionEvent event) {
        switchScene(event, "/view/beneficiary_compliance.fxml");
    }

    /**
     * Handles navigation to the Job Matches / Vacancies view.
     */
    @FXML
    private void goToJobMatches(ActionEvent event) {
        switchScene(event, "/view/beneficiary_job_matches.fxml");
    }

    /**
     * Handles opening the beneficiary's profile screen.
     */
    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        // FIXED: Point to beneficiary_profile.fxml instead of official_profile.fxml
        switchScene(event, "/view/beneficiary_profile.fxml");
    }

    /**
     * Handles opening the Add Jobseeker Profile modal dialog.
     */
    @FXML
    private void handleOpenAddProfileModal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/beneficiary_add_profile.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            Scene scene = new Scene(root);
            scene.setFill(null); // Allows rounded borders without square background box

            modalStage.setScene(scene);
            modalStage.centerOnScreen();
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open Add Jobseeker Profile modal.");
        }
    }

    /**
     * Handles logging out and returning to the login view.
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        switchScene(event, "/view/login.fxml");
    }

    /**
     * Reusable helper method to handle scene switching across tabs and views.
     */
    private void switchScene(ActionEvent event, String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("Could not find FXML file: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Retain existing scene size if available
            Scene currentScene = stage.getScene();
            if (currentScene != null) {
                stage.setScene(new Scene(root, currentScene.getWidth(), currentScene.getHeight()));
            } else {
                stage.setScene(new Scene(root));
            }

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load view: " + fxmlPath);
        }
    }
}