package io.github.joshua.costs;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import io.github.joshua.database.DBConnection;
import io.github.joshua.util.QueryResultPresenter;
import io.github.joshua.util.QueryResult;


public class BusinessExpense {

    private Scanner scanner;

    public BusinessExpense() {
        this.scanner = new Scanner(System.in);
    }

    private void consultAllExpenses() {
        try {
            QueryResultPresenter.present(queryAllExpenses());
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el gasto: " + e.getMessage());
        }
    }

    public QueryResult queryAllExpenses() throws SQLException { return query("SELECT * FROM Fact_MaterialExpenses", null); }
    public QueryResult queryExpensesByProduct(String productName) throws SQLException { return query("SELECT * FROM Fact_MaterialExpenses WHERE product_name = ?", productName); }
    private QueryResult query(String sql, String value) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            if (value != null) statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }
    
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

    private void consultExpenseByProduct(String productName) {
        try {
            QueryResultPresenter.present(queryExpensesByProduct(productName));
        } catch (SQLException | IOException e) {
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

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Registrar gasto");
            System.out.println("2. Consultar todos los gastos registrados");
            System.out.println("3. Consultar gastos por producto");
            System.out.println("4. Eliminar registro de gasto");
            System.out.println("5. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre de la categoría: ");
                    String categoryName = scanner.nextLine();
                    System.out.print("Ingrese el nombre del producto: ");
                    String productName = scanner.nextLine();
                    System.out.print("Ingrese el correo del proveedor: ");
                    String supplierEmail = scanner.nextLine();
                    System.out.print("Ingrese la descripción del gasto: ");
                    String description = scanner.nextLine();
                    System.out.print("Ingrese el monto del gasto: ");
                    double amount = Double.parseDouble(scanner.nextLine());
                    System.out.print("Ingrese el método de pago: ");
                    String paymentMethod = scanner.nextLine();
                    System.out.print("Ingrese la cantidad: ");
                    int productQuantity = Integer.parseInt(scanner.nextLine());
                    insertExpense(categoryName, productName, supplierEmail, description, amount, paymentMethod, productQuantity);
                    break;

                case "2":
                    consultAllExpenses();
                    break;
                
                case "3":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForExpense = scanner.nextLine();
                    consultExpenseByProduct(productNameForExpense);
                    break;

                case "4":
                    System.out.print("Ingrese el ID del gasto a eliminar: ");
                    int expenseIdToDelete = Integer.parseInt(scanner.nextLine());
                    deleteExpense(expenseIdToDelete);
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
