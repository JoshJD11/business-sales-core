package io.github.joshua;
// import io.github.joshua.user.UserMenu;

import java.sql.SQLException;

import io.github.joshua.database.DBConnection;

public class Main {
    public static void main(String[] args) {
        // UserMenu userMenu = new UserMenu();
        // userMenu.initialize();

        try {
            DBConnection.getConnection();
            System.out.println("Conexión a la base de datos establecida correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }
}
