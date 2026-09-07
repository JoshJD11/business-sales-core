package io.github.joshua.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class DBConnection { // Azure SQL Database connection class

    public static Connection getConnection() throws SQLException {
        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("DB_URL");
        String dbName = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        String password = dotenv.get("DB_PASSWORD");

        String fullUrl = url + ";database=" + dbName
                + ";encrypt=true"
                + ";trustServerCertificate=false"
                + ";hostNameInCertificate=*.database.windows.net"
                + ";loginTimeout=10"
                + ";user=" + user
                + ";password=" + password;

        return DriverManager.getConnection(fullUrl);
    }
}