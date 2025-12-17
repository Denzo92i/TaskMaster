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
            stage.setScene(new Scene(root));
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

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
    }
}