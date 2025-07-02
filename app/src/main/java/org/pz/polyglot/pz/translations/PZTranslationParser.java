package org.pz.polyglot.pz.translations;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Reads a PZTranslationFile and returns a stream of non-comment lines, skipping
 * the first line (header).
 * Handles block and line comments, and supports fallback charset if decoding
 * fails.
 * This class is not responsible for domain parsing, only for reading and
 * cleaning lines.
 */
public class PZTranslationParser implements AutoCloseable, Iterable<PZTranslationParser.Pair> {
    private static final Logger LOGGER = Logger.getLogger(PZTranslationParser.class.getName());

    public record Pair(String key, String value) {
    }

    private record ReadResult(List<String> lines, Charset charset) {
    }

    private final PZTranslationFile file;
    private final LinkedHashSet<Charset> availableCharsets;
    private final List<String> allLines;
    private final Charset usedCharset;
    private boolean closed;

    public PZTranslationParser(PZTranslationFile file) {
        this.file = file;
        this.availableCharsets = file.getLanguage().getCharsetsDownFrom(file.getSource().getVersion());
        this.closed = false;
        var result = readAllLinesWithCorrectCharset();
        this.allLines = result.lines();
        this.usedCharset = result.charset();
    }

    /**
     * Reads all lines from the file using the first available charset that works
     */
    private ReadResult readAllLinesWithCorrectCharset() {
        for (Charset charset : availableCharsets) {
            try {
                List<String> lines = Files.readAllLines(file.getPath(), charset);
                return new ReadResult(lines, charset);
            } catch (IOException e) {
                // Try next charset
                continue;
            }
        }
        LOGGER.log(Level.WARNING, "Failed to read file with any available charset: " + file.getPath());
        return new ReadResult(List.of(), null); // Return empty list if no charset works
    }

    @Override
    public Iterator<Pair> iterator() {
        return new Iterator<Pair>() {
            private int currentLineIndex = 0;
            private boolean multiline = false;
            private String currentKey = "";
            private StringBuilder currentValue = new StringBuilder();
            private Pair nextPair = null;
            private boolean hasNextCalled = false;

            @Override
            public boolean hasNext() {
                if (closed)
                    return false;
                if (hasNextCalled)
                    return nextPair != null;

                while (currentLineIndex < allLines.size()) {
                    String line = allLines.get(currentLineIndex++);
                    String trimmed = line.trim();

                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        multiline = false;
                        continue;
                    }
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
                        // Multiline value
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
     * Returns a stream of non-comment lines (skipping header).
     */
    public Stream<Pair> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED), false)
                .onClose(this::close);
    }

    /**
     * Returns the charset that was successfully used to read the file
     */
    public Charset getUsedCharset() {
        return usedCharset;
    }

    @Override
    public void close() {
        closed = true;
    }
}
