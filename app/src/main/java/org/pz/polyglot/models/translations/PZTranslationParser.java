package org.pz.polyglot.models.translations;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.pz.polyglot.Logger;
import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.sources.PZSource;

/**
 * Parses translation files and provides an iterator over translation key-value
 * pairs.
 * Handles charset detection, comment skipping, and multiline values.
 */
public class PZTranslationParser implements AutoCloseable, Iterable<PZTranslationParser.Pair> {
    /**
     * Represents a translation entry with its key, value.
     * 
     * @param key   the translation key
     * @param value the translation value
     */
    public record Pair(String key, String value) {
    }

    /**
     * Holds the result of reading lines from a file, including the charset used.
     * 
     * @param lines   the lines read from the file
     * @param charset the charset used to decode the file
     */
    private record ReadResult(List<String> lines, Charset charset) {
    }

    /** Path to the translation file. */
    private Path path;
    /** Charsets to try for reading the file, in order of preference. */
    private final LinkedHashSet<Charset> availableCharsets;
    /** All lines read from the file. */
    private final List<String> allLines;
    /** The charset that was successfully used to read the file. */
    private final Charset usedCharset;
    /** Indicates whether the parser has been closed. */
    private boolean closed;

    /**
     * Constructs a parser for the given file, language, and source.
     * 
     * @param path     the path to the translation file
     * @param language the language configuration
     * @param source   the source configuration
     */
    public PZTranslationParser(Path path, PZLanguage language, PZSource source) {
        this.path = path;
        this.availableCharsets = language.getCharsetsDownFrom(source.getVersion());
        this.closed = false;
        var result = readAllLinesWithCorrectCharset();
        this.allLines = result.lines();
        this.usedCharset = result.charset();
    }

    /**
     * Reads all lines from the file using the first available charset that works.
     * If no charset works, returns an empty list and null charset.
     * 
     * @return the result containing lines and charset
     */
    private ReadResult readAllLinesWithCorrectCharset() {
        for (Charset charset : availableCharsets) {
            try {
                List<String> lines = Files.readAllLines(path, charset);
                return new ReadResult(lines, charset);
            } catch (IOException e) {
                // Try next charset if reading fails
                continue;
            }
        }
        Logger.warning(
                "Failed to read file with any available charset: " + path + ". Tried charsets: " + availableCharsets);
        return new ReadResult(List.of(), null); // Return empty list if no charset works
    }

    /**
     * Returns an iterator over translation pairs in the file.
     * Skips comments and header, supports multiline values.
     * 
     * @return iterator over translation pairs
     */
    @Override
    public Iterator<Pair> iterator() {
        return new Iterator<Pair>() {
            private int currentLineIndex = 0;
            private boolean multiline = false;
            private String currentKey = "";
            private StringBuilder currentValue = new StringBuilder();
            private Pair nextPair = null;
            private boolean hasNextCalled = false;

            /**
             * Finds the next translation pair in the file.
             * 
             * @return true if a next pair is available
             */
            @Override
            public boolean hasNext() {
                if (closed)
                    return false;
                if (hasNextCalled)
                    return nextPair != null;

                while (currentLineIndex < allLines.size()) {
                    String line = allLines.get(currentLineIndex++);
                    String trimmed = line.trim();

                    // Skip empty lines and comment lines
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        multiline = false;
                        continue;
                    }
                    // Skip lines that do not look like translation entries
                    if (!multiline && (!trimmed.contains("=") || !trimmed.contains("\""))) {
                        multiline = false;
                        continue;
                    }
                    if (!multiline) {
                        String[] parts = trimmed.split("=", 2);
                        currentKey = parts[0].trim();
                        String valuePart = parts[1].trim();
                        if (valuePart.isEmpty() || valuePart.indexOf('"') == -1) {
                            multiline = false;
                            continue;
                        }
                        // Multiline value starts with '..' at the end
                        if (trimmed.endsWith("..")) {
                            multiline = true;
                            valuePart = valuePart.substring(0, valuePart.length() - 2).trim();
                            int firstQuote = valuePart.indexOf('"');
                            int lastQuote = valuePart.lastIndexOf('"');
                            currentValue.setLength(0);
                            if (firstQuote != -1 && lastQuote > firstQuote) {
                                currentValue.append(valuePart.substring(firstQuote + 1, lastQuote));
                            } else if (firstQuote != -1) {
                                currentValue.append(valuePart.substring(firstQuote + 1));
                            }
                            continue;
                        } else {
                            int firstQuote = valuePart.indexOf('"');
                            int lastQuote = valuePart.lastIndexOf('"');
                            if (firstQuote == -1 || lastQuote <= firstQuote) {
                                multiline = false;
                                continue;
                            }
                            currentValue.setLength(0);
                            currentValue.append(valuePart.substring(firstQuote + 1, lastQuote));
                            nextPair = new Pair(currentKey, currentValue.toString());
                            hasNextCalled = true;
                            return true;
                        }
                    } else {
                        // Multiline value continuation
                        String valuePart = trimmed;
                        if (valuePart.isEmpty() || valuePart.indexOf('"') == -1) {
                            continue;
                        }
                        if (trimmed.endsWith("..")) {
                            valuePart = valuePart.substring(0, valuePart.length() - 2).trim();
                            int firstQuote = valuePart.indexOf('"');
                            String toAppend = valuePart.substring(firstQuote + 1);
                            if (toAppend.endsWith("\"")) {
                                toAppend = toAppend.substring(0, toAppend.length() - 1);
                            }
                            currentValue.append(toAppend);
                            continue;
                        } else {
                            int firstQuote = valuePart.indexOf('"');
                            int lastQuote = valuePart.lastIndexOf('"');
                            if (firstQuote == -1 || lastQuote <= firstQuote) {
                                multiline = false;
                                continue;
                            }
                            currentValue.append(valuePart.substring(firstQuote + 1, lastQuote));
                            nextPair = new Pair(currentKey, currentValue.toString());
                            hasNextCalled = true;
                            multiline = false;
                            return true;
                        }
                    }
                }
                nextPair = null;
                hasNextCalled = true;
                return false;
            }

            /**
             * Returns the next translation pair.
             * 
             * @return the next Pair
             */
            @Override
            public Pair next() {
                if (!hasNextCalled)
                    hasNext();
                hasNextCalled = false;
                return nextPair;
            }
        };
    }

    /**
     * Returns a stream of translation pairs (skipping header and comments).
     * The stream is closed when finished.
     * 
     * @return stream of translation pairs
     */
    public Stream<Pair> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED), false)
                .onClose(this::close);
    }

    /**
     * Returns the charset that was successfully used to read the file.
     * 
     * @return the used charset, or null if none worked
     */
    public Charset getUsedCharset() {
        return usedCharset;
    }

    /**
     * Closes the parser and releases resources.
     */
    @Override
    public void close() {
        closed = true;
    }
}
