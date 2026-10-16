package ni.edu.uam.proyectocorte2.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USER");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (URL == null || USER == null || PASSWORD == null) {
            throw new SQLException("Faltan las variables de entorno DB_URL, DB_USER o DB_PASSWORD.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}