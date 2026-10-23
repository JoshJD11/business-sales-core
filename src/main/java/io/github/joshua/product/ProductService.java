package io.github.joshua.product;
import io.github.joshua.database.DBConnection;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import io.github.joshua.util.QueryResultPresenter;
import io.github.joshua.util.QueryResult;

public class ProductService {

    private Scanner scanner;

    public ProductService() {
        this.scanner = new Scanner(System.in);
    }

    public void consultAllProducts() {
        try {
            QueryResultPresenter.present(queryAllProducts());
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el producto: " + e.getMessage());
        }
    }

    public QueryResult queryAllProducts() throws SQLException {
        return query("SELECT * FROM Dim_Product");
    }

    public QueryResult queryProductsByName(String productName) throws SQLException {
        return query("SELECT * FROM Dim_Product WHERE product_name = ?", productName);
    }

    private QueryResult query(String sql, String... values) throws SQLException {
        try (Connection conn = DBConnection.getConnection(); PreparedStatement statement = conn.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) statement.setString(index + 1, values[index]);
            try (ResultSet resultSet = statement.executeQuery()) { return QueryResult.from(resultSet); }
        }
    }

    public void insertProductAndCreateInventory(String productName, String category, String unitOfMeasure, double unitPrice) throws SQLException {
        String sql = "{CALL InsertProductAndCreateInventory(?, ?, ?, ?)}";

        try (Connection conn = DBConnection.getConnection();
            CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, productName);
            stmt.setString(2, category);
            stmt.setString(3, unitOfMeasure);
            stmt.setDouble(4, unitPrice);

            stmt.execute();
            System.out.println("Producto insertado correctamente y registro de inventario creado.");
        }
    }

    private void consultProductByName(String productName) {
        try {
            QueryResultPresenter.present(queryProductsByName(productName));
        } catch (SQLException | IOException e) {
            System.out.println("Error al consultar el producto: " + e.getMessage());
        }
    }

    public void updateProduct(int productId, String newProductName, String newCategory, String newUnitOfMeasure, double newUnitPrice) throws SQLException {
        String sql = "UPDATE Dim_Product SET product_name = ?, category = ?, unit_of_measure = ?, unit_price = ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newProductName);
            pstmt.setString(2, newCategory);
            pstmt.setString(3, newUnitOfMeasure);
            pstmt.setDouble(4, newUnitPrice);
            pstmt.setInt(5, productId);

            if (pstmt.executeUpdate() == 0) {
                throw new SQLException("No se encontró el producto con ID: " + productId);
            }
            System.out.println("Producto actualizado correctamente.");
        }
    }

    // Fact_Sales cascades on product delete, so refuse instead of silently wiping the sales history.
    public void deleteProduct(int productId) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM Fact_Sales WHERE product_id = ?";
        String sql = "DELETE FROM Dim_Product WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
                countStmt.setInt(1, productId);
                try (ResultSet rs = countStmt.executeQuery()) {
                    rs.next();
                    int sales = rs.getInt(1);
                    if (sales > 0) {
                        throw new SQLException("No se puede eliminar el producto: tiene " + sales + " venta(s) registrada(s). Elimina primero esas ventas para no perder el historial.");
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, productId);

                if (pstmt.executeUpdate() == 0) {
                    throw new SQLException("No se encontró el producto con ID: " + productId);
                }
                System.out.println("Producto e inventario del producto eliminados correctamente.");
            }
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
                    } catch (SQLException e) {
                        System.out.println("Error al insertar el producto: " + e.getMessage());
                    }
                    break;

                case "4":
                    System.out.print("Ingrese el ID del producto a actualizar: ");
                    String productIdToUpdate = scanner.nextLine();
                    System.out.print("Ingrese el nuevo nombre del producto: ");
                    String updatedProductName = scanner.nextLine();
                    System.out.print("Ingrese la nueva categoría: ");
                    String updatedCategory = scanner.nextLine();
                    System.out.print("Ingrese la nueva unidad de medida: ");
                    String updatedUnitOfMeasure = scanner.nextLine();
                    System.out.print("Ingrese el nuevo precio unitario: ");
                    String updatedUnitPriceInput = scanner.nextLine();
                    try {
                        int productId = Integer.parseInt(productIdToUpdate.trim());
                        double updatedUnitPrice = Double.parseDouble(updatedUnitPriceInput);
                        updateProduct(productId, updatedProductName, updatedCategory, updatedUnitOfMeasure, updatedUnitPrice);
                    } catch (NumberFormatException e) {
                        System.out.println("ID o precio unitario inválido. Por favor, ingrese valores válidos.");
                    } catch (SQLException e) {
                        System.out.println("Error al actualizar el producto: " + e.getMessage());
                    }
                    break;

                case "5":
                    System.out.print("Ingrese el ID del producto a eliminar (ver opción 1): ");
                    String productIdToDelete = scanner.nextLine();
                    try {
                        deleteProduct(Integer.parseInt(productIdToDelete.trim()));
                    } catch (NumberFormatException e) {
                        System.out.println("ID inválido. Por favor, ingrese un número entero.");
                    } catch (SQLException e) {
                        System.out.println("Error al eliminar el producto: " + e.getMessage());
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
