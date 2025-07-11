package org.pz.polyglot.components.addModDialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.pz.polyglot.App;
import org.pz.polyglot.models.sources.PZSources;
import org.pz.polyglot.util.FolderUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Manager for the Add Mod dialog.
 * Handles showing the modal dialog for adding new mods and creating their
 * directory structure.
 */
public class AddModDialogManager {

    private static final Logger logger = Logger.getLogger(AddModDialogManager.class.getName());
    private static final int DIALOG_WIDTH = 380;
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
            logger.severe("Error opening add mod dialog: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates the mod directory structure in the workshop folder.
     * 
     * @param modName the name of the mod
     * @param version the version ("41" or "42")
     * @return true if structure was created successfully
     */
    private static boolean createModStructure(String modName, String version) {
        try {
            var workshopPath = FolderUtils.getWorkshopPath();
            if (workshopPath.isEmpty()) {
                logger.severe("Workshop path is not available");
                return false;
            }

            Path modBasePath = workshopPath.get()
                    .resolve(modName)
                    .resolve("Contents/mods/")
                    .resolve(modName);
            Path translationPath;

            // Create different structures based on version
            if ("42".equals(version)) {
                // Version 42: Workshop/{modname}/common/media/lua/shared/Translate/
                translationPath = modBasePath.resolve("common/media/lua/shared/Translate");
            } else {
                // Version 41: Workshop/{modname}/media/lua/shared/Translate/
                translationPath = modBasePath.resolve("media/lua/shared/Translate");
            }

            // Create all directories in the path recursively
            Files.createDirectories(translationPath);

            // Create mod.info file
            createModInfoFile(modBasePath, modName, version);

            logger.info("Created mod structure for '" + modName + "' (version " + version + ") at: " + translationPath);

            // Rescan sources to include the new mod
            PZSources.getInstance().parseSources();
            logger.info("Sources rescanned after mod creation");

            return true;

        } catch (IOException e) {
            logger.severe("Failed to create mod structure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Creates the mod.info file with the required content.
     * 
     * @param modBasePath the base path of the mod
     * @param modName     the name of the mod
     * @param version     the version ("41" or "42")
     * @throws IOException if file creation fails
     */
    private static void createModInfoFile(Path modBasePath, String modName, String version) throws IOException {
        Path modInfoPath;

        if ("42".equals(version)) {
            // For version 42: place in 42/ folder next to common/
            modInfoPath = modBasePath.resolve("42/mod.info");
            // Ensure the 42/ directory exists
            Files.createDirectories(modBasePath.resolve("42"));
        } else {
            // For version 41: place next to media/ folder
            modInfoPath = modBasePath.resolve("mod.info");
        }

        // Create mod.info content
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

        // Write the file
        Files.writeString(modInfoPath, modInfoContent);
        logger.info("Created mod.info file at: " + modInfoPath);
    }
}
