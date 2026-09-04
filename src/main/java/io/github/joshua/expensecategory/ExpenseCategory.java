package io.github.joshua.expensecategory;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.jakewharton.fliptables.FlipTableConverters;
import java.sql.Connection;
import io.github.joshua.database.DBConnection;

public class ExpenseCategory {
        public void consultExpenseCategory(String categoryName) {
        String sql = "SELECT * FROM Fact_MaterialExpenses WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName);

            var rs = stmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));

        } catch (SQLException e) {
            System.out.println("Error al consultar los gastos por categoría: " + e.getMessage());
        }
    }

    public void deleteExpenseCategory(String categoryName) {
        String sql = "DELETE FROM Fact_MaterialExpenses WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Gastos eliminados correctamente para la categoría: " + categoryName);
            } else {
                System.out.println("No se encontraron gastos para la categoría: " + categoryName);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar los gastos por categoría: " + e.getMessage());
        }
    }

    public void updateExpenseCategory(String oldCategoryName, String newCategoryName) {
        String sql = "UPDATE Fact_MaterialExpenses SET category_name = ? WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCategoryName);
            stmt.setString(2, oldCategoryName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Categoría de gasto actualizada correctamente de " + oldCategoryName + " a " + newCategoryName);
            } else {
                System.out.println("No se encontraron gastos para la categoría: " + oldCategoryName);
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar la categoría de gasto: " + e.getMessage());
        }
    }

    public void insertExpenseCategory(String categoryName) {
    String sql = "INSERT INTO Dim_ExpenseCategory (category_name) VALUES (?)";

    try (Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, categoryName);

        int rowsAffected = stmt.executeUpdate();
        if (rowsAffected > 0) {
            System.out.println("Categoría de gasto '" + categoryName + "' insertada correctamente.");
        } else {
            System.out.println("No se pudo insertar la categoría de gasto.");
        }

    } catch (SQLException e) {
        System.out.println("Error al insertar la categoría de gasto: " + e.getMessage());
    }
}
}
