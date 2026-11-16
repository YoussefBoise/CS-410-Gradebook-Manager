import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection connection = Database.connect()) {

            System.out.println("MySQL JDBC connection successful!");

        } catch (SQLException e) {

            System.out.println("error: " + e.getMessage());
        }
    }
}