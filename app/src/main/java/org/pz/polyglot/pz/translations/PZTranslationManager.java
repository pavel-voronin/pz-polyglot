package org.pz.polyglot.pz.translations;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.pz.polyglot.pz.languages.PZLanguage;
import org.pz.polyglot.pz.languages.PZLanguages;
import org.pz.polyglot.pz.sources.PZSource;
import org.pz.polyglot.pz.sources.PZSources;

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
                        && PZTranslationType.fromString(p.getFileName().toString().split("_")[0]).isPresent())) {
                    for (Path file : files) {
                        PZTranslationType translationType = PZTranslationType
                                .fromString(file.getFileName().toString().split("_")[0])
                                .get();

                        PZTranslationFile translationFile = new PZTranslationFile(file, translationType, lang, source,
                                false);
                        try (PZTranslationParser reader = new PZTranslationParser(translationFile);
                                Stream<PZTranslationParser.Pair> stream = reader.stream()) {
                            stream.forEach(s -> {
                                PZTranslationEntry entry = PZTranslations.getInstance().getOrCreateTranslation(s.key());

                                PZTranslationVariant variant = entry.addVariant(translationFile, s.value(),
                                        reader.getUsedCharset(), s.startLine(),
                                        s.endLine());
                                translationFile.addVariant(variant);
                            });
                        }
                    }

                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
        }
    }

    public static void saveVariant(PZTranslationVariant variant) {
        if (variant.isNew()) {
            return; // Do not save new variants for now
        }

        PZTranslationFile file = variant.getFile();

        if (file.isNew()) {
            return; // Do not save new files for now
        }

        Path path = file.getPath();

        try {
            // Get the text to save - either edited or original
            String textToSave = variant.getEditedText();
            String key = variant.getKey().getKey();

            // Read all lines from the file
            List<String> lines = Files.readAllLines(path, variant.getUsedCharset());

            // Replace the specified lines with the new translation
            replaceLines(lines, variant.getStartLine(), variant.getEndLine(),
                    key, textToSave);

            // Write the modified lines back to the file
            Files.write(path, lines, variant.getUsedCharset());

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

        // If there were multiple lines before, add empty lines to maintain structure
        int originalLineCount = endIndex - startIndex + 1;
        for (int i = 1; i < originalLineCount; i++) {
            lines.add(startIndex + i, "");
        }
    }

    public static void saveEntry(PZTranslationEntry entry) {
        // Create a copy of the collection to avoid ConcurrentModificationException
        // since saveVariant() calls markSaved() which may modify the original
        // collection
        var changedVariantsCopy = new ArrayList<>(entry.getChangedVariants());
        changedVariantsCopy.forEach(PZTranslationManager::saveVariant);
    }

    public static void resetEntry(PZTranslationEntry entry) {
        // Create a copy of the collection to avoid ConcurrentModificationException
        // since reset() may modify the original collection
        var changedVariantsCopy = new ArrayList<>(entry.getChangedVariants());
        changedVariantsCopy.forEach(PZTranslationVariant::reset);
    }

    public static void saveAll() {
        // Create a copy of the collection to avoid ConcurrentModificationException
        // since saveVariant() calls markSaved() which modifies the original collection
        var variantsCopy = new ArrayList<>(PZTranslationSession.getInstance().getVariants());
        variantsCopy.forEach(PZTranslationManager::saveVariant);
    }

    public static void resetAll() {
        // Create a copy of the collection to avoid ConcurrentModificationException
        // since reset() may modify the original collection
        var variantsCopy = new ArrayList<>(PZTranslationSession.getInstance().getVariants());
        variantsCopy.forEach(PZTranslationVariant::reset);
    }
}
