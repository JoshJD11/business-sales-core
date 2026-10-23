package io.github.joshua.supplier;
import io.github.joshua.database.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import io.github.joshua.util.QueryResultPresenter;
import io.github.joshua.util.QueryResult;


public class SupplierService {

    private Scanner scanner;

    public SupplierService() {
        this.scanner = new Scanner(System.in);
    }

    public void insertSupplier(String supplierName, String contactName, String phone, String email) throws SQLException {
        String sql = "INSERT INTO Dim_Supplier (supplier_name, contact_name, phone, email) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, supplierName);
            pstmt.setString(2, contactName);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);

            pstmt.executeUpdate();
            System.out.println("Proveedor insertado correctamente.");
        }
    }

    private void consultAllSuppliers() {
        try {
            QueryResultPresenter.present(queryAllSuppliers());
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el proveedor: " + e.getMessage());
        }
    }

    public QueryResult queryAllSuppliers() throws SQLException { return query("SELECT * FROM Dim_Supplier", null); }
    public QueryResult querySupplierByEmail(String email) throws SQLException { return query("SELECT * FROM Dim_Supplier WHERE email = ?", email); }
    public QueryResult querySupplierByProduct(String productName) throws SQLException { return query("SELECT DISTINCT s.supplier_id, s.supplier_name, s.contact_name, s.phone, s.email FROM Dim_Supplier s JOIN Fact_MaterialExpenses fme ON s.supplier_id = fme.supplier_id JOIN Dim_Product dp ON fme.product_id = dp.product_id WHERE dp.product_name = ?", productName); }
    private QueryResult query(String sql, String value) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            if (value != null) statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }

    private void consultSupplierByEmail(String email) {
        try {
            QueryResultPresenter.present(querySupplierByEmail(email));
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el proveedor: " + e.getMessage());
        }
    }

    private void consultSupplierByProduct(String productName) {
        try {
            QueryResultPresenter.present(querySupplierByProduct(productName));
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el proveedor por producto: " + e.getMessage());
        }
    }

    public void updateSupplier(String email, String newSupplierName, String newContactName, String newPhone) throws SQLException {
        String sql = "UPDATE Dim_Supplier SET supplier_name = ?, contact_name = ?, phone = ? WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newSupplierName);
            pstmt.setString(2, newContactName);
            pstmt.setString(3, newPhone);
            pstmt.setString(4, email);

            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("No se encontró ningún proveedor con el correo: " + email);
            }
            System.out.println("Proveedor actualizado correctamente.");
        }
    }

    public void updateSupplier(int supplierId, String newSupplierName, String newContactName, String newPhone, String newEmail) throws SQLException {
        String sql = "UPDATE Dim_Supplier SET supplier_name = ?, contact_name = ?, phone = ?, email = ? WHERE supplier_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newSupplierName);
            pstmt.setString(2, newContactName);
            pstmt.setString(3, newPhone);
            pstmt.setString(4, newEmail);
            pstmt.setInt(5, supplierId);

            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("No se encontró ningún proveedor con ID: " + supplierId);
            }
            System.out.println("Proveedor actualizado correctamente.");
        }
    }

    public void deleteSupplierByEmail(String email) throws SQLException {
        delete("DELETE FROM Dim_Supplier WHERE email = ?", email, "No se encontró ningún proveedor con el correo: " + email);
    }

    public void deleteSupplier(int supplierId) throws SQLException {
        delete("DELETE FROM Dim_Supplier WHERE supplier_id = ?", supplierId, "No se encontró ningún proveedor con ID: " + supplierId);
    }

    private void delete(String sql, Object key, String notFoundMessage) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, key);

            if (pstmt.executeUpdate() == 0) {
                throw new SQLException(notFoundMessage);
            }
            System.out.println("Proveedor eliminado correctamente.");
        }
    }

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Insertar proveedor");
            System.out.println("2. Consultar todos los proveedores registrados");
            System.out.println("3. Consultar proveedor por correo");
            System.out.println("4. Consultar proovedor por nombre de producto");
            System.out.println("5. Actualizar proveedor");
            System.out.println("6. Eliminar proveedor");
            System.out.println("7. Regresar");
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
                    try {
                        insertSupplier(supplierName, contactName, supplierPhone, newSupplierEmail);
                    } catch (SQLException e) {
                        System.out.println("Error al insertar el proveedor: " + e.getMessage());
                    }
                    break;
                
                case "2":
                    consultAllSuppliers();
                    break;
                
                case "3":
                    System.out.print("Ingrese el correo del proveedor: ");
                    String emailToConsult = scanner.nextLine();
                    consultSupplierByEmail(emailToConsult);
                    break;

                case "4":
                    System.out.print("Ingrese el nombre del producto para consultar su proveedor: ");
                    String productNameForSupplier = scanner.nextLine();
                    consultSupplierByProduct(productNameForSupplier);
                    break;

                case "5":
                    System.out.print("Ingrese el email del proveedor");
                    String supplierEmail = scanner.nextLine();
                    System.out.print("Ingresar nombre del proveedor");
                    String supplierNewName = scanner.nextLine();
                    System.out.print("Ingrese el contacto del proveedor");
                    String supplierNewContact = scanner.nextLine();
                    System.out.print("Ingrese el número de teléfono del proveedor");
                    String supplierNewPhone = scanner.nextLine();
                    try {
                        updateSupplier(supplierEmail, supplierNewName, supplierNewContact, supplierNewPhone);
                    } catch (SQLException e) {
                        System.out.println("Error al actualizar el proveedor: " + e.getMessage());
                    }
                    break;

                case "6":
                    System.out.print("Ingrese el correo del proveedor a eliminar: ");
                    String emailToDelete = scanner.nextLine();
                    try {
                        deleteSupplierByEmail(emailToDelete);
                    } catch (SQLException e) {
                        System.out.println("Error al eliminar el proveedor: " + e.getMessage());
                    }
                    break;

                case "7":
                    exit = true;
                    break;
                
                default:
                    System.out.println("Opción inválida. Por favor, seleccione una opción válida.");
                    break;
            }
        }
    }

}
