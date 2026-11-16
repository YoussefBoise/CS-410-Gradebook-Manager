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
        courseName = args.get(0);
        term = args.get(1);
        section = args.get(2);
        description = args.get(3);

        try (PreparedStatement newClassStatement = con.prepareStatement(
                "insert into classes (course_number, term, section_number, class_description) values (?, ?, ?, ?);"
        )) {
            con.setAutoCommit(false);
            newClassStatement.setString(1, courseName);
            newClassStatement.setString(2, term);
            newClassStatement.setString(3, section);
            newClassStatement.setString(4, description);
            con.commit()
        } catch (SQLException e) {
            System.out.println(e);
            if (con != null) {
                try {
                    System.err.print("Transaction is being rolled back");
                    con.rollback();
                } catch (SQLException excep) {
                    System.out.println(e + "\nTransaction Rollback Failed");
                }
            }
        }
    }

    // Lists all classes.
    public void listClasses() throws SQLException {
        throw new IllegalStateException("list-classes not implemented yet.");
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