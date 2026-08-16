package com.ka4piece.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class OfficialDashboardController {

    // --- MAIN DASHBOARD CONTROLS ---
    @FXML private Hyperlink linkAddHousehold;
    @FXML private TextField txtChildrenCount;
    @FXML private Button btnDecrement;
    @FXML private Button btnIncrement;

    // --- MODAL CONTROLS (Mapped if add_household.fxml uses this controller) ---
    @FXML private TextField txtHeadName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnRegister;
    @FXML private Label lblSuccessMessage;

    private final int MIN_CHILDREN = 0;
    private final int MAX_CHILDREN = 3;

    @FXML
    public void initialize() {
        if (txtChildrenCount != null) {
            txtChildrenCount.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    txtChildrenCount.setText(newValue.replaceAll("[^\\d]", ""));
                }
            });
        }
    }

    // --- MODAL DIALOG HANDLERS ---

    @FXML
    private void openAddHouseholdModal(ActionEvent event) {
        try {
            String fxmlPath = "/add_household.fxml";
            URL resource = getClass().getResource(fxmlPath);

            // Fallback for setups using a /view/ resource directory structure
            if (resource == null) {
                resource = getClass().getResource("/view" + fxmlPath);
            }

            if (resource == null) {
                System.err.println("Could not locate modal resource file: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);

            if (linkAddHousehold != null && linkAddHousehold.getScene() != null) {
                modalStage.initOwner(linkAddHousehold.getScene().getWindow());
            }

            modalStage.setTitle("Register Household");
            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        if (lblSuccessMessage != null) {
            lblSuccessMessage.setVisible(true);
        }
        // TODO: Insert backend database save logic here
    }

    // --- TAB NAVIGATION HANDLERS ---

    @FXML
    private void goToCompliance(MouseEvent event) {
        switchTab(event, "/compliance.fxml");
    }

    @FXML
    private void goToJobVacancies(MouseEvent event) {
        switchTab(event, "/job_vacancies.fxml");
    }

    private void switchTab(MouseEvent event, String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);

            if (resource == null) {
                resource = getClass().getResource("/view" + fxmlPath);
            }

            if (resource == null) {
                System.err.println("Could not locate resource file: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new Scene(root));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- COMPLIANCE STEPPER HANDLERS ---

    @FXML
    private void handleIncrement() {
        if (txtChildrenCount == null) return;
        int currentCount = getCurrentCount();
        if (currentCount < MAX_CHILDREN) {
            txtChildrenCount.setText(String.valueOf(currentCount + 1));
        }
    }

    @FXML
    private void handleDecrement() {
        if (txtChildrenCount == null) return;
        int currentCount = getCurrentCount();
        if (currentCount > MIN_CHILDREN) {
            txtChildrenCount.setText(String.valueOf(currentCount - 1));
        }
    }

    private int getCurrentCount() {
        if (txtChildrenCount == null || txtChildrenCount.getText() == null) {
            return 0;
        }
        try {
            return Integer.parseInt(txtChildrenCount.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}