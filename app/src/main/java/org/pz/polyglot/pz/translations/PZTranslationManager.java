package org.pz.polyglot.pz.translations;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

                        PZTranslationFile translationFile = new PZTranslationFile(file, translationType, lang, source);
                        try (PZTranslationParser reader = new PZTranslationParser(translationFile);
                                Stream<PZTranslationParser.Pair> stream = reader.stream()) {
                            stream.forEach(s -> {
                                PZTranslationEntry entry = PZTranslations.getInstance().getOrCreateTranslation(s.key());

                                entry.addVariant(translationFile, s.value(), reader.getUsedCharset(), s.startLine(),
                                        s.endLine());
                            });
                        }
                    }

                } catch (IOException e) {
                }
            }
        } catch (IOException e) {
        }
    }
}
