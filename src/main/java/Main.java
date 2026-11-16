import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {

    // Runs the interactive gradebook shell.
    public static void main(String[] args) {

        try (Connection connection = Database.connect();
             Scanner scanner = new Scanner(System.in)) {

            GradebookCommands commands =
                    new GradebookCommands(connection);

            System.out.println("Connected to MySQL.");
            System.out.println("Welcome to Gradebook Manager!");
            System.out.println("Type help for available commands.");

            while (true) {

                System.out.print("gradebook> ");

                if (!scanner.hasNextLine()) {
                    break;
                }

                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                try {
                    List<String> tokens =
                            CommandParser.parse(input);

                    String command = tokens.get(0).toLowerCase();

                    List<String> arguments =
                            tokens.subList(1, tokens.size());

                    switch (command) {

                        case "new-class":
                            requireArgs(arguments, 4, 4);
                            commands.newClass(arguments);
                            break;

                        case "list-classes":
                            requireArgs(arguments, 0, 0);
                            commands.listClasses();
                            break;

                        case "select-class":
                            requireArgs(arguments, 1, 3);
                            commands.selectClass(arguments);
                            break;

                        case "show-class":
                            requireArgs(arguments, 0, 0);
                            commands.showClass();
                            break;

                        case "add-category":
                            requireArgs(arguments, 2, 2);
                            commands.addCategory(arguments);
                            break;

                        case "add-assignment":
                            requireArgs(arguments, 4, 4);
                            commands.addAssignment(arguments);
                            break;

                        case "add-student":
                            if (arguments.size() != 1
                                    && arguments.size() != 4) {

                                throw new IllegalArgumentException(
                                        "Expected 1 or 4 arguments.");
                            }

                            commands.addStudent(arguments);
                            break;

                        case "help":
                            printHelp();
                            break;

                        case "quit":
                        case "exit":
                            System.out.println("Goodbye!");
                            return;

                        default:
                            System.out.println(
                                    "error: Unknown command.");
                    }

                } catch (SQLException |
                         IllegalArgumentException |
                         IllegalStateException e) {

                    System.out.println("error: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.out.println("error: " + e.getMessage());
        }
    }

    // Checks the number of command arguments.
    private static void requireArgs(
            List<String> args, int min, int max) {

        if (args.size() < min || args.size() > max) {

            throw new IllegalArgumentException(
                    "Invalid arguments. Type help for usage.");
        }
    }

    // Displays the supported commands.
    private static void printHelp() {

        System.out.println("Available commands:");

        System.out.println(
                "new-class <course> <term> <section> \"description\"");

        System.out.println("list-classes");

        System.out.println(
                "select-class <course> [term] [section]");

        System.out.println("show-class");

        System.out.println("add-category <name> <weight>");

        System.out.println(
                "add-assignment <name> <category> <points> \"description\"");

        System.out.println(
                "add-student <username> <student-id> <last> <first>");

        System.out.println("add-student <username>");

        System.out.println("help");
        System.out.println("quit / exit");
    }
}