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

                printTable([course_number, term, section, description, String.valueOf(num_students)], 5);
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
        String courseName = args.get(0);
        String term = null;
        String section = null;
        try { term = args.get(1); } catch (IndexOutOfBoundsException e) {}
        try { section = args.get(2); } catch (IndexOutOfBoundsException e) {}

        try (PreparedStatement selectClassStatement = connection.prepareStatement(
                "SELECT class_id, course_number, term, section_number FROM classes WHERE course_name = ???"
        )) {
                connection.setAutoCommit(false);
                selectClassStatement.setString(1, courseName);
                if (term) {
                    selectClassStatement.setString(2, ", term = " + term);
                } else {
                    selectClassStatement.setString(2, "");
                }
                if (section) {
                    selectClassStatement.setString(3, ", section_number = " + section);
                } else {
                    selectClassStatement.setString(3, "");
                }
                RowSet output = selectClassStatement.executeQuery();
                if (output.last()) {
                    if (output.getRow() > 1) {
                        System.out.println("Failed to select class. Multiple classes match the given criteria:");
                        output.first();
                        // Print output
                        while(output.next()){
                            String courseNumber = output.getString("course_number");
                            String term = output.getString("term");
                            String section = output.getString("section_number");

                            printTable([course_number, term, section], 3);
                        }
                    } else {
                        activeClassId = output.getInt("class_id");
                    }
                }
                connection.commit()
            }
        } catch (SQLException e) {
            System.err.println(e);
            System.out.println("Failed to list classes");
        }
    }

    // Displays the active class.
    public void showClass() throws SQLException {
        try {
            int selectedClass = requireActiveClass();
            try (PreparedStatement showClassStatement = connection.prepareStatement(
                    "SELECT C.course_number, C.term, C.section_number, COUNT(E.class_id) AS num_students FROM classes C LEFT JOIN enrollment E ON C.class_id = E.class_id WHERE C.class_id = ?;"
            )) {
                showClassStatement.setInt(1, selectedClass);
                RowSet output = showClassStatement.executeQuery();

                String courseNumber = output.getString("course_number");
                String term = output.getString("term");
                String section = output.getString("section_number");
                int numStudents = output.getInt("num_students");

                printTable([course_number, term, section, String.valueOf(num_students)], 4);
            } catch (SQLException e) {
                System.err.println(e);
                System.out.println("Failed to show class");
            }
        } catch (IllegalStateException e) {
            System.out.println(e);
        }
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

    // Stylizes the table output
    private void printTable(String[] args, int numArgs) {
        for (int i = 0; i < numArgs; i++) {
            if (i) {
                System.out.print(" | ");
            }
            System.out.print(args[i]);
        }
        System.out.println();
    }
}