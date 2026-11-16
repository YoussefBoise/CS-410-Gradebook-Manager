import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.List;

public class GradebookCommands {

    private final Connection connection;
    private Integer activeClassId;

    // Initializes the command handler.
    public GradebookCommands(Connection connection) {
        this.connection = connection;
    }

    // Creates a new class.
    public void newClass(List<String> args) throws SQLException {
        String courseName = args.get(0);
        String term = args.get(1);
        String section = args.get(2);
        String description = args.get(3);

        try (PreparedStatement newClassStatement = connection.prepareStatement(
                "insert into classes (course_number, term, section_number, class_description) values (?, ?, ?, ?);"
        )) {
            connection.setAutoCommit(false);
            newClassStatement.setString(1, courseName);
            newClassStatement.setString(2, term);
            newClassStatement.setString(3, section);
            newClassStatement.setString(4, description);
            newClassStatement.executeUpdate()
            connection.commit()
        } catch (SQLException e) {
            System.err.println(e);
            System.out.println("Failed to create new class")
            if (connection != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    connection.rollback();
                } catch (SQLException excep) {
                    System.out.println(e + "\nTransaction Rollback Failed");
                }
            }
        }
    }

    // Lists all classes.
    public void listClasses() throws SQLException {
        RowSet output;
        try (PreparedStatement newClassStatement = connection.prepareStatement(
                "SELECT C.course_number, C.term, C.section_number, C.class_description, COUNT(E.class_id) AS num_students FROM classes C LEFT JOIN enrollment E ON C.class_id = E.class_id GROUP BY C.term ASC, C.course_number ASC;"
        )) {
            output = newClassStatement.executeQuery();
            // Print output
            while(output.next()){
                String courseNumber = output.getString("course_number");
                String term = output.getString("term");
                String section = output.getString("section_number");
                String description = output.getString("class_description");
                int numStudents = output.getInt("num_students");

                // Output styling
                System.out.println(courseNumber + " | " + term + " | " + section + " | " + description + " | " + numStudents);
            }
        } catch (SQLException e) {
            System.err.println(e);
            System.out.println("Failed to list classes");
            if (connection != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    connection.rollback();
                } catch (SQLException excep) {
                    System.out.println(e + "\nTransaction Rollback Failed");
                }
            }
        }
    }

    // Selects the active class.
    public void selectClass(List<String> args) throws SQLException {
        throw new IllegalStateException("select-class not implemented yet.");
    }

    // Displays the active class.
    public void showClass() throws SQLException {
        throw new IllegalStateException("show-class not implemented yet.");
    }

    // Adds a grading category.
    public void addCategory(List<String> args) throws SQLException {
        throw new IllegalStateException("add-category not implemented yet.");
    }

    // Adds an assignment.
    public void addAssignment(List<String> args) throws SQLException {
        throw new IllegalStateException("add-assignment not implemented yet.");
    }

    // Creates or enrolls a student.
    public void addStudent(List<String> args) throws SQLException {
        throw new IllegalStateException("add-student not implemented yet.");
    }

    // Checks whether a class is selected.
    private int requireActiveClass() {
        if (activeClassId == null) {
            throw new IllegalStateException("No class selected.");
        }

        return activeClassId;
    }
}