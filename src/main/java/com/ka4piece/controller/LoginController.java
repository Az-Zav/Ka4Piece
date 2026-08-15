package com.ka4piece.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class LoginController {

    // Root Pane
    @FXML private StackPane rootPane;

    // Header Controls
    @FXML private ImageView logoImageView;

    // Form Input Controls
    @FXML private ToggleGroup userTypeGroup;
    @FXML private ToggleButton officialToggle;
    @FXML private ToggleButton beneficiaryToggle;
    @FXML private TextField usernameField;

    // Password Toggle Controls
    @FXML private PasswordField passwordHidden;
    @FXML private TextField passwordVisible;
    @FXML private Button eyeBtn;

    // Links & Status Controls
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private boolean isPasswordShowing = false;

    @FXML
    public void initialize() {
        // Hide error message initially
        hideError();

        // Default selection for user type group
        if (userTypeGroup != null && userTypeGroup.getSelectedToggle() == null) {
            if (officialToggle != null) {
                officialToggle.setSelected(true);
            }
        }
    }

    /**
     * Toggles the password field between masked (PasswordField) and visible text (TextField).
     */
    @FXML
    private void togglePassword(ActionEvent event) {
        if (isPasswordShowing) {
            // Switch back to hidden PasswordField
            passwordHidden.setText(passwordVisible.getText());
            passwordHidden.setVisible(true);
            passwordHidden.setManaged(true);

            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);

            eyeBtn.setText("👁");
            isPasswordShowing = false;
        } else {
            // Switch to visible TextField
            passwordVisible.setText(passwordHidden.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);

            passwordHidden.setVisible(false);
            passwordHidden.setManaged(false);

            eyeBtn.setText("🙈");
            isPasswordShowing = true;
        }
    }

    /**
     * Helper method to get the active password text regardless of visibility state.
     */
    private String getPassword() {
        return isPasswordShowing ? passwordVisible.getText() : passwordHidden.getText();
    }

    /**
     * Handles the Login button action.
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = getPassword();

        ToggleButton selectedToggle = (ToggleButton) userTypeGroup.getSelectedToggle();
        if (selectedToggle == null) {
            showError("Please select a user type (OFFICIAL or BENEFICIARY).");
            return;
        }

        String userType = selectedToggle.getText(); // "OFFICIAL" or "BENEFICIARY"

        // Basic Validation Check
        if (username.isEmpty() || password.isEmpty()) {
            showError("* Username and Password are required.");
            return;
        }

        // Reset error display on valid attempt
        hideError();

        System.out.println("Attempting login for Role: " + userType + " | Username: " + username);

        boolean loginSuccess = performAuthentication(username, password, userType);

        if (!loginSuccess) {
            showError("Invalid username or password.");
        } else {
            navigateToMainApp(userType);
        }
    }

    /**
     * Handles Forgot Password link click.
     */
    @FXML
    private void handleForgotPassword(ActionEvent event) {
        System.out.println("Redirecting to Forgot Password flow...");
        // TODO: Load Forgot Password FXML scene or open password recovery dialog
    }

    /**
     * Displays error messages below inputs.
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    /**
     * Hides the error message label.
     */
    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Placeholder method for backend credential checks.
     */
    private boolean performAuthentication(String username, String password, String userType) {
        // TODO: Connect with AuthManager/AuthRepository
        return true;
    }

    /**
     * Placeholder method for switching views after successful login.
     */
    private void navigateToMainApp(String userType) {
        // TODO: Implement scene switching logic using App/FXMLLoader
        System.out.println("Login successful. Navigating to dashboard for: " + userType);
    }
}