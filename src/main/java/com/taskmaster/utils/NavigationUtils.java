package com.taskmaster.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.application.Platform;
import javafx.scene.input.KeyCombination;

import java.io.IOException;

public class NavigationUtils {

    /**
     * Navigates to a new view while FORCING fullscreen state to remain active.
     * Automatically applies the theme.css to every page.
     *
     * @param sourceNode A node from the current scene (used to get the stage).
     * @param fxmlPath   The resource path to the FXML file.
     * @param title      The title for the new stage.
     */
    public static void navigateTo(Node sourceNode, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) sourceNode.getScene().getWindow();

            // Sauvegarder l'état AVANT de changer la scène
            boolean wasFullScreen = stage.isFullScreen();
            boolean wasMaximized = stage.isMaximized();

            System.out.println("=== NAVIGATION ===");
            System.out.println("Avant navigation - FullScreen: " + wasFullScreen + ", Maximized: " + wasMaximized);

            // Charger la nouvelle vue
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Créer la scène SANS dimensions fixes
            Scene scene = new Scene(root);

            // 🎨 APPLIQUER AUTOMATIQUEMENT LE THEME CSS
            try {
                scene.getStylesheets().add(
                        NavigationUtils.class.getResource("/com/taskmaster/views/theme.css").toExternalForm()
                );
                System.out.println("✅ Theme CSS appliqué à: " + title);
            } catch (Exception e) {
                System.err.println("⚠️ Impossible de charger theme.css : " + e.getMessage());
            }

            stage.setScene(scene);
            stage.setTitle("TaskMaster - " + title);

            // Configurer le fullscreen (cacher le message Échap)
            stage.setFullScreenExitHint(""); // Cache le message "Appuyez sur Échap"
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Désactive Échap

            // FORCER le retour à l'état fullscreen/maximized
            if (wasFullScreen) {
                // Si c'était en fullscreen, forcer le fullscreen immédiatement
                stage.setFullScreen(true);

                // Double vérification avec Platform.runLater
                Platform.runLater(() -> {
                    if (!stage.isFullScreen()) {
                        stage.setFullScreen(true);
                        System.out.println("→ FullScreen FORCÉ (runLater)");
                    }
                    System.out.println("→ FullScreen: " + stage.isFullScreen());
                });

            } else if (wasMaximized) {
                stage.setMaximized(true);

                Platform.runLater(() -> {
                    if (!stage.isMaximized()) {
                        stage.setMaximized(true);
                        System.out.println("→ Maximized FORCÉ (runLater)");
                    }
                    System.out.println("→ Maximized: " + stage.isMaximized());
                });
            }

            System.out.println("===================");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la vue : " + fxmlPath);
        }
    }

    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 🎨 Appliquer le thème CSS aux messages d'erreur
        try {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    NavigationUtils.class.getResource("/com/taskmaster/views/theme.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            System.err.println("⚠️ Impossible d'appliquer le CSS au dialog d'erreur");
        }

        alert.showAndWait();
    }
}