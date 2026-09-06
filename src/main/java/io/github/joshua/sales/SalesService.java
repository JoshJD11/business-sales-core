package io.github.joshua.sales;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Scanner;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import io.github.joshua.database.DBConnection;
import com.jakewharton.fliptables.FlipTableConverters;


public class SalesService {

    private Scanner scanner;

    public SalesService() {
        this.scanner = new Scanner(System.in);
    }

    public void consultAllSales() {
        String sql = "SELECT * FROM Fact_Sales";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));

        } catch (SQLException e) {
            System.out.println("Error al consultar las ventas: " + e.getMessage());
        }
    }

    private void insertSale(String productName, int quantity, String paymentMethod, String customerEmail) {
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

        } catch (SQLException e) {
            System.out.println("Error al registrar la venta: " + e.getMessage());
        }
    }

    private void consultSalesByProduct(String productName) {
        String sql = "SELECT * FROM Fact_Sales fs JOIN Dim_Product dp ON fs.product_id = dp.product_id WHERE dp.product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, productName);

            ResultSet rs = stmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));

        } catch (SQLException e) {
            System.out.println("Error al consultar las ventas: " + e.getMessage());
        }
    }

    private void deleteSale(int saleId) {
        String sql = "DELETE FROM Fact_Sales WHERE sale_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, saleId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Venta eliminada correctamente.");
            } else {
                System.out.println("No se encontró ninguna venta con el ID: " + saleId);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar la venta: " + e.getMessage());
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
            System.out.println("5. Regresar");
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
                    insertSale(soldProductName, quantity, customerPaymentMethod, customerEmail);
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
                    deleteSale(saleIdToDelete);
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
