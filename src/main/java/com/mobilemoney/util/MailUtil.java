package com.mobilemoney.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MailUtil {
    private static final Logger LOG = Logger.getLogger(MailUtil.class.getName());

    private MailUtil() {
    }

    /**
     * Envoie un mail via SMTP. Les erreurs sont journalisées mais n'interrompent pas l'opération métier.
     * Gmail : connexion explicite au transport (plus fiable que Transport.send seul avec auth).
     */
    public static void sendMail(String smtpHost, int smtpPort, String from, String to, String subject, String body) {
        boolean auth = MailConfig.isSmtpAuth();
        String user = MailConfig.getSmtpUser();
        String password = MailConfig.getSmtpPassword();
        if (auth && (user.isEmpty() || password.isEmpty())) {
            LOG.warning("Mail non envoye (vers " + to + ") : mail.smtp.auth=true mais mail.smtp.user ou mail.smtp.password manquant dans mail.properties.");
            return;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.connectiontimeout", "15000");
            props.put("mail.smtp.timeout", "15000");
            if (auth) {
                props.put("mail.smtp.auth", "true");
            }
            if (MailConfig.isStartTlsEnabled()) {
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.starttls.required", "true");
                props.put("mail.smtp.ssl.trust", smtpHost);
                props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            }

            Session session;
            if (auth) {
                final String u = user;
                final String p = password;
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(u, p);
                    }
                });
            } else {
                session = Session.getInstance(props);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");

            if (auth) {
                try (Transport transport = session.getTransport("smtp")) {
                    transport.connect(smtpHost, smtpPort, user, password);
                    transport.sendMessage(message, message.getAllRecipients());
                }
            } else {
                Transport.send(message);
            }
            LOG.info("Mail envoye avec succes vers " + to);
        } catch (MessagingException e) {
            Throwable cause = e.getNextException() != null ? e.getNextException() : e;
            LOG.log(Level.WARNING,
                    "Mail non envoye (vers " + to + " via " + smtpHost + ":" + smtpPort + "). "
                            + "Causes frequentes : mot de passe d'application Google incorrect, champs mail vides en base, ou spam. "
                            + "Detail : " + e.getMessage() + (cause != e ? " | " + cause.getMessage() : ""),
                    e);
        }
    }
}
