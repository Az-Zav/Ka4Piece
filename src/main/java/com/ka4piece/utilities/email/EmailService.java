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
 *
 * <p>Usage example (password reset):
 * <pre>{@code
 * EmailService.sendCredentialsEmail(
 *     "user@example.com", "Juan dela Cruz",
 *     "jdelacruz", tempPassword,
 *     null, false,
 *     email -> showInfo("Email sent to " + email),
 *     (email, err) -> showError("Could not send email: " + err)
 * );
 * }</pre>
 */
public class EmailService {

    // Two daemon threads handle background email sends.
    // Daemon threads exit automatically when the JVM shuts down,
    // so they won't block application exit.
    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(2, r -> {
                Thread t = new Thread(r, "ka4piece-email-worker");
                t.setDaemon(true);
                return t;
            });

    // Lazily created, thread-safe SMTP session singleton.
    private static volatile Session mailSession;

    // Utility class — prevent instantiation
    private EmailService() {}

    // =========================================================================
    // Public API
    // =========================================================================

    /**
     * Sends a password reset credentials email asynchronously.
     *
     * @param recipientEmail    destination address
     * @param recipientName     display name (used in greeting)
     * @param username          the account username to include in the email
     * @param temporaryPassword plain-text temporary password to include
     * @param onSuccess         called with the recipient address on successful send
     * @param onFailure         called with the recipient address and error message on failure
     */
    public static void sendCredentialsEmail(
            String recipientEmail,
            String recipientName,
            String username,
            String temporaryPassword,
            SuccessCallback onSuccess,
            FailureCallback onFailure) {

        EXECUTOR.submit(() -> {
            try {
                Session session = getSession();
                Message  message = buildMessage(
                        session,
                        recipientEmail,
                        recipientName,
                        username,
                        temporaryPassword
                );
                Transport.send(message);

                if (onSuccess != null) {
                    Platform.runLater(() -> onSuccess.onSuccess(recipientEmail));
                }

            } catch (MessagingException | UnsupportedEncodingException e) {
                if (onFailure != null) {
                    Platform.runLater(() -> onFailure.onFailure(recipientEmail, e.getMessage()));
                }
            }
        });
    }

    // =========================================================================
    // SMTP session
    // =========================================================================

    private static Session getSession() {
        if (mailSession == null) {
            synchronized (EmailService.class) {
                if (mailSession == null) {         // double-checked locking
                    Properties props = new Properties();
                    props.put("mail.smtp.host",              EmailConfig.SMTP_HOST);
                    props.put("mail.smtp.port",              EmailConfig.SMTP_PORT);
                    props.put("mail.smtp.auth",              EmailConfig.SMTP_AUTH);
                    props.put("mail.smtp.starttls.enable",   EmailConfig.SMTP_STARTTLS);
                    props.put("mail.smtp.starttls.required", "true");
                    props.put("mail.smtp.ssl.protocols",     "TLSv1.2 TLSv1.3");
                    props.put("mail.smtp.ssl.trust",         EmailConfig.SMTP_HOST);
                    props.put("mail.smtp.connectiontimeout", "15000");
                    props.put("mail.smtp.timeout",           "15000");
                    props.put("mail.smtp.writetimeout",      "15000");

                    mailSession = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(
                                    EmailConfig.SENDER_EMAIL,
                                    EmailConfig.APP_PASSWORD
                            );
                        }
                    });
                }
            }
        }
        return mailSession;
    }

    // =========================================================================
    // Message builder
    // =========================================================================

    private static Message buildMessage(
            Session session,
            String recipientEmail,
            String recipientName,
            String username,
            String temporaryPassword) throws MessagingException, UnsupportedEncodingException {

        MimeMessage msg = new MimeMessage(session);

        // From
        msg.setFrom(new InternetAddress(
                EmailConfig.SENDER_EMAIL,
                EmailConfig.SENDER_NAME,
                "UTF-8"
        ));

        // To
        msg.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(recipientEmail, recipientName, "UTF-8")
        );

        // Subject + date
        msg.setSubject(EmailConfig.SUBJECT_PASSWORD_RESET, "UTF-8");
        msg.setSentDate(new Date());

        // Body: multipart/alternative — plain-text fallback first, HTML preferred
        MimeMultipart multipart = new MimeMultipart("alternative");

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(
                buildPlainText(recipientName, username, temporaryPassword),
                "UTF-8"
        );
        multipart.addBodyPart(textPart);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(
                buildHtmlBody(recipientName, username, temporaryPassword),
                "text/html; charset=UTF-8"
        );
        multipart.addBodyPart(htmlPart);

        msg.setContent(multipart);
        return msg;
    }

    // =========================================================================
    // Body builders
    // =========================================================================

    private static String buildPlainText(
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

    private static String buildHtmlBody(
            String name,
            String username,
            String tempPassword) {

        String statusLine = "A password reset has been initiated for your account on the <strong>Ka4Piece System</strong>.";
        String changeNotice = "You will be required to <strong>change your password</strong> on your next login.";

        return "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
                + "<title>Ka4Piece Password Reset</title>"
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

    // =========================================================================
    // Helpers
    // =========================================================================

    private static String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#39;");
    }

    // =========================================================================
    // Callback interfaces
    // =========================================================================

    /** Called on the JavaFX Application Thread when the email is sent successfully. */
    public interface SuccessCallback {
        void onSuccess(String recipientEmail);
    }

    /** Called on the JavaFX Application Thread when sending fails. */
    public interface FailureCallback {
        void onFailure(String recipientEmail, String errorMessage);
    }
}
