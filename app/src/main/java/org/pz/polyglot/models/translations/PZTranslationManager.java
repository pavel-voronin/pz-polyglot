package org.pz.polyglot.models.translations;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.pz.polyglot.models.TranslationSession;
import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.sources.PZSource;
import org.pz.polyglot.models.sources.PZSources;

/**
 * Manages translation files and variants for Polyglot.
 * Handles loading, saving, and updating translation entries and files.
 */
public class PZTranslationManager {
    /**
     * Loads all translation files from all sources into the translation registry.
     * After execution, all translations from all sources are loaded.
     */
    public static void loadFilesFromSources() {
        for (PZSource source : PZSources.getInstance().getSources()) {
            loadFilesFromSource(source);
        }
    }

    /**
     * Loads all translation files of known types from the given source into the
     * translation registry.
     * It creates or updates translation keys with specific translations.
     *
     * @param source the translation source to load files from
     */
    private static void loadFilesFromSource(PZSource source) {
        try (DirectoryStream<Path> langDirs = Files.newDirectoryStream(source.getPath(), p -> Files.isDirectory(p)
                && PZLanguages.getInstance().getLanguage(p.getFileName().toString()).isPresent())) {
            for (Path langDir : langDirs) {
                PZLanguage lang = PZLanguages.getInstance().getLanguage(langDir.getFileName().toString()).get();

                try (DirectoryStream<Path> files = Files.newDirectoryStream(langDir, p -> Files.isRegularFile(p)
                        && p.getFileName().toString().endsWith("_" + lang.getCode() + ".txt")
                        && PZTranslationType
                                .fromString(extractTypeFromFileName(p.getFileName().toString(), lang.getCode()))
                                .isPresent())) {
                    for (Path file : files) {
                        PZTranslationType translationType = PZTranslationType
                                .fromString(extractTypeFromFileName(file.getFileName().toString(), lang.getCode()))
                                .get();

                        try (PZTranslationParser reader = new PZTranslationParser(file, lang, source);
                                Stream<PZTranslationParser.Pair> stream = reader.stream()) {
                            stream.forEach(s -> {
                                PZTranslationEntry entry = PZTranslations.getInstance().getOrCreateTranslation(s.key());
                                entry.addVariant(source, lang, translationType, s.value(), reader.getUsedCharset());
                            });
                        }
                    }

                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     * Extracts the translation type from a filename by removing the language suffix
     * and extension.
     * For example: "IG_UI_PTBR.txt" with language "PTBR" -> "IG_UI"
     *
     * @param fileName     the filename to process
     * @param languageCode the language code to remove
     * @return the translation type string
     */
    private static String extractTypeFromFileName(String fileName, String languageCode) {
        // Remove _LANG.txt suffix using regex
        return fileName.replaceFirst("_" + languageCode + "\\.txt$", "");
    }

    /**
     * Saves a translation variant to its corresponding file.
     * If the file does not exist, it is created.
     * Updates the translation entry or adds a new one as needed.
     *
     * @param variant the translation variant to save
     */
    public static void saveVariant(PZTranslationVariant variant) {
        try {
            Path filePath = constructFilePath(variant);

            // Create file if it doesn't exist
            if (!Files.exists(filePath)) {
                createNewTranslationFile(filePath, variant);
            }

            // Read all lines from the file
            List<String> lines = Files.readAllLines(filePath, variant.getUsedCharset());

            String key = variant.getKey().getKey();
            String textToSave = variant.getEditedText();

            // Find existing key or add new one
            int[] keyLines = findKeyInFile(lines, key);

            if (keyLines != null) {
                // Replace existing key
                replaceLines(lines, keyLines[0], keyLines[1], key, textToSave);
            } else {
                // Add new key before closing brace
                addNewKeyToFile(lines, key, textToSave);
            }

            // Write the modified lines back to the file
            Files.write(filePath, lines, variant.getUsedCharset());

            variant.markSaved();
        } catch (IOException e) {
            System.err.println("Failed to save variant: " + e.getMessage());
        }
    }

    /**
     * Replaces lines from startLine to endLine (inclusive, 1-based) with a single
     * line
     * containing the translation in the format: key = "value".
     *
     * @param lines     the list of lines in the file
     * @param startLine the starting line number (1-based)
     * @param endLine   the ending line number (1-based)
     * @param key       the translation key
     * @param value     the translation value
     */
    private static void replaceLines(List<String> lines, int startLine, int endLine,
            String key, String value) {
        if (startLine < 1 || endLine < 1 || startLine > lines.size() || endLine > lines.size()) {
            return; // Invalid line numbers
        }

        // Convert to 0-based indexing
        int startIndex = startLine - 1;
        int endIndex = endLine - 1;

        // Create the new translation line
        String newLine = "    " + key + " = \"" + value + "\",";

        // Remove the old lines
        for (int i = endIndex; i >= startIndex; i--) {
            lines.remove(i);
        }

        // Add the new line at the start position
        lines.add(startIndex, newLine);
    }

    /**
     * Constructs the file path for a translation variant based on its source,
     * language, and type.
     *
     * @param variant the translation variant
     * @return the path to the translation file
     */
    private static Path constructFilePath(PZTranslationVariant variant) {
        Path sourcePath = variant.getSource().getPath();
        String languageCode = variant.getLanguage().getCode();
        String fileName = variant.getType().name() + "_" + languageCode + ".txt";
        return sourcePath.resolve(languageCode).resolve(fileName);
    }

    /**
     * Finds the line numbers of a translation key in the file.
     * Returns an array with start and end line numbers (inclusive, 1-based), or
     * null if not found.
     *
     * @param lines the lines of the file
     * @param key   the translation key to search for
     * @return int array [startLine, endLine] or null if not found
     */
    private static int[] findKeyInFile(List<String> lines, String key) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith(key + " =")) {
                int endLine = i + 1;
                // If it's a multiline value, find the actual end
                if (!line.endsWith(",") && !line.endsWith("}")) {
                    for (int j = i + 1; j < lines.size(); j++) {
                        String nextLine = lines.get(j).trim();
                        if (nextLine.endsWith(",") || nextLine.equals("}")) {
                            endLine = j + 1;
                            break;
                        }
                    }
                }
                return new int[] { i + 1, endLine }; // Convert to 1-based indexing
            }
        }
        return null; // Key not found
    }

    /**
     * Adds a new translation key-value pair to the file, inserting before the
     * closing brace.
     *
     * @param lines the lines of the file
     * @param key   the translation key
     * @param value the translation value
     */
    private static void addNewKeyToFile(List<String> lines, String key, String value) {
        int insertPosition = lines.size(); // Default to end
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i).trim();
            if (line.equals("}")) {
                insertPosition = i;
                break;
            }
        }
        String newLine = "    " + key + " = \"" + value + "\",";
        lines.add(insertPosition, newLine);
    }

    /**
     * Creates a new translation file with the basic structure if it does not exist.
     *
     * @param filePath the path to the new file
     * @param variant  the translation variant for file naming and charset
     * @throws IOException if file creation fails
     */
    private static void createNewTranslationFile(Path filePath, PZTranslationVariant variant) throws IOException {
        Files.createDirectories(filePath.getParent());
        String fileTemplate = variant.getType().name() + "_" + variant.getLanguage().getCode() + " = {\n}";
        Files.write(filePath, fileTemplate.getBytes(variant.getSupposedCharset()));
    }

    /**
     * Saves all translation variants in the current session.
     * Uses a copy of the collection to avoid ConcurrentModificationException.
     */
    public static void saveAll() {
        var variantsCopy = new ArrayList<>(TranslationSession.getInstance().getVariants());
        variantsCopy.forEach(PZTranslationManager::saveVariant);
    }
}
