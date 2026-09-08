package io.github.joshua.admin;

import io.github.joshua.database.DBConnection;

import java.io.IOException;
import java.sql.*;
import io.github.joshua.util.AppSettings;
import io.github.joshua.util.ExcelExportService;
import io.github.joshua.util.QueryResult;
import io.github.joshua.util.QueryResultPresenter;

public class SqlConsoleService {

    public QueryResult query(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return QueryResult.from(rs);
        }
    }

    public void exportQuery(String sql, String fileName) throws SQLException, IOException {
        ExcelExportService.exportToExcel(query(sql), fileName);
    }

    public void executeCustomQuery(String sql) {
        String trimmedSql = sql.trim().toUpperCase();

        if (trimmedSql.startsWith("DROP") || trimmedSql.startsWith("TRUNCATE") || trimmedSql.startsWith("DELETE")) {
            System.out.println("Por seguridad, este tipo de comando no se permite desde esta opción. Usa 'Vaciar base de datos' si es intencional.");
            return;
        }

        try {
            if (trimmedSql.startsWith("SELECT")) {
                if (AppSettings.isExportSelectsToExcel()) {
                    exportQuery(sql, "resultado_" + System.currentTimeMillis() + ".xlsx");
                } else {
                    executeConsoleSelect(sql);
                }
                return;
            }

            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                int rows = stmt.executeUpdate(sql);
                System.out.println("Consulta ejecutada. Filas afectadas: " + rows);
            }

        } catch (SQLException | IOException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

    private void executeConsoleSelect(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet resultSet = stmt.executeQuery(sql)) {
            QueryResultPresenter.printTable(resultSet);
        }
    }

}