package org.pz.polyglot.models.languages;

import java.nio.charset.Charset;
import java.util.Optional;

/**
 * Utility class for parsing language descriptor files in Polyglot format.
 * <p>
 * This class is not intended to be instantiated.
 */
public final class PZLanguageParser {

    /**
     * Immutable record representing a parsed language descriptor.
     *
     * @param text    the language text value
     * @param charset the charset used for the language
     */
    public record PZLanguageDescriptor(String text, Charset charset) {
    }

    /**
     * Parses the given file content and returns a {@link PZLanguageDescriptor} if
     * all required fields are present and all lines are valid.
     * <p>
     * The parser expects lines in the format {@code key=value,} and ignores unknown
     * keys. Comments (block and line) are stripped before parsing.
     * Only {@code text} and {@code charset} keys are required for successful
     * parsing.
     *
     * @param code    the code (unused, reserved for future use)
     * @param content the file content as a String
     * @return an {@link Optional} containing the parsed descriptor if successful,
     *         otherwise {@link Optional#empty()} or {@code null} for invalid format
     */
    public static Optional<PZLanguageDescriptor> parse(String code, String content) {
        if (content == null) {
            return Optional.empty();
        }

        // Remove comments before parsing
        content = stripComments(content);

        String text = null;
        String charset = null;

        String[] lines = content.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            // Validate line format: must contain '=' and end with ','
            if (!line.contains("=") || !line.endsWith(",")) {
                return null; // fail validation if any line is invalid
            }
            // Remove trailing comma
            line = line.substring(0, line.length() - 1).trim();
            int eq = line.indexOf('=');
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            switch (key) {
                case "text" -> text = value;
                case "charset" -> charset = value;
                default -> {
                    // Unknown keys are ignored
                }
            }
        }

        // Both required fields must be present
        if (text == null || charset == null) {
            return null;
        }

        return Optional.of(new PZLanguageDescriptor(text, Charset.forName(charset)));
    }

    /**
     * Removes block comments ({@code /* ... *​/}) and line comments ({@code # ...})
     * from the input string.
     * <p>
     * Block comments may span multiple lines. Line comments must start with
     * {@code #}.
     *
     * @param input the input string to process
     * @return the input string with comments removed
     */
    private static String stripComments(String input) {
        // Remove block comments (handles multi-line)
        String noBlockComments = input.replaceAll("(?s)/\\*.*?\\*/", "");
        // Remove line comments starting with #
        StringBuilder sb = new StringBuilder();
        for (String line : noBlockComments.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#")) {
                continue;
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
