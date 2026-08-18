package com.ka4piece.controller;

import com.ka4piece.model.BarangayOfficial;
import com.ka4piece.model.Household;
import com.ka4piece.repository.AuthRepository;
import com.ka4piece.utilities.PasswordUtil;

/**
 * Flow:
 *  1. Receive an email and a userType ("OFFICIAL" or "BENEFICIARY").
 *  2. Look up the account in the correct table.
 *  3. Generate a temporary password using PasswordUtil.
 *  4. Hash and persist it back to the database via AuthRepository.
 *  5. Return the plain-text temporary password on success, or an error message string on failure.
 */
public class ForgotPasswordController {

    private final AuthRepository authRepository;

    public ForgotPasswordController(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    /**
     * Attempts to reset the password for the given email and user type.
     *  email the email to reset
     *  userType "OFFICIAL" or "BENEFICIARY"
     */
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

    // -------------------------------------------------------------------------
    // Result wrapper — avoids using raw Strings as a two-state return value
    // -------------------------------------------------------------------------

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
