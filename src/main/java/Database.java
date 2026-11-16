import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    // Establishes a connection to the MySQL database.
    public static Connection connect() throws SQLException {

        String url = System.getenv().getOrDefault(
                "DB_URL", "jdbc:mysql://localhost:3306/gradebook");

        String user = System.getenv().getOrDefault(
                "DB_USER", "root");

        String password = System.getenv("DB_PASSWORD");

        if (password == null) {
            throw new SQLException("Database password is not configured.");
        }

        return DriverManager.getConnection(url, user, password);
    }
}