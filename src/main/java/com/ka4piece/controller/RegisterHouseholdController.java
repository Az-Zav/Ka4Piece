package com.ka4piece.controller;

import com.ka4piece.manager.AuthManager;
import com.ka4piece.model.Household;
import com.ka4piece.utilities.IdUtil;
import com.ka4piece.utilities.PasswordUtil;
import com.ka4piece.utilities.email.EmailService;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the Household Registration modal dialog.
 * Handles household registration, compliance eligibility inputs, secure credential generation,
 * persistence, automated credentials email dispatch, and auto-closing upon success.
 */
public class RegisterHouseholdController {

    private AuthManager authManager;

    /**
     * Dependency-injected constructor.
     *
     * @param authManager authentication and account manager
     */
    public RegisterHouseholdController(AuthManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Default no-arg constructor for FXML loader fallback.
     */
    public RegisterHouseholdController() {
    }

    // ── FXML Controls ──────────────────────────────────────────────────────────
    @FXML private TextField txtHeadName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;
    @FXML private TextField txtEmailField;
    @FXML private TextField txtIdField;
    @FXML private Button btnRegister;
    @FXML private Label lblSuccessMessage;

    // Compliance eligibility checkboxes
    @FXML private CheckBox chkPregnant;
    @FXML private CheckBox chkToddler;

    // Student count steppers
    @FXML private TextField txtElemCount;
    @FXML private TextField txtJhsCount;
    @FXML private TextField txtShsCount;

    // ── Stepper Event Handlers ─────────────────────────────────────────────────

    @FXML
    private void incrementElem() {
        updateCount(txtElemCount, 1);
    }

    @FXML
    private void decrementElem() {
        updateCount(txtElemCount, -1);
    }

    @FXML
    private void incrementJhs() {
        updateCount(txtJhsCount, 1);
    }

    @FXML
    private void decrementJhs() {
        updateCount(txtJhsCount, -1);
    }

    @FXML
    private void incrementShs() {
        updateCount(txtShsCount, 1);
    }

    @FXML
    private void decrementShs() {
        updateCount(txtShsCount, -1);
    }

    /**
     * Helper to safely update stepper counts while preventing negative values.
     */
    private void updateCount(TextField field, int delta) {
        if (field == null) return;
        try {
            int current = Integer.parseInt(field.getText().trim());
            int updated = Math.max(0, current + delta);
            field.setText(String.valueOf(updated));
        } catch (NumberFormatException e) {
            field.setText("0");
        }
    }

    // ── Registration Logic ─────────────────────────────────────────────────────

    /**
     * Validates input fields, persists the new household account, sends login credentials via email,
     * and automatically closes the modal dialog upon completion.
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            // 1. Extract and clean input values from registration form
            String headName = txtHeadName.getText() == null ? "" : txtHeadName.getText().trim();
            String address = txtAddress.getText() == null ? "" : txtAddress.getText().trim();
            String barangay = txtBarangay.getText() == null ? "" : txtBarangay.getText().trim();
            String email = txtEmailField.getText() == null ? "" : txtEmailField.getText().trim();

            // 2. Extract boolean flags and student counts
            boolean hasPregnant = (chkPregnant != null) && chkPregnant.isSelected();
            boolean hasToddler = (chkToddler != null) && chkToddler.isSelected();

            int elemCount = parseCount(txtElemCount);
            int jhsCount = parseCount(txtJhsCount);
            int shsCount = parseCount(txtShsCount);

            // 3. Check if all required text fields are filled in
            if (headName.isEmpty() || email.isEmpty() || address.isEmpty() || barangay.isEmpty()) {
                showError("Please fill in all required fields.");
                return;
            }

            // 4. Generate unique ID and secure temporary password
            String generatedId = IdUtil.generateId("HH");
            String temporaryPassword = PasswordUtil.generateTemporaryPassword();
            String hashedPassword = PasswordUtil.hashPassword(temporaryPassword);

            // 5. Create new Household object including compliance fields
            Household newHousehold = new Household(
                    generatedId,
                    headName,
                    address,
                    barangay,
                    email,
                    hashedPassword
            );
            newHousehold.setHasPregnantMember(hasPregnant);
            newHousehold.setHas0to5Member(hasToddler);
            newHousehold.setElemCount(elemCount);
            newHousehold.setJhsCount(jhsCount);
            newHousehold.setShsCount(shsCount);

            // 6. Register the household via AuthManager
            authManager.registerHousehold(newHousehold);

            // 7. Disable register button after successful creation
            if (btnRegister != null) {
                btnRegister.setDisable(true);
            }

            // 8. Display initial success message with generated ID
            if (lblSuccessMessage != null) {
                lblSuccessMessage.setText("Household registered successfully: " + generatedId + ". Sending credentials email...");
                lblSuccessMessage.setVisible(true);
            }

            // 9. Automatically dispatch login credentials email asynchronously and auto-close modal on completion
            EmailService.sendAccountCreationEmail(
                    email,
                    headName,
                    email,
                    temporaryPassword,
                    recipientEmail -> {
                        if (lblSuccessMessage != null) {
                            lblSuccessMessage.setText("Household registered successfully (" + generatedId + "). Credentials sent to " + recipientEmail + ".");
                        }

                        // Automatically close modal after brief delay matching ForgotPasswordController
                        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
                        delay.setOnFinished(e -> closeModal());
                        delay.play();
                    },
                    (recipientEmail, errorMsg) -> {
                        if (lblSuccessMessage != null) {
                            lblSuccessMessage.setText("Household registered (" + generatedId + "), but email delivery failed: " + errorMsg);
                        }
                    }
            );

        } catch (IllegalArgumentException e) {
            System.err.println("[RegisterHousehold] Validation error: " + e.getMessage());
            showError(e.getMessage());

        } catch (RuntimeException e) {
            System.err.println("[RegisterHousehold] Database error during registration: " + e.getMessage());
            e.printStackTrace();
            showError("A database error occurred. Please contact support.");

        } catch (Exception e) {
            System.err.println("[RegisterHousehold] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            showError("An unexpected error occurred. Please try again.");
        }
    }

    /**
     * Programmatic closure of the registration modal window.
     */
    private void closeModal() {
        if (txtEmailField != null && txtEmailField.getScene() != null && txtEmailField.getScene().getWindow() != null) {
            Stage stage = (Stage) txtEmailField.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Safely parse integer values from stepper text fields.
     */
    private int parseCount(TextField field) {
        if (field == null || field.getText() == null) return 0;
        try {
            return Math.max(0, Integer.parseInt(field.getText().trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Helper method to print error messages to UI label.
     */
    private void showError(String message) {
        if (lblSuccessMessage != null) {
            lblSuccessMessage.setText(message);
            lblSuccessMessage.setVisible(true);
        }
    }
}