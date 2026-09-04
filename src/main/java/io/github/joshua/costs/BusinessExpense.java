package io.github.joshua.costs;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import io.github.joshua.database.DBConnection;
import com.jakewharton.fliptables.FlipTableConverters;


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
                System.out.println(FlipTableConverters.fromResultSet(rs));
            } else {
                System.out.println("No se encontró ningún gasto para el producto: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al consultar el gasto: " + e.getMessage());
        }
    }

    public void deleteExpense(int expenseId) {
        String sql = "DELETE FROM Fact_MaterialExpenses WHERE expense_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, expenseId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Gasto eliminado correctamente.");
            } else {
                System.out.println("No se encontró el gasto con ID: " + expenseId);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar el gasto: " + e.getMessage());
        }
    }
}
