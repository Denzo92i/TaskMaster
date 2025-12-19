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
import java.net.URL;

public class NavigationUtils {

    // 🚀 Cache pour le chemin CSS (évite de le rechercher à chaque fois)
    private static String cachedCssPath = null;

    /**
     * Navigates to a new view while FORCING fullscreen state to remain active.
     * Automatically applies the theme.css to every page.
     * OPTIMIZED: Réduit les appels inutiles et améliore la fluidité.
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

            // 🚀 OPTIMISATION 1: Charger le FXML de manière optimisée
            FXMLLoader loader = new FXMLLoader(NavigationUtils.class.getResource(fxmlPath));
            loader.setClassLoader(NavigationUtils.class.getClassLoader());
            Parent root = loader.load();

            // 🚀 OPTIMISATION 2: Réutiliser la scène existante si possible
            Scene currentScene = stage.getScene();
            Scene scene;

            if (currentScene != null) {
                // Réutiliser la scène existante (plus rapide)
                currentScene.setRoot(root);
                scene = currentScene;
            } else {
                // Créer une nouvelle scène seulement si nécessaire
                scene = new Scene(root);
                stage.setScene(scene);
            }

            // 🎨 OPTIMISATION 3: Appliquer le CSS une seule fois avec cache
            applyCssToScene(scene, title);

            // 🚀 OPTIMISATION 4: Mettre à jour le titre avant les opérations visuelles
            stage.setTitle("TaskMaster - " + title);

            // Configurer le fullscreen (cacher le message Échap)
            stage.setFullScreenExitHint("");
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

            // 🚀 OPTIMISATION 5: Restaurer l'état en une seule fois
            restoreWindowState(stage, wasFullScreen, wasMaximized);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la vue : " + fxmlPath);
        }
    }

    /**
     * 🎨 Applique le CSS de manière optimisée avec cache
     */
    private static void applyCssToScene(Scene scene, String pageTitle) {
        try {
            // Si le CSS est déjà chargé dans la scène, ne rien faire
            if (!scene.getStylesheets().isEmpty()) {
                return;
            }

            // Charger le chemin CSS une seule fois
            if (cachedCssPath == null) {
                URL cssUrl = NavigationUtils.class.getResource("/com/taskmaster/views/theme.css");
                if (cssUrl != null) {
                    cachedCssPath = cssUrl.toExternalForm();
                } else {
                    System.err.println("⚠️ theme.css introuvable !");
                    return;
                }
            }

            // Appliquer le CSS
            scene.getStylesheets().clear();
            scene.getStylesheets().add(cachedCssPath);

        } catch (Exception e) {
            System.err.println("⚠️ Erreur lors de l'application du CSS : " + e.getMessage());
        }
    }

    /**
     * 🚀 Restaure l'état de la fenêtre de manière optimisée
     */
    private static void restoreWindowState(Stage stage, boolean wasFullScreen, boolean wasMaximized) {
        if (wasFullScreen) {
            // FORCER le fullscreen immédiatement
            stage.setFullScreen(true);

            // Vérification rapide après le prochain frame
            Platform.runLater(() -> {
                if (!stage.isFullScreen()) {
                    stage.setFullScreen(true);
                }
            });

        } else if (wasMaximized) {
            stage.setMaximized(true);

            Platform.runLater(() -> {
                if (!stage.isMaximized()) {
                    stage.setMaximized(true);
                }
            });
        }
    }

    /**
     * Affiche un message d'erreur avec le thème appliqué
     */
    private static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 🎨 Appliquer le thème CSS au dialog
        try {
            if (cachedCssPath == null) {
                URL cssUrl = NavigationUtils.class.getResource("/com/taskmaster/views/theme.css");
                if (cssUrl != null) {
                    cachedCssPath = cssUrl.toExternalForm();
                }
            }

            if (cachedCssPath != null) {
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.getStylesheets().add(cachedCssPath);
                dialogPane.getStyleClass().add("dialog-pane");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Impossible d'appliquer le CSS au dialog d'erreur");
        }

        alert.showAndWait();
    }

    /**
     * 🧹 Méthode utilitaire pour nettoyer le cache si nécessaire
     */
    public static void clearCache() {
        cachedCssPath = null;
    }
}