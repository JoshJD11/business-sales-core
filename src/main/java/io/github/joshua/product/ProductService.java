package io.github.joshua.product;
import io.github.joshua.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ProductService {

    public void insertProduct(String productName, String category, String unitOfMeasure, double unitPrice) {
        String sql = "INSERT INTO Dim_Product (product_name, category, unit_of_measure, unit_price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);
            pstmt.setString(2, category);
            pstmt.setString(3, unitOfMeasure);
            pstmt.setDouble(4, unitPrice);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Producto insertado correctamente.");
            } else {
                System.out.println("No se pudo insertar el producto.");
            }

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
                System.out.println("Producto encontrado: " + rs.getString("product_name") + ", Categoría: " + rs.getString("category") + ", Unidad de medida: " + rs.getString("unit_of_measure") + ", Precio unitario: " + rs.getDouble("unit_price"));
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
}
