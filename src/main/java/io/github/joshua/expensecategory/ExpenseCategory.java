package io.github.joshua.expensecategory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import io.github.joshua.util.QueryResultPresenter;
import io.github.joshua.util.QueryResult;

import java.io.IOException;
import java.sql.Connection;
import io.github.joshua.database.DBConnection;

public class ExpenseCategory {

    Scanner scanner;

    public ExpenseCategory() {
        this.scanner = new Scanner(System.in);
    }

    private void consultAllCategories() {
        try {
            QueryResultPresenter.present(queryAllCategories());
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar las categorías: " + e.getMessage());
        }
    }

    public QueryResult queryAllCategories() throws SQLException { return query("SELECT * FROM Dim_ExpenseCategory", null); }
    public QueryResult queryCategoryByName(String categoryName) throws SQLException { return query("SELECT * FROM Dim_ExpenseCategory WHERE category_name = ?", categoryName); }
    public QueryResult queryExpensesByCategory(String categoryName) throws SQLException {
        return query("SELECT fme.* FROM Fact_MaterialExpenses fme JOIN Dim_ExpenseCategory ec ON fme.category_id = ec.category_id WHERE ec.category_name = ?", categoryName);
    }
    private QueryResult query(String sql, String value) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            if (value != null) statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }

    private void consultExpenseCategory(String categoryName) {
        try {
            QueryResultPresenter.present(queryCategoryByName(categoryName));
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar la categoría: " + e.getMessage());
        }
    }

    // Fact_MaterialExpenses cascades on category delete, so refuse instead of silently wiping those expenses.
    public void deleteExpenseCategory(String categoryName) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM Fact_MaterialExpenses fme JOIN Dim_ExpenseCategory ec ON fme.category_id = ec.category_id WHERE ec.category_name = ?";
        String sql = "DELETE FROM Dim_ExpenseCategory WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                countStmt.setString(1, categoryName);
                try (ResultSet rs = countStmt.executeQuery()) {
                    rs.next();
                    int expenses = rs.getInt(1);
                    if (expenses > 0) {
                        throw new SQLException("No se puede eliminar la categoría: tiene " + expenses + " gasto(s) registrado(s). Edita esos gastos para asignarles otra categoría o elimínalos primero.");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, categoryName);

                if (stmt.executeUpdate() == 0) {
                    throw new SQLException("No se encontró la categoría: " + categoryName);
                }
                System.out.println("Categoría eliminada correctamente: " + categoryName);
            }
        }
    }

    public void updateExpenseCategory(String oldCategoryName, String newCategoryName) throws SQLException {
        String sql = "UPDATE Dim_ExpenseCategory SET category_name = ? WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCategoryName);
            stmt.setString(2, oldCategoryName);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No se encontró la categoría: " + oldCategoryName);
            }
            System.out.println("Categoría actualizada correctamente de " + oldCategoryName + " a " + newCategoryName);
        }
    }

    public void insertExpenseCategory(String categoryName) throws SQLException {
        String checkSql = "SELECT 1 FROM Dim_ExpenseCategory WHERE category_name = ?";
        String insertSql = "INSERT INTO Dim_ExpenseCategory (category_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, categoryName);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        throw new SQLException("La categoría '" + categoryName + "' ya existe.");
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, categoryName);
                insertStmt.executeUpdate();
                System.out.println("Categoría de gasto '" + categoryName + "' insertada correctamente.");
            }
        }
    }

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Consultar todas las categorias registradas");
            System.out.println("2. Consultar categoría de producto");
            System.out.println("3. Insertar categoría de producto");
            System.out.println("4. Actualizar categoría de producto");
            System.out.println("5. Eliminar categoría de producto");
            System.out.println("6. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    consultAllCategories();
                    break;

                case "2":
                    System.out.print("Ingrese el nombre de la categoría a consultar: ");
                    String categoryNameToConsult = scanner.nextLine();
                    consultExpenseCategory(categoryNameToConsult);
                    break;
                
                case "3":
                    System.out.print("Ingrese el nombre de la categoría a insertar: ");
                    String categoryNameToInsert = scanner.nextLine();
                    try {
                        insertExpenseCategory(categoryNameToInsert);
                    } catch (SQLException e) {
                        System.out.println("Error al insertar la categoría de gasto: " + e.getMessage());
                    }
                    break;

                case "4":
                    System.out.print("Ingrese el nombre de la categoría a actualizar: ");
                    String categoryNameToUpdate = scanner.nextLine();
                    System.out.print("Ingrese el nuevo nombre que tendrá la categoría");
                    String newCategoryName = scanner.nextLine();
                    try {
                        updateExpenseCategory(categoryNameToUpdate, newCategoryName);
                    } catch (SQLException e) {
                        System.out.println("Error al actualizar la categoría de gasto: " + e.getMessage());
                    }
                    break;
                case "5":
                    System.out.print("Ingrese el nombre de la categoría a eliminar: ");
                    String categoryNameToDelete = scanner.nextLine();
                    try {
                        deleteExpenseCategory(categoryNameToDelete);
                    } catch (SQLException e) {
                        System.out.println("Error al eliminar la categoría de gasto: " + e.getMessage());
                    }
                    break;

                case "6":
                    exit = true;
                    break;
                
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                    break;
            }
        }
    }

}
