package io.github.joshua.sales;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import io.github.joshua.database.DBConnection;

public class SalesService {

    public void insertSale(String productName, int quantity, String paymentMethod, String customerEmail) {
        String sql = "{CALL InsertSale(?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, productName);
            stmt.setInt(2, quantity);
            stmt.setString(3, paymentMethod);

            if (customerEmail != null && !customerEmail.isEmpty()) {
                stmt.setString(4, customerEmail);
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.execute();
            System.out.println("Venta registrada correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al registrar la venta: " + e.getMessage());
        }
    }

    public void generateReceipt(String productName, int quantity) {
        System.out.println("Recibo generado para el producto: " + productName + " con cantidad: " + quantity); // Not implemented yet
    }

}
