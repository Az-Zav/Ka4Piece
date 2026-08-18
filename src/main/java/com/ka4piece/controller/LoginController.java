package com.ka4piece.controller;

import com.ka4piece.model.BarangayOfficial;
import com.ka4piece.model.Household;
import com.ka4piece.model.Session;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.repository.DbConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

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
    private DbConfig dbconfig;

    @FXML
    public void initialize() throws IOException {
        dbconfig = new DbConfig("db.properties");
        // Initialize AuthRepository connection
        authRepository = new AuthRepository("jdbc:mysql://localhost:3306/ka4piece_db", "root", "");

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
        switchSceneFromButton(event, "/forgot_password.fxml");
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
     * Database authentication check & Session setup.
     */
    private boolean performAuthentication(String identifier, String password, String userType) {
        if (authRepository == null) return false;

        if ("OFFICIAL".contains(userType)) {
            // Check by ID or Email
            BarangayOfficial official = authRepository.findOfficialById(identifier);
            if (official == null) {
                official = authRepository.findOfficialByEmail(identifier);
            }

            if (official != null && password.equals(official.getPassword())) {
                // Set global session
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

            if (household != null && password.equals(household.getPassword())) {
                // Set global session
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
     * Scene routing to appropriate dashboard after login.
     */
    private void navigateToMainApp(ActionEvent event, String userType) {
        if ("OFFICIAL".contains(userType)) {
            switchSceneFromButton(event, "/official_dashboard.fxml");
        } else {
            switchSceneFromButton(event, "/beneficiary_dashboard.fxml");
        }
    }

    private void switchSceneFromButton(ActionEvent event, String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                resource = getClass().getResource("/view" + fxmlPath);
            }
            if (resource == null) {
                showError("Could not find view file: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading screen: " + e.getMessage());
        }
    }
}