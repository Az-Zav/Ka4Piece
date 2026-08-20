package com.ka4piece.controller;

import com.ka4piece.model.BarangayOfficial;
import com.ka4piece.model.Household;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.utilities.PasswordUtil;
import com.ka4piece.utilities.email.EmailService;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Flow:
 *  1. Receive an email and a userType ("OFFICIAL" or "BENEFICIARY").
 *  2. Look up the account in the correct table.
 *  3. Generate a temporary password using PasswordUtil.
 *  4. Hash and persist it back to the database via AuthRepository.
 *  5. Return the plain-text temporary password on success, or an error message string on failure.
 */
public class ForgotPasswordController {

    // ── FXML Controls ──────────────────────────────────────────────────────────
    @FXML private TextField txtEmail;
    @FXML private Label lblMessage;
    @FXML private Button btnSend;

    // ── Dependencies ───────────────────────────────────────────────────────────
    private final AuthRepository authRepository;

    public ForgotPasswordController(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }


    public ForgotPasswordController() {
        this.authRepository = null;
    }

    // ── FXML Lifecycle ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        hideMessage();
    }

    // ── Action Handlers ────────────────────────────────────────────────────────

    /**
     * Triggered when the user clicks "Send Temporary Password".
     * Validates the email, finds the matching account, resets the password,
     * and dispatches a credentials email asynchronously.
     */
    @FXML
    private void handleSendReset(ActionEvent event) {
        String email = txtEmail.getText() == null ? "" : txtEmail.getText().trim();

        if (email.isEmpty()) {
            showMessage("Please enter your email address.", true);
            return;
        }

        if (authRepository == null) {
            showMessage("Service unavailable. Please restart the application.", true);
            return;
        }

        // Try OFFICIAL first, then HOUSEHOLD
        ResetResult result = handleReset(email);

        if (!result.isSuccess()) {
            showMessage(result.getErrorMessage(), true);
            return;
        }

        // Retrieve display info for the email body
        String recipientName;
        String username;

        BarangayOfficial official = authRepository.findOfficialByEmail(email);
        if (official != null) {
            recipientName = official.getName();
            username      = official.getEmail(); // Use email as login credential
        } else {
            Household household = authRepository.findHouseholdByEmail(email);
            if (household != null) {
                recipientName = household.getHeadName();
                username      = household.getEmail(); // Use email as login credential
            } else {
                showMessage("Account not found. Please check the email address.", true);
                return;
            }
        }

        final String tempPassword = result.getTempPassword();
        final String finalName    = recipientName;
        final String finalUser    = username;

        // Disable button to prevent duplicate requests while email is in flight
        if (btnSend != null) btnSend.setDisable(true);

        EmailService.sendCredentialsEmail(
                email,
                finalName,
                finalUser,
                tempPassword,
                recipientEmail -> {
                    showMessage("Email sent successfully.", false);

                    PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
                    delay.setOnFinished(e -> closeModal());
                    delay.play();
                },
                (recipientEmail, errorMsg) -> {
                    if (btnSend != null) btnSend.setDisable(false);
                    showMessage("Password was reset but the email could not be sent: " + errorMsg, true);
                }
        );
    }

    /**
     * Closes the modal when the user clicks "Cancel".
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeModal(event);
    }

    // ── Business Logic (Password Reset) ────────────────────────────────────────

    /**
     * Attempts to reset the password for the given email address.
     * Searches OFFICIAL accounts first, then HOUSEHOLD accounts.
     *
     * @param email user email address
     * @return ResetResult instance encapsulating success status and temporary password or error message
     */
    public ResetResult handleReset(String email) {
        if (email == null || email.isBlank()) {
            return ResetResult.failure("Please enter your email.");
        }

        String trimmed = email.trim();

        try {
            ResetResult officialResult = resetOfficialPassword(trimmed);
            if (officialResult.isSuccess()) {
                return officialResult;
            }

            return resetHouseholdPassword(trimmed);

        } catch (RuntimeException e) {
            return ResetResult.failure("Database error: " + e.getMessage());
        }
    }


    public ResetResult handleReset(String email, String userType) {
        // --- Input validation ---
        if (email == null || email.isBlank()) {
            return ResetResult.failure("Please enter your email.");
        }
        if (userType == null || userType.isBlank()) {
            return ResetResult.failure("User type is required.");
        }

        String trimmed = email.trim();

        try {
            if ("OFFICIAL".equalsIgnoreCase(userType)) {
                return resetOfficialPassword(trimmed);
            } else if ("BENEFICIARY".equalsIgnoreCase(userType)) {
                return resetHouseholdPassword(trimmed);
            } else {
                return ResetResult.failure("Unknown user type: " + userType);
            }
        } catch (RuntimeException e) {
            return ResetResult.failure("Database error: " + e.getMessage());
        }
    }

    // --- Private helpers ---

    private ResetResult resetOfficialPassword(String email) {
        BarangayOfficial official = authRepository.findOfficialByEmail(email);
        if (official == null) {
            return ResetResult.failure("No official account found with that email.");
        }

        String tempPassword = PasswordUtil.generateTemporaryPassword();
        String hashed = PasswordUtil.hashPassword(tempPassword);

        authRepository.updateOfficialPassword(official.getOfficialId(), hashed);

        return ResetResult.success(tempPassword);
    }

    private ResetResult resetHouseholdPassword(String email) {
        Household household = authRepository.findHouseholdByEmail(email);
        if (household == null) {
            return ResetResult.failure("No beneficiary account found with that email.");
        }

        String tempPassword = PasswordUtil.generateTemporaryPassword();
        String hashed = PasswordUtil.hashPassword(tempPassword);

        authRepository.updateHouseholdPassword(household.getHouseholdId(), hashed);

        return ResetResult.success(tempPassword);
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────────

    private void showMessage(String msg, boolean isError) {
        if (lblMessage != null) {
            lblMessage.setText(msg);
            lblMessage.setTextFill(isError ? Color.web("#DC2626") : Color.web("#16A34A"));
            lblMessage.setVisible(true);
            lblMessage.setManaged(true);
        }
    }

    private void hideMessage() {
        if (lblMessage != null) {
            lblMessage.setVisible(false);
            lblMessage.setManaged(false);
        }
    }

    private void closeModal(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void closeModal() {
        if (txtEmail != null && txtEmail.getScene() != null && txtEmail.getScene().getWindow() != null) {
            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.close();
        }
    }

    // ── Result Wrapper ─────────────────────────────────────────────────────────

    /**
     * Encapsulates the outcome of a password-reset attempt.
     */
    public static class ResetResult {
        private final boolean success;
        private final String tempPassword; // non-null on success
        private final String errorMessage; // non-null on failure

        private ResetResult(boolean success, String tempPassword, String errorMessage) {
            this.success = success;
            this.tempPassword = tempPassword;
            this.errorMessage = errorMessage;
        }

        public static ResetResult success(String tempPassword) {
            return new ResetResult(true, tempPassword, null);
        }

        public static ResetResult failure(String errorMessage) {
            return new ResetResult(false, null, errorMessage);
        }

        public boolean isSuccess()       { return success; }
        public String getTempPassword()  { return tempPassword; }
        public String getErrorMessage()  { return errorMessage; }
    }
}
