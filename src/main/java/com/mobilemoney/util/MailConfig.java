package com.mobilemoney.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Charge {@code mail.properties} depuis le classpath.
 * Optionnel : {@code mail.local.properties} (non versionné recommandé pour le mot de passe).
 */
public final class MailConfig {
    private static final Properties PROPERTIES = new Properties();

    static {
        loadOptional("mail.properties");
        loadOptional("mail.local.properties");
    }

    private static void loadOptional(String resource) {
        try (InputStream input = MailConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (input != null) {
                PROPERTIES.load(input);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger " + resource + ".", e);
        }
    }

    private MailConfig() {
    }

    public static String getFrom() {
        String f = trimOrEmpty(PROPERTIES.getProperty("mail.from"));
        if (!f.isEmpty()) {
            return f;
        }
        String u = trimOrEmpty(PROPERTIES.getProperty("mail.smtp.user"));
        if (!u.isEmpty()) {
            return u;
        }
        return "noreply@mobilemoney.local";
    }

    public static String getSmtpHost() {
        return trimOrEmpty(PROPERTIES.getProperty("mail.smtp.host", "localhost"));
    }

    public static int getSmtpPort() {
        String p = PROPERTIES.getProperty("mail.smtp.port", "25");
        try {
            return Integer.parseInt(trimOrEmpty(p));
        } catch (NumberFormatException e) {
            return 25;
        }
    }

    /**
     * Gmail et la plupart des fournisseurs : true + utilisateur / mot de passe.
     */
    public static boolean isSmtpAuth() {
        return Boolean.parseBoolean(trimOrEmpty(PROPERTIES.getProperty("mail.smtp.auth", "false")));
    }

    public static boolean isStartTlsEnabled() {
        return Boolean.parseBoolean(trimOrEmpty(PROPERTIES.getProperty("mail.smtp.starttls.enable", "false")));
    }

    public static String getSmtpUser() {
        return trimOrEmpty(PROPERTIES.getProperty("mail.smtp.user"));
    }

    public static String getSmtpPassword() {
        String p = PROPERTIES.getProperty("mail.smtp.password", "");
        if (p == null) {
            return "";
        }
        return p.replaceAll("\\s+", "").trim();
    }

    private static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }
}
