package io.github.joshua.admin;

import com.jakewharton.fliptables.FlipTableConverters;
import io.github.joshua.database.DBConnection;

import java.io.IOException;
import java.sql.*;
import io.github.joshua.util.AppSettings;
import io.github.joshua.util.ExcelExportService;

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
                if (AppSettings.isExportSelectsToExcel()) {
                    ExcelExportService.exportToExcel(rs, "resultado_" + System.currentTimeMillis() + ".xlsx");
                } else {
                    System.out.println(FlipTableConverters.fromResultSet(rs));
                }
            } else {
                int rows = stmt.executeUpdate(sql);
                System.out.println("Consulta ejecutada. Filas afectadas: " + rows);
            }

        } catch (SQLException | IOException e) {
            System.out.println("Error al ejecutar la consulta: " + e.getMessage());
        }
    }

}