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

        try {
            resetDatabaseConfirmed();
        } catch (SQLException e) {
            System.out.println("Error al vaciar la base de datos: " + e.getMessage());
        }
    }

    public void resetDatabaseConfirmed() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                deleteAllRows(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        System.out.println("Base de datos vaciada correctamente.");
    }

    // Deletes every row (children before parents) and restarts identity counters; tables and procedures are kept.
    public void deleteAllRows(Connection conn) throws SQLException {
        String[] tables = {"Fact_Sales", "Fact_MaterialExpenses", "Inventory", "Dim_Customer", "Dim_Supplier", "Dim_ExpenseCategory", "Dim_Product", "Dim_Date"};

        try (Statement stmt = conn.createStatement()) {
            for (String table : tables) {
                stmt.executeUpdate("DELETE FROM " + table);
                // A table whose identity was never used would start at 0 after RESEED 0, so only reseed used ones.
                stmt.execute("IF EXISTS (SELECT 1 FROM sys.identity_columns WHERE object_id = OBJECT_ID('" + table + "') AND last_value IS NOT NULL) "
                        + "DBCC CHECKIDENT ('" + table + "', RESEED, 0) WITH NO_INFOMSGS");
            }
        }
    }
}
