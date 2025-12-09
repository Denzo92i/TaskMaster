package com.taskmaster.controllers;

import com.taskmaster.models.User;
import com.taskmaster.utils.SessionManager;
import com.taskmaster.dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class UserProfileController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField roleField;

    private UserDAO userDAO = new UserDAO();
    private User currentUser;

    @FXML
    public void initialize() {
        // Récupération de l'utilisateur connecté
        currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            loadUserInfo();
        } else {
            showError("Utilisateur non connecté !");
        }
    }

    private void loadUserInfo() {
        firstNameField.setText(currentUser.getFirstName());
        lastNameField.setText(currentUser.getLastName());
        emailField.setText(currentUser.getEmail());
        roleField.setText(currentUser.getRole());
    }

    @FXML
    private void enableEditing() {
        firstNameField.setEditable(true);
        lastNameField.setEditable(true);
        emailField.setEditable(true);
    }

    @FXML
    private void saveProfile() {
        if (currentUser == null) return;

        currentUser.setFirstName(firstNameField.getText().trim());
        currentUser.setLastName(lastNameField.getText().trim());
        currentUser.setEmail(emailField.getText().trim());

        if (userDAO.update(currentUser)) {
            showSuccess("Profil mis à jour !");
            // Désactiver l’édition après sauvegarde
            firstNameField.setEditable(false);
            lastNameField.setEditable(false);
            emailField.setEditable(false);
        } else {
            showError("Erreur lors de la mise à jour !");
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
