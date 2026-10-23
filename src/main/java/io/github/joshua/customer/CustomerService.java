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

    public void insertCustomer(String customerName, String email, String phone) throws SQLException {
        String sql = "INSERT INTO Dim_Customer (customer_name, email, phone) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customerName);
            stmt.setString(2, blankToNull(email));
            stmt.setString(3, blankToNull(phone));

            stmt.executeUpdate();
            System.out.println("Cliente insertado correctamente.");
        }
    }

    public void updateCustomer(int customerId, String newCustomerName, String newEmail, String newPhone) throws SQLException {
        String sql = "UPDATE Dim_Customer SET customer_name = ?, email = ?, phone = ? WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newCustomerName);
            stmt.setString(2, blankToNull(newEmail));
            stmt.setString(3, blankToNull(newPhone));
            stmt.setInt(4, customerId);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No se encontró el cliente con ID: " + customerId);
            }
            System.out.println("Cliente actualizado correctamente.");
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

    public void deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM Dim_Customer WHERE customer_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No se encontró el cliente con ID: " + customerId);
            }
            System.out.println("Cliente eliminado correctamente.");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
                    System.out.print("Ingrese el correo del cliente (opcional): ");
                    String customerEmailToInsert = scanner.nextLine();
                    System.out.print("Ingrese el número de teléfono del cliente (opcional): ");
                    String customerPhone = scanner.nextLine();
                    try {
                        insertCustomer(customerName, customerEmailToInsert, customerPhone);
                    } catch (SQLException e) {
                        System.out.println("Error al insertar el cliente: " + e.getMessage());
                    }
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
                    System.out.print("Ingrese el ID del cliente a actualizar (ver opción 2): ");
                    String customerIdToUpdate = scanner.nextLine();
                    System.out.print("Ingrese el nuevo nombre del cliente: ");
                    String newCustomerName = scanner.nextLine();
                    System.out.print("Ingrese el nuevo correo del cliente (opcional): ");
                    String newCustomerEmail = scanner.nextLine();
                    System.out.print("Ingrese el nuevo número de teléfono del cliente (opcional): ");
                    String newCustomerPhone = scanner.nextLine();
                    try {
                        updateCustomer(Integer.parseInt(customerIdToUpdate.trim()), newCustomerName, newCustomerEmail, newCustomerPhone);
                    } catch (NumberFormatException e) {
                        System.out.println("ID inválido. Por favor, ingrese un número entero.");
                    } catch (SQLException e) {
                        System.out.println("Error al actualizar el cliente: " + e.getMessage());
                    }
                    break;
                case "5":
                    System.out.print("Ingrese el ID del cliente a eliminar (ver opción 2): ");
                    String customerIdToDelete = scanner.nextLine();
                    try {
                        deleteCustomer(Integer.parseInt(customerIdToDelete.trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("ID inválido. Por favor, ingrese un número entero.");
                    } catch (SQLException e) {
                        System.out.println("Error al eliminar el cliente: " + e.getMessage());
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
