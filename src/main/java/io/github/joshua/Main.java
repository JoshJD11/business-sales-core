package io.github.joshua;
import io.github.joshua.notification.NotificationSender;
// import io.github.joshua.notification.WhatsAppNotificationSender;
import io.github.joshua.notification.EmailNotificationSender;

public class Main {
    public static void main(String[] args) {
        NotificationSender notificationSender = new EmailNotificationSender();
        notificationSender.sendNotification("joshua.jimenez.delgado@gmail.com", "Mensaje de prueba desde Java");
    }
}
