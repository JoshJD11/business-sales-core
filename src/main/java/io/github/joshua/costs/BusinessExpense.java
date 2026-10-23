package io.github.joshua.costs;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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

    private static final String EXPENSES_SELECT = "SELECT fme.expense_id, fme.date_id, ec.category_name, dp.product_name, ds.supplier_name, ds.email AS supplier_email, fme.description, fme.amount, fme.payment_method, fme.quantity, fme.created_at "
            + "FROM Fact_MaterialExpenses fme JOIN Dim_ExpenseCategory ec ON fme.category_id = ec.category_id LEFT JOIN Dim_Product dp ON fme.product_id = dp.product_id LEFT JOIN Dim_Supplier ds ON fme.supplier_id = ds.supplier_id";

    public QueryResult queryAllExpenses() throws SQLException { return query(EXPENSES_SELECT + " ORDER BY fme.expense_id", null); }
    public QueryResult queryExpensesByProduct(String productName) throws SQLException { return query(EXPENSES_SELECT + " WHERE dp.product_name = ? ORDER BY fme.expense_id", productName); }
    private QueryResult query(String sql, String value) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            if (value != null) statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }
    
    public void insertExpense(String categoryName, String productName, String supplierEmail, String description, double amount, String paymentMethod, Integer quantity) throws SQLException {
        String sql = "{CALL InsertExpense(?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, categoryName);
            setOptionalString(stmt, 2, productName);
            setOptionalString(stmt, 3, supplierEmail);
            stmt.setString(4, description);
            stmt.setDouble(5, amount);
            stmt.setString(6, paymentMethod);

            if (quantity != null) {
                stmt.setInt(7, quantity);
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.execute();
            System.out.println("Gasto registrado correctamente.");
        }
    }

    public void updateExpense(int expenseId, String categoryName, String productName, String supplierEmail, String description, double amount, String paymentMethod, Integer quantity) throws SQLException {
        String sql = "{CALL UpdateExpense(?, ?, ?, ?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, expenseId);
            stmt.setString(2, categoryName);
            setOptionalString(stmt, 3, productName);
            setOptionalString(stmt, 4, supplierEmail);
            stmt.setString(5, description);
            stmt.setDouble(6, amount);
            stmt.setString(7, paymentMethod);

            if (quantity != null) {
                stmt.setInt(8, quantity);
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.execute();
            System.out.println("Gasto actualizado correctamente.");
        }
    }

    private void setOptionalString(CallableStatement stmt, int index, String value) throws SQLException {
        if (value != null && !value.isBlank()) {
            stmt.setString(index, value.trim());
        } else {
            stmt.setNull(index, Types.VARCHAR);
        }
    }

    private void consultExpenseByProduct(String productName) {
        try {
            QueryResultPresenter.present(queryExpensesByProduct(productName));
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el gasto: " + e.getMessage());
        }
    }

    public void deleteExpense(int expenseId) throws SQLException {
        String sql = "DELETE FROM Fact_MaterialExpenses WHERE expense_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, expenseId);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No se encontró el gasto con ID: " + expenseId);
            }
            System.out.println("Gasto eliminado correctamente.");
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
            System.out.println("5. Editar registro de gasto");
            System.out.println("6. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre de la categoría: ");
                    String categoryName = scanner.nextLine();
                    System.out.print("Ingrese el nombre del producto (opcional): ");
                    String productName = scanner.nextLine();
                    System.out.print("Ingrese el correo del proveedor (opcional): ");
                    String supplierEmail = scanner.nextLine();
                    System.out.print("Ingrese la descripción del gasto: ");
                    String description = scanner.nextLine();
                    System.out.print("Ingrese el monto del gasto: ");
                    double amount = Double.parseDouble(scanner.nextLine());
                    System.out.print("Ingrese el método de pago: ");
                    String paymentMethod = scanner.nextLine();
                    System.out.print("Ingrese la cantidad (opcional, presione enter para omitir): ");
                    String quantityInput = scanner.nextLine().trim();
                    Integer productQuantity = quantityInput.isEmpty() ? null : Integer.parseInt(quantityInput);
                    try {
                        insertExpense(categoryName, productName, supplierEmail, description, amount, paymentMethod, productQuantity);
                    } catch (SQLException e) {
                        System.out.println("Error al registrar el gasto: " + e.getMessage());
                    }
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
                    try {
                        deleteExpense(expenseIdToDelete);
                    } catch (SQLException e) {
                        System.out.println("Error al eliminar el gasto: " + e.getMessage());
                    }
                    break;

                case "5":
                    System.out.print("Ingrese el ID del gasto a editar: ");
                    int expenseIdToUpdate = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese el nombre de la categoría: ");
                    String updatedCategoryName = scanner.nextLine();
                    System.out.print("Ingrese el nombre del producto (opcional): ");
                    String updatedProductName = scanner.nextLine();
                    System.out.print("Ingrese el correo del proveedor (opcional): ");
                    String updatedSupplierEmail = scanner.nextLine();
                    System.out.print("Ingrese la descripción del gasto: ");
                    String updatedDescription = scanner.nextLine();
                    System.out.print("Ingrese el monto del gasto: ");
                    double updatedAmount = Double.parseDouble(scanner.nextLine());
                    System.out.print("Ingrese el método de pago: ");
                    String updatedPaymentMethod = scanner.nextLine();
                    System.out.print("Ingrese la cantidad (opcional, presione enter para omitir): ");
                    String updatedQuantityInput = scanner.nextLine().trim();
                    Integer updatedQuantity = updatedQuantityInput.isEmpty() ? null : Integer.parseInt(updatedQuantityInput);
                    try {
                        updateExpense(expenseIdToUpdate, updatedCategoryName, updatedProductName, updatedSupplierEmail, updatedDescription, updatedAmount, updatedPaymentMethod, updatedQuantity);
                    } catch (SQLException e) {
                        System.out.println("Error al actualizar el gasto: " + e.getMessage());
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
