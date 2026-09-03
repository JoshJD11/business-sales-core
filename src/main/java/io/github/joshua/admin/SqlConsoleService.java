package io.github.joshua.admin;

import io.github.joshua.database.DBConnection;
import java.sql.*;

public class SqlConsoleService {

    public void executeCustomQuery(String sql) {
        String trimmedSql = sql.trim().toUpperCase();

        if (trimmedSql.startsWith("DROP") || trimmedSql.startsWith("TRUNCATE") || trimmedSql.startsWith("DELETE")) {
            System.out.println("Por seguridad, este tipo de comando no se permite desde esta opción. Usa 'Vaciar base de datos' si es intencional.");
            return;
        }

        try (Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement()) {

            if (trimmedSql.startsWith("SELECT")) {
                ResultSet rs = stmt.executeQuery(sql);
                printResults(rs);
            } else {
                int rows = stmt.executeUpdate(sql);
                System.out.println("Consulta ejecutada. Filas afectadas: " + rows);
            }

        } catch (SQLException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

    private void printResults(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            System.out.print(meta.getColumnName(i) + "\t");
        }
        System.out.println();

        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getString(i) + "\t");
            }
            System.out.println();
        }
    }
}