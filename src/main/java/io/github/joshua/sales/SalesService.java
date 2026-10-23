package io.github.joshua.sales;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import io.github.joshua.util.QueryResultPresenter;
import io.github.joshua.util.QueryResult;

import io.github.joshua.database.DBConnection;


public class SalesService {

    private Scanner scanner;

    public SalesService() {
        this.scanner = new Scanner(System.in);
    }

    public void consultAllSales() {
        try {
            QueryResultPresenter.present(queryAllSales());
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar las ventas: " + e.getMessage());
        }
    }

    private static final String SALES_SELECT = "SELECT fs.sale_id, fs.date_id, dp.product_name, dc.customer_name, dc.email AS customer_email, fs.quantity, fs.unit_price, fs.total_price, fs.payment_method, fs.created_at "
            + "FROM Fact_Sales fs JOIN Dim_Product dp ON fs.product_id = dp.product_id LEFT JOIN Dim_Customer dc ON fs.customer_id = dc.customer_id";

    public QueryResult queryAllSales() throws SQLException { return query(SALES_SELECT + " ORDER BY fs.sale_id", null); }
    public QueryResult querySalesByProduct(String productName) throws SQLException { return query(SALES_SELECT + " WHERE dp.product_name = ? ORDER BY fs.sale_id", productName); }
    private QueryResult query(String sql, String value) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            if (value != null) statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }

    public void insertSale(String productName, int quantity, String paymentMethod, String customerEmail) throws SQLException {
        String sql = "{CALL InsertSale(?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, productName);
            stmt.setInt(2, quantity);
            stmt.setString(3, paymentMethod);

            if (customerEmail != null && !customerEmail.isEmpty()) {
                stmt.setString(4, customerEmail);
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.execute();
            System.out.println("Venta registrada correctamente.");
        }
    }

    private void consultSalesByProduct(String productName) {
        try {
            QueryResultPresenter.present(querySalesByProduct(productName));
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar las ventas: " + e.getMessage());
        }
    }

    public void updateSale(int saleId, String productName, int quantity, String paymentMethod, String customerEmail) throws SQLException {
        String sql = "{CALL UpdateSale(?, ?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, saleId);
            stmt.setString(2, productName);
            stmt.setInt(3, quantity);
            stmt.setString(4, paymentMethod);

            if (customerEmail != null && !customerEmail.isEmpty()) {
                stmt.setString(5, customerEmail);
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.execute();
            System.out.println("Venta actualizada correctamente.");
        }
    }

    public void deleteSale(int saleId) throws SQLException {
        String sql = "{CALL DeleteSale(?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, saleId);
            stmt.execute();
            System.out.println("Venta eliminada correctamente y stock devuelto al inventario.");
        }
    }

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Registrar venta");
            System.out.println("2. Consultar todas las ventas registradas");
            System.out.println("3. Consultar ventas por producto");
            System.out.println("4. Eliminar registro de venta");
            System.out.println("5. Editar registro de venta");
            System.out.println("6. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre del producto: ");
                    String soldProductName = scanner.nextLine();
                    System.out.print("Ingrese la cantidad: ");
                    int quantity = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese el método de pago: ");
                    String customerPaymentMethod = scanner.nextLine();
                    System.out.print("Ingrese el correo del cliente, en caso de no tener uno, presione enter: ");
                    String customerEmail = scanner.nextLine();
                    try {
                        insertSale(soldProductName, quantity, customerPaymentMethod, customerEmail);
                    } catch (SQLException e) {
                        System.out.println("Error al registrar la venta: " + e.getMessage());
                    }
                    break;
                
                case "2":
                    consultAllSales();
                    break;
                
                case "3":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameForSales = scanner.nextLine();
                    consultSalesByProduct(productNameForSales);
                    break;

                case "4":
                    System.out.print("Ingrese el ID de la venta a eliminar: ");
                    int saleIdToDelete = Integer.parseInt(scanner.nextLine());
                    try {
                        deleteSale(saleIdToDelete);
                    } catch (SQLException e) {
                        System.out.println("Error al eliminar la venta: " + e.getMessage());
                    }
                    break;

                case "5":
                    System.out.print("Ingrese el ID de la venta a editar: ");
                    int saleIdToUpdate = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese el nombre del producto: ");
                    String updatedProductName = scanner.nextLine();
                    System.out.print("Ingrese la cantidad: ");
                    int updatedQuantity = Integer.parseInt(scanner.nextLine());
                    System.out.print("Ingrese el método de pago: ");
                    String updatedPaymentMethod = scanner.nextLine();
                    System.out.print("Ingrese el correo del cliente, en caso de no tener uno, presione enter: ");
                    String updatedCustomerEmail = scanner.nextLine();
                    try {
                        updateSale(saleIdToUpdate, updatedProductName, updatedQuantity, updatedPaymentMethod, updatedCustomerEmail);
                    } catch (SQLException e) {
                        System.out.println("Error al actualizar la venta: " + e.getMessage());
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
