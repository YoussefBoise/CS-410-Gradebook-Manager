import java.util.ArrayList;
import java.util.List;

public class CommandParser {

    // Splits input into arguments while preserving quoted text.
    public static List<String> parse(String input) {

        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean inQuotes = false;
        boolean tokenStarted = false;

        for (char c : input.toCharArray()) {

            if (c == '"') {
                inQuotes = !inQuotes;
                tokenStarted = true;

            } else if (Character.isWhitespace(c) && !inQuotes) {

                if (tokenStarted) {
                    arguments.add(current.toString());
                    current.setLength(0);
                    tokenStarted = false;
                }

            } else {
                current.append(c);
                tokenStarted = true;
            }
        }

        if (inQuotes) {
            throw new IllegalArgumentException(
                    "Missing closing quote.");
        }

        if (tokenStarted) {
            arguments.add(current.toString());
        }

        return arguments;
    }

}