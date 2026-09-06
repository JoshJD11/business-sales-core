package io.github.joshua.product;
import io.github.joshua.database.DBConnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import com.jakewharton.fliptables.FlipTableConverters;

public class ProductService {

    private Scanner scanner;

    public ProductService() {
        this.scanner = new Scanner(System.in);
    }

    public void consultAllProducts() {
        String sql = "SELECT * FROM Dim_Product";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            var rs = pstmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));

        } catch (SQLException e) {
            System.out.println("Error al consultar el producto: " + e.getMessage());
        }
    }

    private void insertProductAndCreateInventory(String productName, String category, String unitOfMeasure, double unitPrice) {
        String sql = "{CALL InsertProductAndCreateInventory(?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, productName);
            stmt.setString(2, category);
            stmt.setString(3, unitOfMeasure);
            stmt.setDouble(4, unitPrice);

            stmt.execute();
            System.out.println("Producto insertado correctamente y registro de inventario creado.");

        } catch (SQLException e) {
            System.out.println("Error al insertar el producto: " + e.getMessage());
        }
    }

    private void consultProductByName(String productName) {
        String sql = "SELECT * FROM Dim_Product WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);
            var rs = pstmt.executeQuery();
            System.out.println(FlipTableConverters.fromResultSet(rs));


        } catch (SQLException e) {
            System.out.println("Error al consultar el producto: " + e.getMessage());
        }
    }

    private void updateProduct(String productName, String newCategory, String newUnitOfMeasure, double newUnitPrice) {
        String sql = "UPDATE Dim_Product SET category = ?, unit_of_measure = ?, unit_price = ? WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newCategory);
            pstmt.setString(2, newUnitOfMeasure);
            pstmt.setDouble(3, newUnitPrice);
            pstmt.setString(4, productName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Producto actualizado correctamente.");
            } else {
                System.out.println("No se encontró el producto: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al actualizar el producto: " + e.getMessage());
        }
    }

    private void deleteProduct(String productName) {
        String sql = "DELETE FROM Dim_Product WHERE product_name = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, productName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Producto e inventario del producto eliminados correctamente.");
            } else {
                System.out.println("No se encontró el producto: " + productName);
            }

        } catch (SQLException e) {
            System.out.println("Error al eliminar el producto: " + e.getMessage());
        }
    }

    public void init() {

        boolean exit = false;

        while(!exit) {
            
            System.out.println("\nSeleccione una opción:");
            System.out.println("1. Consultar todos los productos");
            System.out.println("2. Consultar productos por nombre");
            System.out.println("3. Insertar producto");
            System.out.println("4. Actualizar producto");
            System.out.println("5. Eliminar producto");
            System.out.println("6. Regresar");
            System.out.print("Opción: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    consultAllProducts();
                    break;
                    
                case "2":
                    System.out.print("Ingrese el nombre del producto: ");
                    String productNameToConsult = scanner.nextLine();
                    consultProductByName(productNameToConsult);
                    break;
                
                case "3":
                    System.out.print("Ingrese el nombre del producto: ");
                    String newProductName = scanner.nextLine();
                    System.out.print("Ingrese la categoría: ");
                    String newCategory = scanner.nextLine();
                    System.out.print("Ingrese la unidad de medida: ");
                    String newUnitOfMeasure = scanner.nextLine();
                    System.out.print("Ingrese el precio unitario: ");
                    try {
                        double newUnitPrice = Double.parseDouble(scanner.nextLine());
                        insertProductAndCreateInventory(newProductName, newCategory, newUnitOfMeasure, newUnitPrice);
                    } catch (NumberFormatException e) {
                        System.out.println("Precio unitario inválido. Por favor, ingrese un número válido.");
                    }
                    break;

                case "4":
                    System.out.print("Ingrese el nombre del producto a actualizar: ");
                    String productToUpdate = scanner.nextLine();
                    System.out.print("Ingrese la nueva categoría: ");
                    String updatedCategory = scanner.nextLine();
                    System.out.print("Ingrese la nueva unidad de medida: ");
                    String updatedUnitOfMeasure = scanner.nextLine();
                    System.out.print("Ingrese el nuevo precio unitario: ");
                    try {
                        double updatedUnitPrice = Double.parseDouble(scanner.nextLine());
                        updateProduct(productToUpdate, updatedCategory, updatedUnitOfMeasure, updatedUnitPrice);
                    } catch (NumberFormatException e) {
                        System.out.println("Precio unitario inválido. Por favor, ingrese un número válido.");
                    }
                    break;

                case "5":
                    System.out.print("Ingrese el nombre del producto a eliminar: ");
                    String productToDelete = scanner.nextLine();
                    deleteProduct(productToDelete);
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
