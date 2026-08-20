package com.ka4piece.controller;

import com.ka4piece.app.App;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.repository.DbConfig;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
public class BeneficiaryProfileController {

    // --- GENERAL CONTROLS ---
    @FXML private Button btnToggleEdit;
    @FXML private Button btnChangePassword;
    @FXML private TextField txtUserId;
    @FXML private Label lblFullName;
    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private Label lblMessage;

    // --- HOUSEHOLD / BENEFICIARY FIELDS ---
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;

    // --- CONTAINER PANELS ---
    @FXML private HBox boxEditActions;

    private boolean isEditMode = false;
    private Session activeSession;
    private AuthRepository authRepository;

    public BeneficiaryProfileController(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public BeneficiaryProfileController() {
    }

    @FXML
    public void initialize() {
        if (txtUserId != null) {
            txtUserId.setEditable(false);
        }

        // Load DB config as fallback to prevent null repository crashes
        if (authRepository == null) {
            try {
                DbConfig config = new DbConfig("db.properties");
                authRepository = new AuthRepository(config.getJdbcurl(), config.getUsername(), config.getPassword());
            } catch (Exception e) {
                System.err.println("Could not load db.properties: " + e.getMessage());
            }
        }

        activeSession = Session.getInstance();

        autoPopulateProfileData();
        setEditMode(false);
    }

    private void autoPopulateProfileData() {
        if (activeSession == null || activeSession.getUserId() == null) {
            if (txtUserId != null) txtUserId.setText("N/A");
            if (txtName != null) txtName.setText("Guest Beneficiary");
            if (txtEmail != null) txtEmail.setText("guest@ka4piece.com");
            return;
        }

        if (lblFullName != null) {
            lblFullName.setText("HEAD OF HOUSEHOLD NAME");
        }

        Household household = authRepository != null
                ? authRepository.findHouseholdById(activeSession.getUserId())
                : null;

        if (household != null) {
            if (txtUserId != null) txtUserId.setText(household.getHouseholdId());
            if (txtName != null) txtName.setText(household.getHeadName());
            if (txtEmail != null) txtEmail.setText(household.getEmail());
            if (txtAddress != null) txtAddress.setText(household.getAddress());
            if (txtBarangay != null) txtBarangay.setText(household.getBarangay());
        } else {
            if (txtUserId != null) txtUserId.setText(activeSession.getUserId());
            if (txtName != null) txtName.setText(activeSession.getDisplayName());
        }
    }

    private void setEditMode(boolean enable) {
        isEditMode = enable;

        if (txtUserId != null) txtUserId.setEditable(false);

        if (txtName != null) txtName.setEditable(enable);
        if (txtEmail != null) txtEmail.setEditable(enable);
        if (txtAddress != null) txtAddress.setEditable(enable);
        if (txtBarangay != null) txtBarangay.setEditable(enable);

        if (boxEditActions != null) {
            boxEditActions.setVisible(enable);
            boxEditActions.setManaged(enable);
        }

        if (btnToggleEdit != null) {
            btnToggleEdit.setVisible(!enable);
            btnToggleEdit.setManaged(!enable);
        }

        if (btnChangePassword != null) {
            btnChangePassword.setVisible(!enable);
            btnChangePassword.setManaged(!enable);
        }
    }

    @FXML
    private void handleToggleEditMode(ActionEvent event) {
        setEditMode(true);
        showMessage("", false);
    }

    @FXML
    private void handleCancelEdit(ActionEvent event) {
        autoPopulateProfileData();
        setEditMode(false);
        showMessage("Changes cancelled.", false);
    }

    @FXML
    private void handleSaveProfile(ActionEvent event) {
        String newName = txtName != null ? txtName.getText().trim() : "";
        String newEmail = txtEmail != null ? txtEmail.getText().trim() : "";

        if (newName.isEmpty()) {
            showMessage("Name cannot be empty.", true);
            return;
        }

        String userId = (activeSession != null) ? activeSession.getUserId() : null;

        if (userId != null && authRepository != null) {
            try {
                activeSession.setDisplayName(newName);

                String address = txtAddress != null ? txtAddress.getText().trim() : "";
                String barangay = txtBarangay != null ? txtBarangay.getText().trim() : "";
                authRepository.updateHouseholdProfile(userId, newName, address, barangay, newEmail);
                showMessage("Profile updated successfully!", false);
            } catch (Exception e) {
                e.printStackTrace();
                showMessage("Failed to save profile: " + e.getMessage(), true);
                return;
            }
        } else {
            if (activeSession != null) {
                activeSession.setDisplayName(newName);
            }
            showMessage("Guest profile updated locally.", false);
        }

        setEditMode(false);
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        openModal(event, "/view/change_password_dialog.fxml", "Change Password");
    }

    // --- NAVIGATION HANDLERS ---

    @FXML
    private void handleOpenViewProfile(Event event) {
        App.switchScene(event, "/view/beneficiary_profile.fxml");
    }

    @FXML
    private void goToCompliance(Event event) {
        App.switchScene(event, "/view/beneficiary_compliance.fxml");
    }

    @FXML
    private void goToJobMatches(Event event) {
        App.switchScene(event, "/view/beneficiary_job_matches.fxml");
    }

    @FXML
    private void goToComplianceMouse(MouseEvent event) {
        App.switchScene(event, "/view/beneficiary_compliance.fxml");
    }

    @FXML
    private void goToJobMatchesMouse(MouseEvent event) {
        App.switchScene(event, "/view/beneficiary_job_matches.fxml");
    }

    @FXML
    private void handleLogout(Event event) {
        NavigationUtils.showLogoutModal(event);
    }

    // --- HELPER METHODS ---

    private void openModal(Event event, String path, String title) {
        try {
            Parent root = App.loadFXML(path);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            if (event != null && event.getSource() instanceof Node node) {
                stage.initOwner(node.getScene().getWindow());
            }
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error opening modal window.", true);
        }
    }

    private void showMessage(String msg, boolean isError) {
        if (lblMessage != null) {
            if (msg == null || msg.isEmpty()) {
                lblMessage.setVisible(false);
                lblMessage.setManaged(false);
            } else {
                lblMessage.setText(msg);
                lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
                lblMessage.setVisible(true);
                lblMessage.setManaged(true);
            }
        }
    }
}