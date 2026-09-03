package io.github.joshua.sales;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.sql.PreparedStatement;
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

    public void consultSalesByProduct(String productName) {
        String sql = "SELECT * FROM Fact_Sales fs JOIN Dim_Product dp ON fs.product_id = dp.product_id WHERE dp.product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productName);

            var rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("Cantidad: " + rs.getInt("quantity") + ", Precio unitario: " + rs.getDouble("unit_price") + ", Precio total: " + rs.getDouble("total_price") + ", Método de pago: " + rs.getString("payment_method"));
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar la ventas: " + e.getMessage());
        }
    }

    public void deleteSale(int saleId) {
        String sql = "DELETE FROM Fact_Sales WHERE sale_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Venta eliminada correctamente.");
            } else {
                System.out.println("No se encontró ninguna venta con el ID: " + saleId);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar la venta: " + e.getMessage());
        }
    }
}
