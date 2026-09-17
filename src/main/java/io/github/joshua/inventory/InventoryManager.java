package io.github.joshua.inventory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import io.github.joshua.database.DBConnection;
import io.github.joshua.notification.NotificationSender;
import io.github.joshua.util.QueryResultPresenter;
import io.github.joshua.util.QueryResult;


public class InventoryManager {

    private NotificationSender notificationSender;
    private Scanner scanner;

    public InventoryManager(NotificationSender ns) {
        setNotificationMethod(ns);
        this.scanner = new Scanner(System.in);
    }

    public void setNotificationMethod(NotificationSender ns) {
        this.notificationSender = ns;
    }

    private boolean isStackLimitExceeded(String productName) {
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

    private void checkIfHaveToNotify(String productName) { // This only will notify email or whatsapp if the product has reached its stack limit, the idea is to never notify in other social media.
        if (isStackLimitExceeded(productName)) {
            String message = "El producto " + productName + " ha alcanzado su límite de pila.";
            notificationSender.sendNotification(message); // This only notify the admin (The email or wsp number that is in the env file).
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

    private void consultProductStock(String productName) {
        try {
            QueryResultPresenter.present(queryProductStock(productName));
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el stock del producto: " + e.getMessage());
        }
    }

    public QueryResult queryProductStock(String productName) throws SQLException {
        String sql = "SELECT i.inventory_id, p.product_name, i.quantity_on_hand, i.minimum_stock, i.last_updated "
                + "FROM Dim_Product p LEFT JOIN Inventory i ON p.product_id = i.product_id"
                + (productName == null ? "" : " WHERE p.product_name = ?");
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            if (productName != null) statement.setString(1, productName);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }

    public void init() {
        boolean exit = false;

        while(!exit) {
            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Consultar inventario del producto");
            System.out.println("2. Actualizar stock mínimo del producto");
            System.out.println("3. Actualizar cantidad de un producto en inventario");
            System.out.println("4. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForInventory = scanner.nextLine();
                    consultProductStock(productNameForInventory);
                    break;
                
                case "2":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForMinStock = scanner.nextLine();
                    System.out.print("Ingrese el nuevo límite del stock: ");
                    try {
                        int newMinStock = Integer.parseInt(scanner.nextLine());
                        updateMinimumStock(productNameForMinStock, newMinStock);
                    } catch (NumberFormatException e) {
                        System.out.println("Límite de stock inválido. Por favor, ingrese un número entero.");
                    }
                    break;

                case "3":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameToUpdate = scanner.nextLine();
                    System.out.print("Ingrese la nueva cantidad: ");
                    try {
                        int newQuantity = Integer.parseInt(scanner.nextLine());
                        updateQuantityOnHand(productNameToUpdate, newQuantity);
                    } catch (NumberFormatException e) {
                        System.out.println("Cantidad inválida. Por favor, ingrese un número entero.");
                    }
                    break;

                case "4":
                    exit = true;
                    break;
                
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                    break;
            }
        }
    }
}
