package io.github.joshua.notification;
import io.github.joshua.util.AppConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;


public class EmailNotificationSender implements NotificationSender {

    private final String fromEmail;
    private final String appPassword;
    private final Session session;

    public EmailNotificationSender() {
        this.fromEmail = AppConfig.get("GMAIL_ADDRESS");
        this.appPassword = AppConfig.get("GMAIL_APP_PASSWORD");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        this.session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });
    }

    @Override
    public void sendNotification(String message) {
        send(fromEmail, "AVISO SOBRE EL INVENTARIO DEL NEGOCIO", message);
    }

    public void send(String recipient, String subject, String body) {
        try {
            MimeMessage mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(fromEmail));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            mimeMessage.setSubject(subject);
            mimeMessage.setText(body);

            Transport.send(mimeMessage);
            System.out.println("Correo enviado correctamente.");

        } catch (MessagingException e) {
            System.out.println("Error al enviar correo: " + e.getMessage());
        }
    }
}
