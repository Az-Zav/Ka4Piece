package com.ka4piece.utilities.email;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailConfig {
    private final String senderEmail, appPassword, senderName;
    private final String smtpHost, smtpPort, smtpAuth, smtpStartTls;

    // Reads from a properties file to get email configuration details
    public EmailConfig(String propertiesFilePath) throws IOException {
        Properties props = new Properties();
        try (InputStream input = new FileInputStream(propertiesFilePath)) {
            props.load(input);
        }

        this.senderEmail = props.getProperty("senderEmail", props.getProperty("email.sender", ""));
        this.appPassword = props.getProperty("appPassword", props.getProperty("email.password", ""));
        this.senderName = props.getProperty("senderName", props.getProperty("email.name", "Ka4Piece System"));
        this.smtpHost = props.getProperty("smtpHost", props.getProperty("email.smtp.host", "smtp.gmail.com"));
        this.smtpPort = props.getProperty("smtpPort", props.getProperty("email.smtp.port", "587"));
        this.smtpAuth = props.getProperty("smtpAuth", props.getProperty("email.smtp.auth", "true"));
        this.smtpStartTls = props.getProperty("smtpStartTls", props.getProperty("email.smtp.starttls", "true"));
    }

    public String getSenderEmail() { return senderEmail; }
    public String getAppPassword() { return appPassword; }
    public String getSenderName() { return senderName; }
    public String getSmtpHost() { return smtpHost; }
    public String getSmtpPort() { return smtpPort; }
    public String getSmtpAuth() { return smtpAuth; }
    public String getSmtpStartTls() { return smtpStartTls; }
}
