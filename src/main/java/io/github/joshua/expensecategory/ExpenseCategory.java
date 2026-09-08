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
        return query("SELECT fme.* FROM Fact_MaterialExpenses fme JOIN Dim_ExpenseCategory dec ON fme.category_id = dec.category_id WHERE dec.category_name = ?", categoryName);
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

    public void deleteExpenseCategory(String categoryName) {
        String sql = "DELETE FROM Dim_ExpenseCategory WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, categoryName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Categoría eliminada correctamente: " + categoryName);
            } else {
                System.out.println("No se encontró la categoría: " + categoryName);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar los gastos por categoría: " + e.getMessage());
        }
    }

    public void updateExpenseCategory(String oldCategoryName, String newCategoryName) {
        String sql = "UPDATE Dim_ExpenseCategory SET category_name = ? WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCategoryName);
            stmt.setString(2, oldCategoryName);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Categoría actualizada correctamente de " + oldCategoryName + " a " + newCategoryName);
            } else {
                System.out.println("No se encontró la categoría: " + oldCategoryName);
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar la categoría de gasto: " + e.getMessage());
        }
    }

    public void insertExpenseCategory(String categoryName) {
        String checkSql = "SELECT 1 FROM Dim_ExpenseCategory WHERE category_name = ?";
        String insertSql = "INSERT INTO Dim_ExpenseCategory (category_name) VALUES (?)";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, categoryName);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("La categoría '" + categoryName + "' ya existe.");
                        return;
                    }
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, categoryName);
                insertStmt.executeUpdate();
                System.out.println("Categoría de gasto '" + categoryName + "' insertada correctamente.");
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar la categoría de gasto: " + e.getMessage());
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
                    insertExpenseCategory(categoryNameToInsert);
                    break;

                case "4":
                    System.out.print("Ingrese el nombre de la categoría a actualizar: ");
                    String categoryNameToUpdate = scanner.nextLine();
                    System.out.print("Ingrese el nuevo nombre que tendrá la categoría");
                    String newCategoryName = scanner.nextLine();
                    updateExpenseCategory(categoryNameToUpdate, newCategoryName);
                    break;
                case "5":
                    System.out.print("Ingrese el nombre de la categoría a eliminar: ");
                    String categoryNameToDelete = scanner.nextLine();
                    deleteExpenseCategory(categoryNameToDelete);
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
