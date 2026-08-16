package com.ka4piece.utilities.email;

/**
 * Centralised SMTP / email configuration for the Ka4Piece system.
 *
 * Fill in SENDER_EMAIL, APP_PASSWORD, and SENDER_NAME before deploying.
 * All other values are suitable defaults for Gmail STARTTLS on port 587.
 */
public class EmailConfig {

    // ── Sender identity ──────────────────────────────────────────────────────
    public static final String SENDER_EMAIL = "";      // Gmail From-address
    public static final String APP_PASSWORD  = "";     // Gmail App Password
    public static final String SENDER_NAME   = "Ka4Piece System";  // Display name

    // ── SMTP settings (Gmail STARTTLS) ───────────────────────────────────────
    public static final String SMTP_HOST     = "smtp.gmail.com";
    public static final String SMTP_PORT     = "587";   // STARTTLS port
    public static final String SMTP_AUTH     = "true";
    public static final String SMTP_STARTTLS = "true";

    // ── Email subject templates ───────────────────────────────────────────────
    public static final String SUBJECT_PASSWORD_RESET = "[Ka4Piece] Password Reset Request";

    // Utility class — prevent instantiation
    private EmailConfig() {}
}
