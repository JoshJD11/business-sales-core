package io.github.joshua.customer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import com.jakewharton.fliptables.FlipTableConverters;

import io.github.joshua.database.DBConnection;

public class CustomerService {
    public void insertCustomer(String customerName, String email, String phone) {
        String sql = "INSERT INTO Dim_Customer (customer_name, email, phone) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerName);
            stmt.setString(2, email);
            stmt.setString(3, phone);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cliente insertado correctamente.");
            } else {
                System.out.println("No se pudo insertar el cliente.");
            }

        } catch (SQLException e) {
            System.out.println("Error al insertar el cliente: " + e.getMessage());
        }
    }

    public void updateCustomer(String email, String newCustomerName, String newPhone) {
        String sql = "UPDATE Dim_Customer SET customer_name = ?, phone = ? WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCustomerName);
            stmt.setString(2, newPhone);
            stmt.setString(3, email);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cliente actualizado correctamente.");
            } else {
                System.out.println("No se encontró el cliente con el correo: " + email);
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar el cliente: " + e.getMessage());
        }
    }

    public void consultCustomerByEmail(String email) {
        String sql = "SELECT * FROM Dim_Customer WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));

        } catch (SQLException e) {
            System.out.println("Error al consultar el cliente: " + e.getMessage());
        }
    }

    public void deleteCustomer(String email) {
        String sql = "DELETE FROM Dim_Customer WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Cliente eliminado correctamente.");
            } else {
                System.out.println("No se encontró el cliente con el correo: " + email);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar el cliente: " + e.getMessage());
        }
    }
}
