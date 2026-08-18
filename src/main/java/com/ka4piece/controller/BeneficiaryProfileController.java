package com.ka4piece.controller;

import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.repository.AuthRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

import java.io.IOException;
import java.net.URL;

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

    @FXML
    public void initialize() {
        if (txtUserId != null) {
            txtUserId.setEditable(false);
        }

        authRepository = new AuthRepository("jdbc:mysql://localhost:3306/ka4piece", "root", "");

        // Fetch active global session instance
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
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        try {
            URL resource = resolveResource("/beneficiary_change_password_dialog.fxml");
            if (resource == null) {
                showMessage("Password dialog FXML file not found.", true);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Parent modalRoot = loader.load();

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(((Node) event.getSource()).getScene().getWindow());
            modalStage.setTitle("Change Password");
            modalStage.setScene(new Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showMessage("Error opening change password window.", true);
        }
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

        if (activeSession != null && authRepository != null) {
            activeSession.setDisplayName(newName);
            String userId = activeSession.getUserId();

            // Update Household/Beneficiary Profile
            String address = txtAddress != null ? txtAddress.getText().trim() : "";
            String barangay = txtBarangay != null ? txtBarangay.getText().trim() : "";
            authRepository.updateHouseholdProfile(userId, newName, address, barangay, newEmail);
        }

        setEditMode(false);
        showMessage("Profile updated successfully!", false);
    }

    // --- NAVIGATION HANDLERS ---

    @FXML
    private void handleOpenViewProfile(ActionEvent event) {
        switchSceneFromButton(event, "/beneficiary_profile.fxml");
    }

    @FXML
    private void goToCompliance(ActionEvent event) {
        switchSceneFromButton(event, "/beneficiary_compliance.fxml");
    }

    @FXML
    private void goToJobMatches(ActionEvent event) {
        switchSceneFromButton(event, "/beneficiary_job_matches.fxml");
    }

    @FXML
    private void goToComplianceMouse(MouseEvent event) {
        switchSceneFromMouse(event, "/beneficiary_compliance.fxml");
    }

    @FXML
    private void goToJobMatchesMouse(MouseEvent event) {
        switchSceneFromMouse(event, "/beneficiary_job_matches.fxml");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        if (Session.getInstance() != null) {
            Session.getInstance().clearSession();
        }
        switchSceneFromButton(event, "/login.fxml");
    }

    // --- ROUTING HELPERS ---

    private void switchSceneFromButton(ActionEvent event, String fxmlPath) {
        try {
            URL resource = resolveResource(fxmlPath);
            if (resource == null) return;

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchSceneFromMouse(MouseEvent event, String fxmlPath) {
        try {
            URL resource = resolveResource(fxmlPath);
            if (resource == null) return;

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private URL resolveResource(String fxmlPath) {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) {
            resource = getClass().getResource("/view" + fxmlPath);
        }
        return resource;
    }

    private void showMessage(String msg, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setTextFill(isError ? Color.RED : Color.GREEN);
            lblMessage.setVisible(true);
        }
    }
}