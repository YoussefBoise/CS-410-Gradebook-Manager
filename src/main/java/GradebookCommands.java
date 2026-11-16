import java.sql.Connection;
import java.sql.SQLException;
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
        throw new IllegalStateException("new-class not implemented yet.");
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