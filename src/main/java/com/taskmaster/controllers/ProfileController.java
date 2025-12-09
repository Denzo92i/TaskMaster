package com.taskmaster.controllers;

import com.taskmaster.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ProfileController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel;

    private User user;

    // Cette méthode sera appelée depuis AdminDashboardController
    public void setUser(User user) {
        this.user = user;
        loadUserData();
    }

    // Remplit les labels avec les infos de l'utilisateur
    private void loadUserData() {
        if (user != null) {
            nameLabel.setText(user.getFullName());
            emailLabel.setText(user.getEmail());
            roleLabel.setText(user.getRole());
        }
    }
}
