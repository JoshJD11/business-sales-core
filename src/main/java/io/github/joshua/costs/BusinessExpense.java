package io.github.joshua.costs;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import io.github.joshua.database.DBConnection;


public class BusinessExpense {
    
    public void insertExpense(String categoryName, String productName, String supplierEmail, String description, double amount, String paymentMethod, int quantity) {
        String sql = "CALL InsertExpense(?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName);
            stmt.setString(2, productName);
            stmt.setString(3, supplierEmail);
            stmt.setString(4, description);
            stmt.setDouble(5, amount);
            stmt.setString(6, paymentMethod);
            stmt.setInt(7, quantity);

            stmt.executeUpdate();
            System.out.println("Gasto registrado correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al registrar el gasto: " + e.getMessage());
        }
    }

    public void consultExpenseByProduct(String productName) {
        String sql = "SELECT * FROM Fact_MaterialExpenses WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productName);

            var rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Gasto encontrado: " + rs.getString("description") + ", Monto: " + rs.getDouble("amount") + ", Método de pago: " + rs.getString("payment_method"));
            } else {
                System.out.println("No se encontró ningún gasto para el producto: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar el gasto: " + e.getMessage());
        }
    }
}
