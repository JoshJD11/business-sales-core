package io.github.joshua.expensecategory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import com.jakewharton.fliptables.FlipTableConverters;
import java.sql.Connection;
import io.github.joshua.database.DBConnection;

public class ExpenseCategory {

    Scanner scanner;

    public ExpenseCategory() {
        this.scanner = new Scanner(System.in);
    }

    private void consultExpenseCategory(String categoryName) {
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

    private void deleteExpenseCategory(String categoryName) {
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

    private void updateExpenseCategory(String oldCategoryName, String newCategoryName) {
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

    private void insertExpenseCategory(String categoryName) {
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

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Consultar categoría de producto");
            System.out.println("2. Insertar categoría de producto");
            System.out.println("3. Actualizar categoría de producto");
            System.out.println("4. Eliminar categoría de producto");
            System.out.println("5. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre de la categoría a consultar: ");
                    String categoryNameToConsult = scanner.nextLine();
                    consultExpenseCategory(categoryNameToConsult);
                    break;
                
                case "2":
                    System.out.print("Ingrese el nombre de la categoría a insertar: ");
                    String categoryNameToInsert = scanner.nextLine();
                    insertExpenseCategory(categoryNameToInsert);
                    break;

                case "3":
                    System.out.print("Ingrese el nombre de la categoría a actualizar: ");
                    String categoryNameToUpdate = scanner.nextLine();
                    System.out.print("Ingrese el nuevo nombre que tendrá la categoría");
                    String newCategoryName = scanner.nextLine();
                    updateExpenseCategory(newCategoryName, categoryNameToUpdate);
                    break;
                case "4":
                    System.out.print("Ingrese el nombre de la categoría a eliminar: ");
                    String categoryNameToDelete = scanner.nextLine();
                    deleteExpenseCategory(categoryNameToDelete);
                    break;

                case "5":
                    exit = true;
                    break;
                
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                    break;
            }
        }
    }

}
