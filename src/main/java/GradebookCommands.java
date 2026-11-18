import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.text.DecimalFormat;

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
            newClassStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            System.err.println(e);
            System.out.println("Failed to create new class");
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
        ResultSet output;
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

                String[] row = {courseNumber, term, section, description, String.valueOf(numStudents)};
                printRow(row);
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

    public void selectClass(List<String> args) throws SQLException {
        String courseName = args.get(0);
        String term = null;
        String section = null;

        try { term = args.get(1); } catch (IndexOutOfBoundsException e) {}
        try { section = args.get(2); } catch (IndexOutOfBoundsException e) {}

        try (PreparedStatement selectClassStatement = connection.prepareStatement(
                // Fixed the column name and made term and section optional.
                "SELECT class_id, course_number, term, section_number " +
                        "FROM classes WHERE course_number = ? " +
                        "AND term = COALESCE(?, term) " +
                        "AND section_number = COALESCE(?, section_number)"
        )) {
            selectClassStatement.setString(1, courseName);

            // Use the term if one was entered.
            selectClassStatement.setString(2, term);

            // Use the section if one was entered.
            if (section != null) {
                selectClassStatement.setInt(3, Integer.parseInt(section));
            } else {
                selectClassStatement.setNull(3, java.sql.Types.INTEGER);
            }

            try (ResultSet output = selectClassStatement.executeQuery()) {

                // Check if the class exists.
                if (!output.next()) {
                    throw new IllegalArgumentException("Class not found.");
                }

                int selectedId = output.getInt("class_id");

                // Make sure only one class matches.
                if (output.next()) {
                    throw new IllegalArgumentException(
                            "Multiple classes found. Specify term and section.");
                }

                // Save the selected class.
                activeClassId = selectedId;
                System.out.println("Class selected successfully.");
            }
        }
    }

    // Displays the active class.
    public void showClass() throws SQLException {
        try {
            int selectedClass = requireActiveClass();
            try (PreparedStatement showClassStatement = connection.prepareStatement(
                    // Get the selected class and count its enrolled students.
                    "SELECT C.course_number, C.term, C.section_number, " +
                            "COUNT(E.class_id) AS num_students " +
                            "FROM classes C " +
                            "LEFT JOIN enrollment E ON C.class_id = E.class_id " +
                            "WHERE C.class_id = ? " +
                            "GROUP BY C.class_id"
            )) {
                showClassStatement.setInt(1, selectedClass);
                ResultSet output = showClassStatement.executeQuery();

                // Move to the first result before reading the class information.
                if (!output.next()) {
                    throw new SQLException("Selected class not found.");
                }

                String courseNumber = output.getString("course_number");
                String term = output.getString("term");
                String section = output.getString("section_number");
                int numStudents = output.getInt("num_students");

                String[] row = {courseNumber, term, section, String.valueOf(numStudents)};
                printRow(row);
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
        // Make sure a class is selected before adding a category.
        int selectedClass = requireActiveClass();
        String type = args.get(0);
        Integer weight = Integer.valueOf(args.get(1));
        // Categories need a weight greater than zero.
        if (weight <= 0) {
            throw new IllegalArgumentException(
                    "Category weight must be greater than zero.");
        }

        try (PreparedStatement newCategoryStatement = connection.prepareStatement(
                "insert into categories (category_type, category_weight, class_id) values (?, ?, ?);"
        )) {
            connection.setAutoCommit(false);
            newCategoryStatement.setString(1, type);
            newCategoryStatement.setInt(2, weight);
            newCategoryStatement.setInt(3, selectedClass); // currently selected class not active
            newCategoryStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            System.err.println(e);
            System.out.println("Failed to create new category. Make sure the category doesn't already exist for the class");
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

    // Shows the active class' categories with weights and rescaled percentage
    public void showCategories() throws SQLException {
        try {
            int selectedClass = requireActiveClass();
            try (PreparedStatement showCategoriesStatement = connection.prepareStatement(
                    "SELECT category_type, category_weight, SUM(category_weight) AS summed_weight FROM categories WHERE class_id = ?;"
            )) {
                showCategoriesStatement.setInt(1, selectedClass);
                ResultSet output = showCategoriesStatement.executeQuery();

                while (output.next()) {
                    String type = output.getString("category_type");
                    int weight = output.getInt("category_weight");
                    int summedWeight = output.getInt("summed_weight");

                    DecimalFormat df = new DecimalFormat("###.##");
                    String[] row = {type, String.valueOf(weight), df.format(weight / summedWeight * 100) + "%"};
                    printRow(row);
                }
            } catch (SQLException e) {
                System.err.println(e);
                System.out.println("Failed to show class categories");
            }
        } catch (IllegalStateException e) {
            System.out.println(e);
        }
    }

    // Adds an assignment.
    public void addAssignment(List<String> args) throws SQLException {
        String name = args.get(0);
        Integer type = Integer.valueOf(args.get(1));
        Integer points = Integer.valueOf(args.get(2));
        if (points < 0) {
            System.out.println("Assignment points must be non-negative");
            return;
        }
        String description = args.get(3);

        try (PreparedStatement newAssignmentStatement = connection.prepareStatement(
                "insert into assignments (assignment_name, assignment_description, points, class_id, category_id) values (?, ?, ?, ?, ?);"
        )) {
            connection.setAutoCommit(false);
            newAssignmentStatement.setString(1, name);
            newAssignmentStatement.setString(2, description);
            newAssignmentStatement.setInt(3, points);
            newAssignmentStatement.setInt(4, activeClassId);
            newAssignmentStatement.setInt(5, type);
            newAssignmentStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            System.out.println(e);
            System.out.println("Failed to create new assignment");
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

    // Shows the classes categories with weights and rescaled percentage
    public void showAssignments() throws SQLException {
        try {
            int selectedClass = requireActiveClass();
            try (PreparedStatement showAssignmentsStatement = connection.prepareStatement(
                    "SELECT A.assignment_name, A.assignment_description, A.points, C.category_type, SUM(A.points) AS summed_points FROM assignments A JOIN categories C ON A.category_id = C.category_id WHERE A.class_id = ? GROUP BY C.category_type;"
            )) {
                showAssignmentsStatement.setInt(1, selectedClass);
                ResultSet output = showAssignmentsStatement.executeQuery();

                while (output.next()) {
                    String name = output.getString("assignment_name");
                    String description = output.getString("assignment_description");
                    int points = output.getInt("points");
                    int summedPoints = output.getInt("summed_points");
                    String category = output.getString("category_type");

                    String[] row = {name, description, String.valueOf(points), category, String.valueOf(summedPoints)};
                    printRow(row);
                }
            } catch (SQLException e) {
                System.err.println(e);
                System.out.println("Failed to show class assignments");
            }
        } catch (IllegalStateException e) {
            System.out.println(e);
        }
    }

    // Creates or enrolls a student.
    public void addStudent(List<String> args) throws SQLException {
        String name = args.get(0);
        Integer id = null;
        String last = null;
        String first = null;
        try {
            id = Integer.valueOf(args.get(1));
            last = args.get(2);
            first = args.get(3);
        } catch (IndexOutOfBoundsException e) {}

        // if statement to seperate between the two different add-student behaviours
        if (first != null) {
            // add-student case where a new student is being created
            try (PreparedStatement newStudentStatement = connection.prepareStatement(
                    "insert into students (student_id, username, student_name) values (?, ?, ? ?);"
            )) {
                connection.setAutoCommit(false);
                newStudentStatement.setString(1, name);
                newStudentStatement.setInt(2, id);
                newStudentStatement.setString(3, first);
                newStudentStatement.setString(4, last);
                newStudentStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                System.out.println(e);
                System.out.println("Failed to create new student");
                if (connection != null) {
                    try {
                        System.err.print("Transaction is being rolled back");
                        connection.rollback();
                    } catch (SQLException excep) {
                        System.out.println(e + "\nTransaction Rollback Failed");
                    }
                }
            }
        } else {
            // add-student case where a student is added to a class
            // Derive student_id from username
            PreparedStatement getId = connection.prepareStatement(
                    "SELECT student_id FROM students WHERE username = ?"
            );
            getId.setString(1, name);
            ResultSet output = getId.executeQuery();
            id = output.getInt("student_id");
            // Enroll student
            try (PreparedStatement newStudentStatement = connection.prepareStatement(
                    "insert into enrollment (student_id, class_id) values (?, ?);"
            )) {
                connection.setAutoCommit(false);
                newStudentStatement.setInt(1, id);
                newStudentStatement.setInt(2, activeClassId);
                newStudentStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                System.out.println(e);
                System.out.println("Failed to enroll student");
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
    }

    // Checks whether a class is selected.
    private int requireActiveClass() {
        if (activeClassId == null) {
            throw new IllegalStateException("No class selected.");
        }

        return activeClassId;
    }

    // Stylizes the table output
    private void printRow(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                System.out.print(" | ");
            }
            System.out.print(args[i]);
        }
        System.out.println();
    }
}