package io.github.joshua.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.joshua.util.AppConfig;

public class DBConnection { // Azure SQL Database connection class

    private static final int MAX_CONNECTION_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MILLIS = 2_000L;

    public static Connection getConnection() throws SQLException {
        String url = AppConfig.get("DB_URL");
        String dbName = AppConfig.get("DB_NAME");
        String user = AppConfig.get("DB_USER");
        String password = AppConfig.get("DB_PASSWORD");

        String fullUrl = url + ";database=" + dbName
                + ";encrypt=true"
                + ";trustServerCertificate=false"
                + ";hostNameInCertificate=*.database.windows.net"
                + ";loginTimeout=30"
                + ";user=" + user
                + ";password=" + password;

        SQLException lastException = null;
        for (int attempt = 1; attempt <= MAX_CONNECTION_ATTEMPTS; attempt++) {
            try {
                return DriverManager.getConnection(fullUrl);
            } catch (SQLException exception) {
                lastException = exception;
                if (attempt == MAX_CONNECTION_ATTEMPTS) {
                    break;
                }
                try {
                    Thread.sleep(RETRY_DELAY_MILLIS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("La espera para reconectar a la base de datos fue interrumpida.", interruptedException);
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }
        throw new SQLException("No se pudo abrir la conexión a la base de datos.");
    }
}