package io.github.joshua.customer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.sql.Connection;
import com.jakewharton.fliptables.FlipTableConverters;

import io.github.joshua.database.DBConnection;

public class CustomerService {

    Scanner scanner;

    public CustomerService() {
        this.scanner = new Scanner(System.in);
    }

    private void insertCustomer(String customerName, String email, String phone) {
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

    private void updateCustomer(String email, String newCustomerName, String newPhone) {
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

    private void consultCustomerByEmail(String email) {
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

    private void deleteCustomer(String email) {
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

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Insertar cliente");
            System.out.println("2. Consultar cliente por correo");
            System.out.println("3. Actualizar cliente");
            System.out.println("4. Eliminar cliente");
            System.out.println("5. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    System.out.print("Ingrese el nombre del cliente: ");
                    String customerName = scanner.nextLine();
                    System.out.print("Ingrese el correo del cliente: ");
                    String customerEmailToInsert = scanner.nextLine();
                    System.out.print("Ingrese el número de teléfono del cliente: ");
                    String customerPhone = scanner.nextLine();
                    insertCustomer(customerName, customerEmailToInsert, customerPhone);
                    break;
                
                case "2":
                    System.out.print("Ingrese el correo del cliente: ");
                    String customerEmailToConsult = scanner.nextLine();
                    consultCustomerByEmail(customerEmailToConsult);
                    break;

                case "3":
                    System.out.print("Ingrese el correo del cliente a actualizar: ");
                    String customerEmailToUpdate = scanner.nextLine();
                    System.out.print("Ingrese el nuevo nombre del cliente: ");
                    String newCustomerName = scanner.nextLine();
                    System.out.print("Ingrese el nuevo número de teléfono del cliente: ");
                    String newCustomerPhone = scanner.nextLine();
                    updateCustomer(customerEmailToUpdate, newCustomerName, newCustomerPhone);
                    break;
                case "4":
                    System.out.print("Ingrese el correo del cliente a eliminar: ");
                    String customerEmailToDelete = scanner.nextLine();
                    deleteCustomer(customerEmailToDelete);
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
