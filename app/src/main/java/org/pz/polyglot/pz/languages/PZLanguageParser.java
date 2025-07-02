package org.pz.polyglot.pz.languages;

import java.nio.charset.Charset;
import java.util.Optional;

public final class PZLanguageParser {
    public record PZLanguageDescriptor(String text, Charset charset) {
    }

    /**
     * Parses the given file content and returns a Language object if all required
     * fields are present and all lines are valid (key=value, ending with a comma).
     * Comments are stripped before parsing.
     * Only VERSION, text, and charset are required. All other keys and lines are
     * ignored.
     * 
     * @param content the file content as a String
     * @return Language object if parsing is successful, otherwise null
     */
    public static Optional<PZLanguageDescriptor> parse(String code, String content) {
        if (content == null) {
            return Optional.empty();
        }

        // Remove block comments (/* ... */) and line comments (# ...)
        content = stripComments(content);

        String text = null;
        String charset = null;

        String[] lines = content.split("\n");

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            // Must contain '=' and end with ','
            if (!line.contains("=") || !line.endsWith(",")) {
                return null; // fail validation if any line is invalid
            }

            // Remove trailing comma
            line = line.substring(0, line.length() - 1).trim();

            int eq = line.indexOf('=');
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();

            switch (key) {
                case "text":
                    text = value;
                    break;
                case "charset":
                    charset = value;
                    break;
                default:
                    // ignore unknown keys
            }
        }

        if (text == null || charset == null) {
            return null;
        }

        return Optional.of(new PZLanguageDescriptor(text, Charset.forName(charset)));
    }

    /**
     * Removes block comments (/* ... *​/) and line comments (# ...)
     */
    private static String stripComments(String input) {
        // Remove block comments (handles multi-line)
        String noBlockComments = input.replaceAll("(?s)/\\*.*?\\*/", "");
        // Remove line comments starting with #
        StringBuilder sb = new StringBuilder();
        for (String line : noBlockComments.split("\\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#"))
                continue;
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
}
