package io.github.joshua.supplier;
import io.github.joshua.database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.jakewharton.fliptables.FlipTableConverters;


public class SupplierService {

    public void insertSupplier(String supplierName, String contactName, String phone, String email) {
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

    public void consultSupplierByEmail(String email) {
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

    public void consultSupplierByProduct(String productName) {
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

    public void updateSupplier(String email, String newSupplierName, String newContactName, String newPhone) {
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

    public void deleteSupplierByEmail(String email) {
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

}
