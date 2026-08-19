package com.ka4piece.controller;

import com.ka4piece.manager.AuthManager;
import com.ka4piece.model.Household;
import com.ka4piece.utilities.PasswordUtil;
import com.ka4piece.utilities.IdUtil;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class RegisterHouseholdController {

    private AuthManager authManager;

    // Constructor injection
    public RegisterHouseholdController(AuthManager authManager) {
        this.authManager = authManager;
    }

    public RegisterHouseholdController() {
    }

    // --- FXML CONTROL INJECTIONS ---
    @FXML private TextField txtHeadName;
    @FXML private TextField txtAddress;
    @FXML private TextField txtBarangay;
    @FXML private TextField txtEmailField;
    @FXML private TextField txtIdField;
    @FXML private Button btnRegister;
    @FXML private Label lblSuccessMessage;

    // --- NEW FXML CONTROLS FOR COMPLIANCE CONDITIONS ---
    @FXML private CheckBox chkPregnant;
    @FXML private CheckBox chkToddler;

    // Custom Stepper TextFields (- / +)
    @FXML private TextField txtElemCount;
    @FXML private TextField txtJhsCount;
    @FXML private TextField txtShsCount;

    // --- STEPPER EVENT HANDLERS (- / +) ---

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

    // --- REGISTRATION LOGIC ---

    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            // 1. Extract and clean input values from registration form
            String headName = txtHeadName.getText().trim();
            String address = txtAddress.getText().trim();
            String barangay = txtBarangay.getText().trim();
            String email = txtEmailField.getText().trim();

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

            // 4. Generate ID
            String generatedId = IdUtil.generateId("HH");

            // 5. Create new Household object including the new compliance fields
            Household newHousehold = new Household(
                    generatedId,
                    headName,
                    address,
                    barangay,
                    email,
                    PasswordUtil.generateTemporaryPassword()
            );
            newHousehold.setHasPregnantMember(hasPregnant);
            newHousehold.setHas0to5Member(hasToddler);
            newHousehold.setElemCount(elemCount);
            newHousehold.setJhsCount(jhsCount);
            newHousehold.setShsCount(shsCount);


            // 6. Register the household via AuthManager
            authManager.registerHousehold(newHousehold);

            // 7. Display Success Message with generated ID
            if (lblSuccessMessage != null) {
                lblSuccessMessage.setText("Household registered successfully: " + generatedId);
                lblSuccessMessage.setVisible(true);
            }

            // 8. Disable register button after registration while modal stays open for review
            if (btnRegister != null) {
                btnRegister.setDisable(true);
            }

            // 9. Complete catching of errors with dedicated messages
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