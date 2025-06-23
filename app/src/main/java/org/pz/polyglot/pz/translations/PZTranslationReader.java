package org.pz.polyglot.pz.translations;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Iterator;
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
public class PZTranslationReader implements AutoCloseable, Iterable<PZTranslationReader.Pair> {
    private static final Logger LOGGER = Logger.getLogger(PZTranslationReader.class.getName());

    public record Pair(String key, String value) {}

    private final PZTranslationFile file;
    private final Charset primaryCharset;
    private final Charset fallbackCharset;
    private BufferedReader reader;
    private boolean usingFallback;
    private boolean closed;
    private int linesRead;
    private String nextLine;
    private boolean hasNextCalled;

    public PZTranslationReader(PZTranslationFile file) {
        this.file = file;
        this.primaryCharset = file.getLanguage().getCharset();
        this.fallbackCharset = file.getLanguage().getFallbackCharset().orElse(java.nio.charset.StandardCharsets.UTF_8);
        this.usingFallback = false;
        this.closed = false;
        this.linesRead = 0;
        this.nextLine = null;
        this.hasNextCalled = false;
        if (!openReader(primaryCharset)) {
            if (!openReader(fallbackCharset)) {
                LOGGER.log(Level.WARNING, "Failed to read file: " + file.getPath());
            } else {
                usingFallback = true;
            }
        }
    }

    private boolean openReader(Charset charset) {
        try {
            this.reader = Files.newBufferedReader(file.getPath(), charset);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public Iterator<Pair> iterator() {
        return new Iterator<Pair>() {
            private boolean multiline = false;
            private String currentKey = "";
            private StringBuilder currentValue = new StringBuilder();

            @Override
            public boolean hasNext() {
                if (closed || reader == null)
                    return false;
                if (hasNextCalled)
                    return nextLine != null;
                while (true) {
                    String line = null;
                    try {
                        if (!reader.ready())
                            break;
                        line = reader.readLine();
                        linesRead++;
                    } catch (IOException e) {
                        if (!usingFallback && openReader(fallbackCharset)) {
                            usingFallback = true;
                            for (int i = 0; i < linesRead; i++) {
                                try {
                                    reader.readLine();
                                } catch (IOException ex) {
                                    close();
                                    break;
                                }
                            }
                            continue;
                        } else {
                            LOGGER.log(Level.WARNING, "Failed to read file: " + file.getPath());
                            close();
                            nextLine = null;
                            hasNextCalled = true;
                            return false;
                        }
                    }
                    if (line == null)
                        break;
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
                            nextLine = currentKey + "=" + '"' + currentValue.toString() + '"';
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
                            nextLine = currentKey + "=" + '"' + currentValue.toString() + '"';
                            hasNextCalled = true;
                            multiline = false;
                            return true;
                        }
                    }
                }
                nextLine = null;
                hasNextCalled = true;
                return false;
            }

            @Override
            public Pair next() {
                if (!hasNextCalled)
                    hasNext();
                hasNextCalled = false;
                if (nextLine == null || currentKey == null) {
                    return null;
                }
                String value = nextLine;
                int eq = value.indexOf('=');
                if (eq != -1) {
                    value = value.substring(eq + 1).trim();
                }
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                return new Pair(currentKey, value);
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

    @Override
    public void close() {
        if (!closed) {
            closeQuietly(reader);
            closed = true;
        }
    }

    private static void closeQuietly(BufferedReader reader) {
        try {
            if (reader != null)
                reader.close();
        } catch (IOException ignored) {
        }
    }
}
