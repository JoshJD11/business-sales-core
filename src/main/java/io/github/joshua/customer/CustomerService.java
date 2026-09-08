package io.github.joshua.customer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.io.IOException;
import java.sql.Connection;
import io.github.joshua.util.QueryResultPresenter;
import io.github.joshua.util.QueryResult;

import io.github.joshua.database.DBConnection;

public class CustomerService {

    Scanner scanner;

    public CustomerService() {
        this.scanner = new Scanner(System.in);
    }

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

    private void consultAllCustomers() {
        try {
            QueryResultPresenter.present(queryAllCustomers());
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el cliente: " + e.getMessage());
        }
    }

    public QueryResult queryAllCustomers() throws SQLException { return queryAll("SELECT * FROM Dim_Customer"); }
    public QueryResult queryCustomerByEmail(String email) throws SQLException { return query("SELECT * FROM Dim_Customer WHERE email = ?", email); }
    private QueryResult query(String sql, String value) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }
    private QueryResult queryAll(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            return QueryResult.from(resultSet);
        }
    }

    private void consultCustomerByEmail(String email) {
        try {
            QueryResultPresenter.present(queryCustomerByEmail(email));
        } catch (SQLException | IOException e) {
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

    public void init() {
        boolean exit = false;

        while(!exit) {

            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Insertar cliente");
            System.out.println("2. Consultar todos los clientes registrados");
            System.out.println("3. Consultar cliente por correo");
            System.out.println("4. Actualizar cliente");
            System.out.println("5. Eliminar cliente");
            System.out.println("6. Regresar");
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
                    consultAllCustomers();
                    break;

                case "3":
                    System.out.print("Ingrese el correo del cliente: ");
                    String customerEmailToConsult = scanner.nextLine();
                    consultCustomerByEmail(customerEmailToConsult);
                    break;

                case "4":
                    System.out.print("Ingrese el correo del cliente a actualizar: ");
                    String customerEmailToUpdate = scanner.nextLine();
                    System.out.print("Ingrese el nuevo nombre del cliente: ");
                    String newCustomerName = scanner.nextLine();
                    System.out.print("Ingrese el nuevo número de teléfono del cliente: ");
                    String newCustomerPhone = scanner.nextLine();
                    updateCustomer(customerEmailToUpdate, newCustomerName, newCustomerPhone);
                    break;
                case "5":
                    System.out.print("Ingrese el correo del cliente a eliminar: ");
                    String customerEmailToDelete = scanner.nextLine();
                    deleteCustomer(customerEmailToDelete);
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
