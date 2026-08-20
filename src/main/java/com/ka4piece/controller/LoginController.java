package com.ka4piece.controller;

import com.ka4piece.model.BarangayOfficial;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.utilities.PasswordUtil;
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
    private AuthRepository authRepository;

    public LoginController(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LoginController() {
    }

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
            passwordHidden.setText(passwordVisible.getText());
            passwordHidden.setVisible(true);
            passwordHidden.setManaged(true);

            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);

            eyeBtn.setText("👁");
            isPasswordShowing = false;
        } else {
            passwordVisible.setText(passwordHidden.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);

            passwordHidden.setVisible(false);
            passwordHidden.setManaged(false);

            eyeBtn.setText("🙈");
            isPasswordShowing = true;
        }
    }

    private String getPassword() {
        return isPasswordShowing ? passwordVisible.getText() : passwordHidden.getText();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String emailOrUser = usernameField.getText().trim();
        String password = getPassword();

        ToggleButton selectedToggle = (ToggleButton) userTypeGroup.getSelectedToggle();
        if (selectedToggle == null) {
            showError("Please select a user type (OFFICIAL or BENEFICIARY).");
            return;
        }

        String userType = selectedToggle.getText().toUpperCase(); // "OFFICIAL" or "BENEFICIARY"

        if (emailOrUser.isEmpty() || password.isEmpty()) {
            showError("* Email/Username and Password are required.");
            return;
        }

        hideError();

        // Authenticate user with database
        boolean loginSuccess = performAuthentication(emailOrUser, password, userType);

        if (!loginSuccess) {
            showError("Invalid email/username or password.");
        } else {
            navigateToMainApp(event, userType);
        }
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        NavigationUtils.showForgotPasswordModal(event);
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void hideError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }

    /**
     * Performs database authentication check and configures the active Session singleton.
     */
    private boolean performAuthentication(String identifier, String password, String userType) {
        if (authRepository == null) return false;

        if (userType.equals("OFFICIAL")) {
            BarangayOfficial official = authRepository.findOfficialById(identifier);
            if (official == null) {
                official = authRepository.findOfficialByEmail(identifier);
            }

            if (official != null && PasswordUtil.verifyPassword(password, official.getPassword())) {
                Session session = Session.getInstance();
                session.setUserId(official.getOfficialId());
                session.setRole("OFFICIAL");
                session.setDisplayName(official.getName());
                session.setAdmin(official.isAdmin());
                return true;
            }
        } else {
            // BENEFICIARY / HOUSEHOLD
            Household household = authRepository.findHouseholdById(identifier);
            if (household == null) {
                household = authRepository.findHouseholdByEmail(identifier);
            }

            if (household != null && PasswordUtil.verifyPassword(password, household.getPassword())) {
                Session session = Session.getInstance();
                session.setUserId(household.getHouseholdId());
                session.setRole("HOUSEHOLD");
                session.setDisplayName(household.getHeadName());
                session.setAdmin(false);
                return true;
            }
        }

        return false;
    }

    /**
     * Routes to the appropriate dashboard scene based on user type.
     */
    private void navigateToMainApp(ActionEvent event, String userType) {
        if (userType.equals("OFFICIAL")) {
            NavigationUtils.switchSceneFromButton(event, "/view/official_compliance.fxml");
        } else {
            NavigationUtils.switchSceneFromButton(event, "/view/beneficiary_compliance.fxml");
        }
    }

}