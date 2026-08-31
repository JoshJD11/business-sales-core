package io.github.joshua.notification;

public interface NotificationSender {
    public void sendNotification(String recipient, String message);
}
