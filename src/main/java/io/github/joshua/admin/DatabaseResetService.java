package io.github.joshua.admin;

import io.github.joshua.database.DBConnection;
import java.sql.*;
import java.util.Scanner;

public class DatabaseResetService {

    private final Scanner scanner = new Scanner(System.in);

    public void resetDatabase() {
        System.out.println("⚠️  Esto va a BORRAR TODOS LOS DATOS de todas las tablas. Esta acción no se puede deshacer.");
        System.out.print("Escribe 'CONFIRMAR' (en mayúsculas) para continuar: ");
        String confirmation = scanner.nextLine();

        if (!confirmation.equals("CONFIRMAR")) {
            System.out.println("Operación cancelada.");
            return;
        }

        resetDatabaseConfirmed();
    }

    public void resetDatabaseConfirmed() {
        String[] dropStatements = {
            "IF OBJECT_ID('Fact_Sales', 'U') IS NOT NULL DROP TABLE Fact_Sales;",
            "IF OBJECT_ID('Fact_MaterialExpenses', 'U') IS NOT NULL DROP TABLE Fact_MaterialExpenses;",
            "IF OBJECT_ID('Inventory', 'U') IS NOT NULL DROP TABLE Inventory;",
            "IF OBJECT_ID('Dim_Customer', 'U') IS NOT NULL DROP TABLE Dim_Customer;",
            "IF OBJECT_ID('Dim_Supplier', 'U') IS NOT NULL DROP TABLE Dim_Supplier;",
            "IF OBJECT_ID('Dim_ExpenseCategory', 'U') IS NOT NULL DROP TABLE Dim_ExpenseCategory;",
            "IF OBJECT_ID('Dim_Product', 'U') IS NOT NULL DROP TABLE Dim_Product;",
            "IF OBJECT_ID('Dim_Date', 'U') IS NOT NULL DROP TABLE Dim_Date;"
        };

        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement()) {

            for (String drop : dropStatements) {
                stmt.execute(drop);
            }

            System.out.println("Base de datos vaciada correctamente.");

        } catch (SQLException e) {
            System.out.println("Error al vaciar la base de datos: " + e.getMessage());
        }
    }
}