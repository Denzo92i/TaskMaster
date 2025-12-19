package com.taskmaster.controllers;

import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.User;
import com.taskmaster.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        errorLabel.setText("");

        // Permettre la connexion avec la touche Enter
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // Authentification
        User user = userDAO.authenticate(username, password);

        if (user != null) {
            // Connexion réussie
            SessionManager.setCurrentUser(user);
            redirectToDashboard(user);
        } else {
            showError("Identifiants incorrects");
        }
    }

    private void redirectToDashboard(User user) {
        try {
            String fxmlPath = user.isAdmin() ?
                    "/com/taskmaster/views/admin_dashboard.fxml" :
                    "/com/taskmaster/views/user_dashboard.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);

            // 🎨 CORRECTION CRITIQUE : Appliquer le CSS AVANT de définir la scène
            try {
                String cssPath = getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm();
                scene.getStylesheets().add(cssPath);
                System.out.println("✅ Thème CSS appliqué au dashboard lors de la connexion");
            } catch (Exception cssError) {
                System.err.println("⚠️ Impossible de charger le CSS: " + cssError.getMessage());
                cssError.printStackTrace();
            }

            stage.setScene(scene);
            stage.setTitle("TaskMaster - " + (user.isAdmin() ? "Admin" : "Dashboard"));

            // Rendre la fenêtre redimensionnable
            stage.setResizable(true);

            // Configurer le fullscreen (cacher le message Échap et désactiver la touche)
            stage.setFullScreenExitHint(""); // Cache le message "Appuyez sur Échap"
            stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH); // Désactive Échap

            // Mettre en FULLSCREEN immédiatement
            stage.setFullScreen(true);

            // Double vérification avec Platform.runLater
            Platform.runLater(() -> {
                if (!stage.isFullScreen()) {
                    stage.setFullScreen(true);
                    System.out.println("→ FullScreen FORCÉ après connexion");
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmaster/views/register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 450, 600);

            // 🎨 Appliquer le CSS à la page d'inscription
            try {
                scene.getStylesheets().add(
                        getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
                );
            } catch (Exception cssError) {
                System.err.println("⚠️ CSS non chargé pour register");
            }

            stage.setScene(scene);
            stage.setTitle("TaskMaster - Inscription");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de la page d'inscription");
        }
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
    }
}