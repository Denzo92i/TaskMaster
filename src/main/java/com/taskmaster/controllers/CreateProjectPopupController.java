package com.taskmaster.controllers;

import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.Project;
import com.taskmaster.models.User;
import com.taskmaster.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.collections.FXCollections;

import java.time.LocalDate;
import java.util.List;

public class CreateProjectPopupController {

    @FXML private TextField projectNameField;
    @FXML private TextArea projectDescriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<User> managerComboBox;
    @FXML private Label errorLabel;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final UserDAO userDAO = new UserDAO();

    private StackPane overlay;
    private ProjectManagementController parentController;

    public void setOverlay(StackPane overlay) {
        this.overlay = overlay;
    }

    public void setParentController(ProjectManagementController parentController) {
        this.parentController = parentController;
    }

    @FXML
    public void initialize() {
        // Initialiser les statuts
        statusComboBox.setItems(FXCollections.observableArrayList(
                "TODO",
                "IN_PROGRESS",
                "COMPLETED",
                "ON_HOLD"
        ));
        statusComboBox.setValue("TODO");

        // Charger les utilisateurs pour le chef de projet
        loadManagers();

        // Définir la date de début par défaut à aujourd'hui
        startDatePicker.setValue(LocalDate.now());
    }

    private void loadManagers() {
        List<User> users = userDAO.findAll();
        managerComboBox.setItems(FXCollections.observableArrayList(users));

        // Afficher le nom complet dans la ComboBox
        managerComboBox.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getFirstName() + " " + user.getLastName());
                }
            }
        });

        managerComboBox.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getFirstName() + " " + user.getLastName());
                }
            }
        });
    }

    @FXML
    private void handleCreateProject() {
        // Réinitialiser le message d'erreur
        hideError();

        // Validation
        String errorMessage = validateForm();
        if (errorMessage != null) {
            showError(errorMessage);
            return;
        }

        try {
            // Créer le projet
            Project project = new Project();
            project.setName(projectNameField.getText().trim());
            project.setDescription(projectDescriptionArea.getText().trim());
            project.setStartDate(startDatePicker.getValue());
            project.setEndDate(endDatePicker.getValue());
            project.setStatus(statusComboBox.getValue());

            // Définir le chef de projet
            User selectedManager = managerComboBox.getValue();
            if (selectedManager != null) {
                project.setManagerId(selectedManager.getId());
            }

            // Définir l'utilisateur qui crée le projet
            if (SessionManager.getCurrentUser() != null) {
                project.setCreatedBy(SessionManager.getCurrentUser().getId());
            } else {
                showError("Erreur : utilisateur non connecté");
                return;
            }

            // Sauvegarder dans la base de données
            boolean success = projectDAO.create(project);

            if (success) {
                System.out.println("✓ Projet créé avec succès : " + project.getName());
                closeModal();
                if (parentController != null) {
                    parentController.refreshProjects();
                }
            } else {
                showError("Erreur lors de la création du projet dans la base de données");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur inattendue : " + e.getMessage());
        }
    }

    private String validateForm() {
        // Vérifier le nom
        if (projectNameField.getText() == null || projectNameField.getText().trim().isEmpty()) {
            return "Le nom du projet est obligatoire.";
        }

        if (projectNameField.getText().trim().length() < 3) {
            return "Le nom du projet doit contenir au moins 3 caractères.";
        }

        // Vérifier la description
        if (projectDescriptionArea.getText() == null || projectDescriptionArea.getText().trim().isEmpty()) {
            return "La description est obligatoire.";
        }

        if (projectDescriptionArea.getText().trim().length() < 10) {
            return "La description doit contenir au moins 10 caractères.";
        }

        // Vérifier la date de début
        if (startDatePicker.getValue() == null) {
            return "La date de début est obligatoire.";
        }

        // Vérifier la date de fin
        if (endDatePicker.getValue() == null) {
            return "La date de fin est obligatoire.";
        }

        // Vérifier que la date de fin est après la date de début
        if (endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            return "La date de fin doit être après la date de début.";
        }

        // Vérifier le statut
        if (statusComboBox.getValue() == null) {
            return "Le statut est obligatoire.";
        }

        // Vérifier le chef de projet
        if (managerComboBox.getValue() == null) {
            return "Le chef de projet est obligatoire.";
        }

        return null; // Aucune erreur
    }

    @FXML
    private void handleCancel() {
        closeModal();
    }

    private void showError(String message) {
        errorLabel.setText("❌ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private void closeModal() {
        if (overlay != null && overlay.getParent() instanceof StackPane) {
            StackPane parent = (StackPane) overlay.getParent();
            parent.getChildren().remove(overlay);
        }
    }
}