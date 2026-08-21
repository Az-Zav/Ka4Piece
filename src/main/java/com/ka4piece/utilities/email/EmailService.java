package com.ka4piece.utilities.email;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import javafx.application.Platform;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Sends system emails (new-account creation and password reset) on a background
 * daemon thread so the JavaFX UI thread is never blocked.
 */
public class EmailService {

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "ka4piece-email-worker");
                t.setDaemon(true);
                return t;
            });

    private static volatile Session mailSession;
    private static volatile EmailConfig emailConfig;

    private EmailService() {}

    public static void setEmailConfig(EmailConfig config) {
        synchronized (EmailService.class) {
            emailConfig = config;
            mailSession = null; // Reset session on config change
        }
    }

    public static EmailConfig getEmailConfig() {
        return emailConfig;
    }

    /**
     * Sends a password reset credentials email asynchronously.
     */
    public static void sendCredentialsEmail(
            String recipientEmail,
            String recipientName,
            String username,
            String temporaryPassword,
            SuccessCallback onSuccess,
            FailureCallback onFailure) {

        sendEmailAsync(
                recipientEmail,
                recipientName,
                "[Ka4Piece] Password Reset Request",
                buildPasswordResetPlainText(recipientName, username, temporaryPassword),
                buildPasswordResetHtmlBody(recipientName, username, temporaryPassword),
                onSuccess,
                onFailure
        );
    }

    /**
     * Sends an account creation credentials email asynchronously upon new household registration.
     *
     * @param recipientEmail   destination email address
     * @param recipientName    name of the household head
     * @param username         account username/email
     * @param temporaryPassword newly generated temporary password
     * @param onSuccess        callback invoked on JavaFX thread upon success
     * @param onFailure        callback invoked on JavaFX thread upon failure
     */
    public static void sendAccountCreationEmail(
            String recipientEmail,
            String recipientName,
            String username,
            String temporaryPassword,
            SuccessCallback onSuccess,
            FailureCallback onFailure) {

        sendEmailAsync(
                recipientEmail,
                recipientName,
                "[Ka4Piece] Welcome to Ka4Piece - Your Account Credentials",
                buildAccountCreationPlainText(recipientName, username, temporaryPassword),
                buildAccountCreationHtmlBody(recipientName, username, temporaryPassword),
                onSuccess,
                onFailure
        );
    }

    /**
     * Internal helper to submit an email sending task to the background executor.
     */
    private static void sendEmailAsync(
            String recipientEmail,
            String recipientName,
            String subject,
            String plainText,
            String htmlContent,
            SuccessCallback onSuccess,
            FailureCallback onFailure) {

        EXECUTOR.submit(() -> {
            try {
                EmailConfig cfg = emailConfig;
                if (cfg == null) {
                    throw new IllegalStateException("EmailConfig has not been initialized. Call EmailService.setEmailConfig() first.");
                }
                Session session = getSession(cfg);
                Message message = buildMessage(
                        session,
                        cfg,
                        recipientEmail,
                        recipientName,
                        subject,
                        plainText,
                        htmlContent
                );
                Transport.send(message);

                if (onSuccess != null) {
                    Platform.runLater(() -> onSuccess.onSuccess(recipientEmail));
                }

            } catch (MessagingException | UnsupportedEncodingException | IllegalStateException e) {
                if (onFailure != null) {
                    Platform.runLater(() -> onFailure.onFailure(recipientEmail, e.getMessage()));
                }
            }
        });
    }

    private static Session getSession(EmailConfig cfg) {
        if (mailSession == null) {
            synchronized (EmailService.class) {
                if (mailSession == null) {
                    Properties props = new Properties();
                    props.put("mail.smtp.host",              cfg.getSmtpHost());
                    props.put("mail.smtp.port",              cfg.getSmtpPort());
                    props.put("mail.smtp.auth",              cfg.getSmtpAuth());
                    props.put("mail.smtp.starttls.enable",   cfg.getSmtpStartTls());
                    props.put("mail.smtp.starttls.required", "true");
                    props.put("mail.smtp.ssl.protocols",     "TLSv1.2 TLSv1.3");
                    props.put("mail.smtp.ssl.trust",         cfg.getSmtpHost());
                    props.put("mail.smtp.connectiontimeout", "15000");
                    props.put("mail.smtp.timeout",           "15000");
                    props.put("mail.smtp.writetimeout",      "15000");

                    mailSession = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    cfg.getSenderEmail(),
                                    cfg.getAppPassword()
                            );
                        }
                    });
                }
            }
        }
        return mailSession;
    }

    /**
     * Constructs a multipart MIME message with both plain-text and HTML alternatives.
     */
    private static Message buildMessage(
            Session session,
            EmailConfig cfg,
            String recipientEmail,
            String recipientName,
            String subject,
            String plainText,
            String htmlContent) throws MessagingException, UnsupportedEncodingException {

        MimeMessage msg = new MimeMessage(session);

        // Sender address and name
        msg.setFrom(new InternetAddress(
                cfg.getSenderEmail(),
                cfg.getSenderName(),
                "UTF-8"
        ));

        // Recipient address and name
        msg.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(recipientEmail, recipientName, "UTF-8")
        );

        msg.setSubject(subject, "UTF-8");
        msg.setSentDate(new Date());

        // Multi-part alternative body (plain text + html)
        MimeMultipart multipart = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(plainText, "UTF-8");
        multipart.addBodyPart(textPart);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
        multipart.addBodyPart(htmlPart);

        msg.setContent(multipart);
        return msg;
    }

    // ── Email Content Builders: Password Reset ─────────────────────────────────

    private static String buildPasswordResetPlainText(
            String name, String username, String tempPassword) {

        return "Hello, " + name + "!\n\n"
                + "A password reset has been requested for your Ka4Piece account.\n\n"
                + "Your Login Credentials:\n"
                + "Username:           " + username + "\n"
                + "Temporary Password: " + tempPassword + "\n\n"
                + "You are required to change this password on your next login.\n\n"
                + "Do not share your credentials with anyone.\n"
                + "-- Ka4Piece System";
    }

    private static String buildPasswordResetHtmlBody(
            String name,
            String username,
            String tempPassword) {

        String statusLine = "A password reset has been initiated for your account on the <strong>Ka4Piece System</strong>.";
        String changeNotice = "You will be required to <strong>change your password</strong> on your next login.";

        return renderTemplate(name, statusLine, changeNotice, username, tempPassword);
    }

    // ── Email Content Builders: Account Creation ───────────────────────────────

    private static String buildAccountCreationPlainText(
            String name, String username, String tempPassword) {

        return "Hello, " + name + "!\n\n"
                + "Your household account has been successfully registered on the Ka4Piece System by your Barangay Official.\n\n"
                + "Your Login Credentials:\n"
                + "Username:           " + username + "\n"
                + "Temporary Password: " + tempPassword + "\n\n"
                + "You are required to change this password on your next login.\n\n"
                + "Do not share your credentials with anyone.\n"
                + "-- Ka4Piece System";
    }

    private static String buildAccountCreationHtmlBody(
            String name,
            String username,
            String tempPassword) {

        String statusLine = "Your household account has been successfully registered on the <strong>Ka4Piece System</strong> by your Barangay Official.";
        String changeNotice = "You will be required to <strong>change your password</strong> on your next login.";

        return renderTemplate(name, statusLine, changeNotice, username, tempPassword);
    }

    /**
     * Shared HTML template generator.
     */
    private static String renderTemplate(
            String name,
            String statusLine,
            String changeNotice,
            String username,
            String tempPassword) {

        return "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>Ka4Piece Notification</title>"
                + "</head>"
                + "<body style='"
                +   "margin:0;"
                +   "padding:32px 24px;"
                +   "background:#ffffff;"
                +   "font-family:Arial,\"Segoe UI\",sans-serif;"
                + "'>"
                + "<h2 style='"
                +   "margin:0 0 20px;"
                +   "font-size:26px;"
                +   "font-weight:700;"
                +   "color:#111111;"
                + "'>"
                + "Hello, " + escapeHtml(name) + "!"
                + "</h2>"
                + "<p style='"
                +   "margin:0 0 14px;"
                +   "font-size:15px;"
                +   "line-height:1.6;"
                +   "color:#333333;"
                + "'>"
                + statusLine
                + "</p>"
                + "<p style='"
                +   "margin:0 0 28px;"
                +   "font-size:15px;"
                +   "line-height:1.6;"
                +   "color:#333333;"
                + "'>"
                + changeNotice
                + "</p>"
                + "<p style='"
                +   "margin:0 0 8px;"
                +   "font-size:15px;"
                +   "font-weight:700;"
                +   "color:#111111;"
                + "'>"
                + "Your Login Credentials:"
                + "</p>"
                + "<p style='"
                +   "margin:0;"
                +   "font-size:15px;"
                +   "line-height:2.2;"
                +   "color:#333333;"
                + "'>"
                + "Username:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                + "<span style='font-family:monospace;color:#111111;font-weight:600;'>"
                + escapeHtml(username)
                + "</span>"
                + "<br>"
                + "Temporary Password:&nbsp;"
                + "<span style='font-family:monospace;color:#111111;font-weight:600;'>"
                + escapeHtml(tempPassword)
                + "</span>"
                + "</p>"
                + "<br><br>"
                + "<p style='"
                +   "margin:0;"
                +   "font-size:13px;"
                +   "color:#888888;"
                +   "line-height:1.5;"
                + "'>"
                + "Do not share your credentials with anyone. "
                + "If you did not expect this email, please contact your system administrator."
                + "</p>"
                + "<br>"
                + "<hr style='border:none;border-top:1px solid #eeeeee;margin:0 0 14px;'>"
                + "<p style='"
                +   "margin:0;"
                +   "font-size:12px;"
                +   "color:#aaaaaa;"
                + "'>"
                + "Ka4Piece System"
                + "&nbsp;&bull;&nbsp;"
                + "Automated notification"
                + "&nbsp;&bull;&nbsp;"
                + "Do not reply"
                + "</p>"
                + "</body>"
                + "</html>";
    }

    /**
     * Escapes special HTML characters to prevent rendering artifacts or injection.
     */
    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#39;");
    }

    /**
     * Callback interface for successful email transmission.
     */
    public interface SuccessCallback {
        void onSuccess(String recipientEmail);
    }

    /**
     * Callback interface for failed email transmission.
     */
    public interface FailureCallback {
        void onFailure(String recipientEmail, String errorMessage);
    }
}
