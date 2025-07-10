package org.pz.polyglot.models.translations;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.pz.polyglot.models.languages.PZLanguage;
import org.pz.polyglot.models.languages.PZLanguages;
import org.pz.polyglot.models.sources.PZSource;
import org.pz.polyglot.models.sources.PZSources;

public class PZTranslationManager {
    public static void loadFilesFromSources() {
        for (PZSource source : PZSources.getInstance().getSources()) {
            loadFilesFromSource(source);
        }
        System.out.println("Loaded translations from all sources");
    }

    // Loads all translation files of known types from the given source to the
    // translation registry. It creates or updates translation keys with specific
    // translations. After run all the translations from the source are loaded,
    // nothing left
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
     * Extracts the translation type from a filename by removing the language
     * suffix and extension.
     * For example: "IG_UI_PTBR.txt" with language "PTBR" -> "IG_UI"
     */
    private static String extractTypeFromFileName(String fileName, String languageCode) {
        // Remove _LANG.txt suffix using regex
        return fileName.replaceFirst("_" + languageCode + "\\.txt$", "");
    }

    public static void saveVariant(PZTranslationVariant variant) {
        try {
            Path filePath = constructFilePath(variant);

            // Create file if it doesn't exist
            if (!Files.exists(filePath)) {
                createNewTranslationFile(filePath, variant);
            }

            // Read all lines from the file
            List<String> lines = Files.readAllLines(filePath, variant.getUsedCharset());

            // TODO ATTENTION!!!
            // broken flow: initially file may be encoded with a wrong charset
            // then we detect right charset and save it in variants as usedCharset
            // then we try to save it with the right charset into the file with the wrong
            // charset need to handle this case properly
            // task-16

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
     * line containing the translation in the format: key = "value"
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

    private static Path constructFilePath(PZTranslationVariant variant) {
        // Construct the file path based on source, language, and type
        Path sourcePath = variant.getSource().getPath();
        String languageCode = variant.getLanguage().getCode();
        String fileName = variant.getType().name() + "_" + languageCode + ".txt";
        return sourcePath.resolve(languageCode).resolve(fileName);
    }

    private static int[] findKeyInFile(List<String> lines, String key) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith(key + " =")) {
                // Found the key, now find where it ends
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

    private static void addNewKeyToFile(List<String> lines, String key, String value) {
        // Find the position to insert (before closing brace)
        int insertPosition = lines.size(); // Default to end

        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i).trim();
            if (line.equals("}")) {
                insertPosition = i;
                break;
            }
        }

        // Create the new translation line
        String newLine = "    " + key + " = \"" + value + "\",";
        lines.add(insertPosition, newLine);
    }

    private static void createNewTranslationFile(Path filePath, PZTranslationVariant variant) throws IOException {
        // Create directories if they don't exist
        Files.createDirectories(filePath.getParent());

        // Create the file with the basic structure
        String fileTemplate = variant.getType().name() + "_" + variant.getLanguage().getCode() + " = {\n}";
        Files.write(filePath, fileTemplate.getBytes(variant.getSupposedCharset()));
    }

    public static void saveAll() {
        // Create a copy of the collection to avoid ConcurrentModificationException
        // since saveVariant() calls markSaved() which modifies the original collection
        var variantsCopy = new ArrayList<>(PZTranslationSession.getInstance().getVariants());
        variantsCopy.forEach(PZTranslationManager::saveVariant);
    }
}
