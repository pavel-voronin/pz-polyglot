package org.pz.polyglot.components.addModDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.pz.polyglot.App;
import org.pz.polyglot.Logger;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.utils.FolderUtils;

/**
 * Manager for the Add Mod dialog.
 * <p>
 * Responsible for displaying the modal dialog to add new mods and for creating
 * their directory structure in the workshop folder.
 */
public class AddModDialogManager {

    /**
     * The width of the Add Mod dialog window in pixels.
     */
    private static final int DIALOG_WIDTH = 380;

    /**
     * The height of the Add Mod dialog window in pixels.
     */
    private static final int DIALOG_HEIGHT = 250;

    /**
     * Shows the Add Mod dialog and creates the mod structure if successful.
     *
     * @param owner the parent stage
     * @return true if mod was created successfully, false if cancelled or failed
     */
    public static boolean showAddModDialog(Stage owner) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/AddModDialog.fxml"));
            Parent root = loader.load();
            AddModDialogController controller = loader.getController();

            Stage dialog = new Stage();
            controller.setDialogStage(dialog);
            dialog.setTitle("Add New Mod");
            dialog.initOwner(owner);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setResizable(false);

            Scene scene = new Scene(root, DIALOG_WIDTH, DIALOG_HEIGHT);
            dialog.setScene(scene);
            dialog.showAndWait();

            if (controller.isModCreated()) {
                return createModStructure(controller.getEnteredModName(), controller.getSelectedVersion());
            }
            return false;

        } catch (IOException e) {
            Logger.error("Error opening add mod dialog: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates the mod directory structure in the workshop folder for the specified
     * mod and version.
     * <p>
     * The structure differs depending on the version:
     * <ul>
     * <li>Version 42: Workshop/{modname}/common/media/lua/shared/Translate/</li>
     * <li>Version 41: Workshop/{modname}/media/lua/shared/Translate/</li>
     * </ul>
     * After creation, the sources are rescanned to include the new mod.
     *
     * @param modName the name of the mod
     * @param version the version ("41" or "42")
     * @return true if structure was created successfully
     */
    private static boolean createModStructure(String modName, String version) {
        try {
            var workshopPath = FolderUtils.getWorkshopPath();
            if (workshopPath.isEmpty()) {
                Logger.error("Workshop path is not available");
                return false;
            }

            Path modBasePath = workshopPath.get()
                    .resolve(modName)
                    .resolve("Contents/mods/")
                    .resolve(modName);
            Path translationPath;

            // Select translation path based on mod version
            if ("42".equals(version)) {
                translationPath = modBasePath.resolve("common/media/lua/shared/Translate");
            } else {
                translationPath = modBasePath.resolve("media/lua/shared/Translate");
            }

            Files.createDirectories(translationPath);

            createModInfoFile(modBasePath, modName, version);

            Logger.info("Created mod structure for '" + modName + "' (version " + version + ") at: " + translationPath);

            // Rescan sources to include the new mod
            PZSources.getInstance().parseSources();
            Logger.info("Sources rescanned after mod creation");

            return true;

        } catch (IOException e) {
            Logger.error("Failed to create mod structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates the mod.info file with the required content for the mod.
     * <p>
     * For version 42, the file is placed in the '42/' folder next to 'common/'.
     * For version 41, the file is placed next to the 'media/' folder.
     *
     * @param modBasePath the base path of the mod
     * @param modName     the name of the mod
     * @param version     the version ("41" or "42")
     * @throws IOException if file creation fails
     */
    private static void createModInfoFile(Path modBasePath, String modName, String version) throws IOException {
        Path modInfoPath;

        if ("42".equals(version)) {
            modInfoPath = modBasePath.resolve("42/mod.info");
            Files.createDirectories(modBasePath.resolve("42"));
        } else {
            modInfoPath = modBasePath.resolve("mod.info");
        }

        // Compose mod.info file content
        String modInfoContent = String.format(
                "name=%s%n" +
                        "id=%s%n" +
                        "modversion=%n" +
                        "author=%n" +
                        "description=%n" +
                        "poster=%n" +
                        "icon=%n",
                modName.trim(),
                modName.trim().toLowerCase().replace(" ", "_"));

        Files.writeString(modInfoPath, modInfoContent);
        Logger.info("Created mod.info file at: " + modInfoPath);
    }
}
