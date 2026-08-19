package com.ka4piece.controller;

import com.ka4piece.model.Session;
import com.ka4piece.manager.AuthManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class ChangePasswordController {

    @FXML private PasswordField txtCurrentPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Label lblDialogMessage;

    private final AuthManager authManager;
    private final Session activeSession;

    public ChangePasswordController(AuthManager authManager) {
        this.authManager = authManager;
        this.activeSession = Session.getInstance();
    }

    @FXML
    public void initialize() {
        // Dependencies are injected via the constructor
    }

    @FXML
    private void handleSavePassword(ActionEvent event) {
        String currentPassword = txtCurrentPassword.getText().trim();
        String newPassword = txtNewPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();

        // 1. Validation Checks
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showMessage("All password fields are required.", true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("New password and confirm password do not match.", true);
            return;
        }

        if (newPassword.length() < 6) {
            showMessage("New password must be at least 6 characters long.", true);
            return;
        }

        if (activeSession == null || activeSession.getUserId() == null) {
            showMessage("Session expired. Please log in again.", true);
            return;
        }

        String userId = activeSession.getUserId();
        String role = activeSession.getRole();

        boolean isCurrentPasswordCorrect = authManager.verifyCurrentPassword(userId, currentPassword, activeSession);
        if (!isCurrentPasswordCorrect) {
            showMessage("Incorrect current password.", true);
            return;
        }

        boolean isUpdated = authManager.changePassword(userId, role, newPassword);

        if (isUpdated) {
            closeModal(event);
        } else {
            showMessage("Failed to update password. Please try again.", true);
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeModal(event);
    }

    private void closeModal(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void showMessage(String msg, boolean isError) {
        if (lblDialogMessage != null) {
            lblDialogMessage.setText(msg);
            lblDialogMessage.setTextFill(isError ? Color.web("#DC2626") : Color.web("#16A34A"));
            lblDialogMessage.setVisible(true);
            lblDialogMessage.setManaged(true);
        }
    }
}
