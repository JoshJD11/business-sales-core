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

    public void checkIfHaveToNotify(String productName) { // This only will notify email or whatsapp if the product has reached its stack limit, the idea is to never notify in other social media.
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

    public void updateMinimumStock(String productName, int newMinimumStock) {
        String sql = "UPDATE Dim_Product SET minimum_stock = ? WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newMinimumStock);
            stmt.setString(2, productName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Límite de pila actualizado correctamente para el producto: " + productName);
            } else {
                System.out.println("No se encontró el producto: " + productName);
            }

            checkIfHaveToNotify(productName);

        } catch (SQLException e) {
            System.out.println("Error al actualizar el límite de pila: " + e.getMessage());
        }
    }

    public void updateQuantityOnHand(String productName, int newQuantity) {
        String sql = "UPDATE Inventory SET quantity_on_hand = ? WHERE product_id = (SELECT product_id FROM Dim_Product WHERE product_name = ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newQuantity);
            stmt.setString(2, productName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cantidad en mano actualizada correctamente para el producto: " + productName);
            } else {
                System.out.println("No se encontró el producto: " + productName);
            }

            checkIfHaveToNotify(productName);

        } catch (SQLException e) {
            System.out.println("Error al actualizar la cantidad en mano: " + e.getMessage());
        }
    }

    public void consultProductStock(String productName) {
        String sql = "SELECT quantity_on_hand, minimum_stock FROM Dim_Product p RIGHT JOIN Inventory i ON p.product_id = i.product_id WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productName);

            var rs = stmt.executeQuery();
            if (rs.next()) {
                int currentQuantity = rs.getInt("quantity_on_hand");
                int minimumStock = rs.getInt("minimum_stock");
                System.out.println("Producto: " + productName);
                System.out.println("Cantidad en mano: " + currentQuantity);
                System.out.println("Límite de pila: " + minimumStock);
            } else {
                System.out.println("No se encontró el producto: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar el stock del producto: " + e.getMessage());
        }
    }
}
