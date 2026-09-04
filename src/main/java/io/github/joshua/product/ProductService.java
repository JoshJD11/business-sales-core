package io.github.joshua.product;
import io.github.joshua.database.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.jakewharton.fliptables.FlipTableConverters;

public class ProductService {

    public void insertProductAndCreateInventory(String productName, String category, String unitOfMeasure, double unitPrice) {
        String sql = "{CALL InsertProductAndCreateInventory(?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, productName);
            stmt.setString(2, category);
            stmt.setString(3, unitOfMeasure);
            stmt.setDouble(4, unitPrice);

            stmt.execute();
            System.out.println("Producto insertado correctamente y registro de inventario creado.");

        } catch (SQLException e) {
            System.out.println("Error al insertar el producto: " + e.getMessage());
        }
    }

    public void consultProductByName(String productName) {
        String sql = "SELECT * FROM Dim_Product WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);
            var rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println(FlipTableConverters.fromResultSet(rs));
            } else {
                System.out.println("No se encontró ningún producto con el nombre: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar el producto: " + e.getMessage());
        }
    }

    public void updateProduct(String productName, String newCategory, String newUnitOfMeasure, double newUnitPrice) {
        String sql = "UPDATE Dim_Product SET category = ?, unit_of_measure = ?, unit_price = ? WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newCategory);
            pstmt.setString(2, newUnitOfMeasure);
            pstmt.setDouble(3, newUnitPrice);
            pstmt.setString(4, productName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Producto actualizado correctamente.");
            } else {
                System.out.println("No se encontró el producto: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar el producto: " + e.getMessage());
        }
    }

    public void deleteProduct(String productName) {
        String sql = "DELETE FROM Dim_Product WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Producto eliminado correctamente.");
            } else {
                System.out.println("No se encontró el producto: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar el producto: " + e.getMessage());
        }
    }
}
