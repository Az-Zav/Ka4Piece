package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.manager.AuthManager;
import com.ka4piece.manager.ComplianceManager;
import com.ka4piece.manager.JobMatchManager;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class OfficialDashboardController {

    private AuthManager authManager;
    private ComplianceManager complianceManager;
    private JobMatchManager jobMatchManager;

    public OfficialDashboardController(AuthManager authManager, ComplianceManager complianceManager, JobMatchManager jobMatchManager) {
        this.authManager = authManager;
        this.complianceManager = complianceManager;
        this.jobMatchManager = jobMatchManager;
    }

    public OfficialDashboardController() {
    }

    // --- MAIN DASHBOARD CONTROLS ---
    @FXML private Hyperlink linkAddHousehold;
    @FXML private TextField txtChildrenCount;
    @FXML private Button btnDecrement;
    @FXML private Button btnIncrement;

    // --- TABLE CONTROLS ---
    @FXML private TableView<?> tblHistory;
    @FXML private TableColumn<?, ?> colMonth;
    @FXML private TableColumn<?, ?> colHealth;
    @FXML private TableColumn<?, ?> colEducation;
    @FXML private TableColumn<?, ?> colFds;
    @FXML private TableColumn<?, ?> colStatus;

    // --- MODAL CONTROLS (Fallback if modal references this controller) ---
    @FXML private TextField txtHeadName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnRegister;
    @FXML private Label lblSuccessMessage;

    // --- SEARCH CONTROLS ---
    @FXML private TextField txtSearch;

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

    // --- NAVBAR & PROFILE EVENT HANDLERS ---

    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        switchSceneFromButton(event, "/view_profile.fxml");
    }

    // --- TAB NAVIGATION HANDLERS ---

    @FXML
    private void goToCompliance(MouseEvent event) {
        switchSceneFromMouse(event, "/compliance.fxml");
    }

    @FXML
    private void goToJobVacancies(MouseEvent event) {
        switchSceneFromMouse(event, "/job_vacancies.fxml");
    }

    // --- MODAL DIALOG HANDLERS ---

    @FXML
    private void openAddHouseholdModal(ActionEvent event) {
        try {
            Parent root = App.loadFXML("/add_household.fxml");
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
    }

    // --- SEARCH METHODS --- 

    @FXML
    private void handleSearch(ActionEvent event) {

    }


    // --- STEPPER HANDLERS ---

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

    // --- HELPER ROUTING METHODS ---

    private void switchSceneFromMouse(MouseEvent event, String fxmlPath) {
        App.switchScene(event, fxmlPath);
    }

    private void switchSceneFromButton(ActionEvent event, String fxmlPath) {
        App.switchScene(event, fxmlPath);
    }

    private URL resolveResource(String fxmlPath) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            resource = getClass().getResource("/view" + fxmlPath);
        }
        return resource;
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