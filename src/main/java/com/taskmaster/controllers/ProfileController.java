package com.taskmaster.controllers;

import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.User;
import com.taskmaster.utils.PasswordHasher;
import com.taskmaster.utils.SessionManager;
import com.taskmaster.utils.Validator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.taskmaster.utils.NavigationUtils;

public class ProfileController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;
    @FXML private TextField newEmailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private UserDAO userDAO = new UserDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            displayUserInfo();
        }

        messageLabel.setText("");
    }

    /**
     * Méthode appelée par AdminDashboardController pour passer l'utilisateur
     * (Optionnelle car initialize() récupère déjà l'utilisateur)
     */
    public void setUser(User user) {
        this.currentUser = user;
        displayUserInfo();
    }

    /**
     * Affiche les informations de l'utilisateur
     */
    private void displayUserInfo() {
        if (currentUser != null) {
            nameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            emailLabel.setText(currentUser.getEmail());
            roleLabel.setText(currentUser.getRole());
            newEmailField.setText(currentUser.getEmail());
        }
    }

    @FXML
    private void handleUpdateEmail() {
        String newEmail = newEmailField.getText().trim();

        // Validation
        if (!Validator.isValidEmail(newEmail)) {
            showError("Email invalide !");
            return;
        }

        // Vérifier si l'email existe déjà
        if (userDAO.emailExists(newEmail) && !newEmail.equals(currentUser.getEmail())) {
            showError("Cet email est déjà utilisé !");
            return;
        }

        // Mise à jour
        currentUser.setEmail(newEmail);
        if (userDAO.update(currentUser)) {
            showSuccess("Email mis à jour avec succès !");
            emailLabel.setText(newEmail);

            // Mettre à jour la session
            SessionManager.setCurrentUser(currentUser);
        } else {
            showError("Erreur lors de la mise à jour de l'email");
        }
    }

    @FXML
    private void handleUpdatePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation des champs vides
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Tous les champs sont obligatoires !");
            return;
        }

        // Vérifier l'ancien mot de passe
        if (!PasswordHasher.checkPassword(currentPassword, currentUser.getPassword())) {
            showError("Mot de passe actuel incorrect !");
            return;
        }

        // Vérifier la force du nouveau mot de passe
        if (!Validator.isStrongPassword(newPassword)) {
            showError(Validator.getPasswordError(newPassword));
            return;
        }

        // Vérifier la confirmation
        if (!Validator.passwordsMatch(newPassword, confirmPassword)) {
            showError("Les mots de passe ne correspondent pas !");
            return;
        }

        // Hasher et mettre à jour
        String hashedPassword = PasswordHasher.hashPassword(newPassword);
        if (userDAO.updatePassword(currentUser.getId(), hashedPassword)) {
            showSuccess("Mot de passe mis à jour avec succès !");

            // Mettre à jour l'objet User en mémoire
            currentUser.setPassword(hashedPassword);
            SessionManager.setCurrentUser(currentUser);

            // Vider les champs
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            showError("Erreur lors de la mise à jour du mot de passe");
        }
    }

    @FXML
    private void goBack() {
        try {
            // Déterminer le dashboard selon le rôle
            String dashboardPath = currentUser.isAdmin()
                    ? "/com/taskmaster/views/admin_dashboard.fxml"
                    : "/com/taskmaster/views/user_dashboard.fxml";

            NavigationUtils.navigateTo(nameLabel, dashboardPath, "Dashboard");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors du retour au dashboard");
        }
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green; -fx-font-size: 14px; -fx-font-weight: bold;");
        messageLabel.setText("✓ " + message);
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-font-weight: bold;");
        messageLabel.setText("✗ " + message);
    }
}