package com.taskmaster;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Classe principale de l'application TaskMaster
 * Point d'entrée du programme
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger l'écran de connexion
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/taskmaster/views/login.fxml")
            );

            Parent root = loader.load();

            // Configuration de la fenêtre
            Scene scene = new Scene(root, 400, 550);

            // 🔥 Chargement du thème CSS noir & violet
            scene.getStylesheets().add(
                    getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
            );

            primaryStage.setTitle("TaskMaster - Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur au démarrage : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // ===== TEST : Générer un hash pour password123 =====
        System.out.println("=== GÉNÉRATION DE HASH ===");
        String testPassword = "password123";
        String newHash = BCrypt.hashpw(testPassword, BCrypt.gensalt(10));
        System.out.println("Mot de passe : " + testPassword);
        System.out.println("Nouveau hash : " + newHash);
        System.out.println("===============================");

        // Lancer l'application JavaFX
        launch(args);
    }
}
