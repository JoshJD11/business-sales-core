package io.github.joshua.inventory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import io.github.joshua.database.DBConnection;
import io.github.joshua.notification.EmailNotificationSender;
import io.github.joshua.notification.NotificationSender;
import io.github.joshua.notification.WhatsAppNotificationSender;
import io.github.cdimascio.dotenv.Dotenv;

public class InventoryManager {

    private NotificationSender notificationSender;

    public InventoryManager(String notificationType) {
        if (notificationType.equalsIgnoreCase("email")) {
            this.notificationSender = new EmailNotificationSender();
        } else {
            this.notificationSender = new WhatsAppNotificationSender();
        }
    }

    public boolean isStackLimitExceeded(String productName) {
        String sql = "SELECT quantity_on_hand, minimum_stock FROM Dim_Product p RIGHT JOIN Inventory i ON p.product_id = i.product_id WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productName);

            var rs = stmt.executeQuery();
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity_on_hand");
                int minimumStock = rs.getInt("minimum_stock");
                return currentQuantity <= minimumStock;
            }

        } catch (SQLException e) {
            System.out.println("Error al verificar el límite de pila: " + e.getMessage());
        }

        return false;
    }

    public void checkIfHaveToNotify(String productName) {
        if (isStackLimitExceeded(productName)) {
            String message = "El producto " + productName + " ha alcanzado su límite de pila.";
            if (notificationSender instanceof EmailNotificationSender) {
                String recipientEmail = Dotenv.load().get("GMAIL_ADDRESS");
                notificationSender.sendNotification(recipientEmail, message);
            } else {
                String recipientPhoneNumber = Dotenv.load().get("WHATSAPP_PHONE_NUMBER"); // <country_code><number>@c.us
                notificationSender.sendNotification(recipientPhoneNumber, message);
            }
        }
    }
}
