package io.github.joshua.supplier;
import io.github.joshua.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import com.jakewharton.fliptables.FlipTableConverters;


public class SupplierService {

    private Scanner scanner;

    public SupplierService() {
        this.scanner = new Scanner(System.in);
    }

    private void insertSupplier(String supplierName, String contactName, String phone, String email) {
        String sql = "INSERT INTO Dim_Supplier (supplier_name, contact_name, phone, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplierName);
            pstmt.setString(2, contactName);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Proveedor insertado correctamente.");
            } else {
                System.out.println("No se pudo insertar el proveedor.");
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar el proveedor: " + e.getMessage());
        }
    }

    private void consultSupplierByEmail(String email) {
        String sql = "SELECT * FROM Dim_Supplier WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            var rs = pstmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));

        } catch (SQLException e) {
            System.out.println("Error al consultar el proveedor: " + e.getMessage());
        }
    }

    private void consultSupplierByProduct(String productName) {
        String sql = "SELECT s.supplier_name, s.contact_name, s.phone, s.email FROM Dim_Supplier s JOIN Fact_MaterialExpenses fme ON s.supplier_id = fme.supplier_id JOIN Dim_Product dp ON fme.product_id = dp.product_id WHERE dp.product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);
            var rs = pstmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));

        } catch (SQLException e) {
            System.out.println("Error al consultar el proveedor por producto: " + e.getMessage());
        }
    }

    private void updateSupplier(String email, String newSupplierName, String newContactName, String newPhone) {
        String sql = "UPDATE Dim_Supplier SET supplier_name = ?, contact_name = ?, phone = ? WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newSupplierName);
            pstmt.setString(2, newContactName);
            pstmt.setString(3, newPhone);
            pstmt.setString(4, email);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Proveedor actualizado correctamente.");
            } else {
                System.out.println("No se encontró ningún proveedor con el correo: " + email);
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar el proveedor: " + e.getMessage());
        }
    }

    private void deleteSupplierByEmail(String email) {
        String sql = "DELETE FROM Dim_Supplier WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Proveedor eliminado correctamente.");
            } else {
                System.out.println("No se encontró ningún proveedor con el correo: " + email);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar el proveedor: " + e.getMessage());
        }
    }

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Insertar proveedor");
            System.out.println("2. Consultar proveedor por correo");
            System.out.println("3. Consultar proovedor por nombre de producto");
            System.out.print("4. Actualizar proveedor");
            System.out.println("5. Eliminar proveedor");
            System.out.println("6. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre del proveedor: ");
                    String supplierName = scanner.nextLine();
                    System.out.print("Ingrese el correo del proveedor: ");
                    String newSupplierEmail = scanner.nextLine();
                    System.out.print("Ingrese el número de teléfono del proveedor: ");
                    String supplierPhone = scanner.nextLine();
                    System.out.print("Ingrese el nombre del contacto del proveedor: ");
                    String contactName = scanner.nextLine();
                    insertSupplier(supplierName, contactName, supplierPhone, newSupplierEmail);
                    break;
                
                case "2":
                    System.out.print("Ingrese el correo del proveedor: ");
                    String emailToConsult = scanner.nextLine();
                    consultSupplierByEmail(emailToConsult);
                    break;

                case "3":
                    System.out.print("Ingrese el nombre del producto para consultar su proveedor: ");
                    String productNameForSupplier = scanner.nextLine();
                    consultSupplierByProduct(productNameForSupplier);
                    break;

                case "4":
                    System.out.println("Ingrese el email del proveedor");
                    String supplierEmail = scanner.nextLine();
                    System.out.println("Ingresar nombre del proveedor");
                    String supplierNewName = scanner.nextLine();
                    System.out.println("Ingrese el contacto del proveedor");
                    String supplierNewContact = scanner.nextLine();
                    System.out.println("Ingrese el número de teléfono del proveedor");
                    String supplierNewPhone = scanner.nextLine();
                    updateSupplier(supplierEmail, supplierNewName, supplierNewContact, supplierNewPhone);
                    break;

                case "5":
                    System.out.print("Ingrese el correo del proveedor a eliminar: ");
                    String emailToDelete = scanner.nextLine();
                    deleteSupplierByEmail(emailToDelete);
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
