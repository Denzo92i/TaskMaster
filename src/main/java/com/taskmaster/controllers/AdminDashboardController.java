package com.taskmaster.controllers;

import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.dao.TaskDAO;
import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.Task;
import com.taskmaster.utils.SessionManager;
import com.taskmaster.utils.NavigationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label userCountLabel;
    @FXML private Label projectCountLabel;
    @FXML private Label taskCountLabel;
    @FXML private TableView<Task> urgentTasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskProjectColumn;
    @FXML private TableColumn<Task, String> taskAssignedColumn;
    @FXML private TableColumn<Task, String> taskDueDateColumn;
    @FXML private TableColumn<Task, String> taskStatusColumn;

    private final UserDAO userDAO = new UserDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final TaskDAO taskDAO = new TaskDAO();

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Bienvenue, " + SessionManager.getCurrentUserFullName());
        }

        loadStatistics();
        setupUrgentTasksTable();
        loadUrgentTasks();
    }

    private void loadStatistics() {
        userCountLabel.setText(String.valueOf(userDAO.count()));
        projectCountLabel.setText(String.valueOf(projectDAO.countByStatus("IN_PROGRESS")));
        taskCountLabel.setText(String.valueOf(taskDAO.countByStatus("IN_PROGRESS")));
    }

    private void setupUrgentTasksTable() {
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        taskProjectColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        taskAssignedColumn.setCellValueFactory(new PropertyValueFactory<>("assignedToName"));
        taskDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        taskStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        taskStatusColumn.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "TODO" -> setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        case "IN_PROGRESS" -> setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                        case "COMPLETED" -> setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                        default -> setStyle("");
                    }
                }
            }
        });
    }

    private void loadUrgentTasks() {
        ObservableList<Task> urgentTasks = FXCollections.observableArrayList();
        for (Task task : taskDAO.findAll()) {
            if ((task.getPriority().equals("HIGH") || task.getPriority().equals("URGENT"))
                    && !task.getStatus().equals("COMPLETED")) {
                urgentTasks.add(task);
            }
        }
        urgentTasksTable.setItems(urgentTasks);
    }

    @FXML
    private void showDashboard() {
        loadStatistics();
        loadUrgentTasks();
    }

    @FXML
    private void showUsers() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/manage_users.fxml", "Gestion Utilisateurs");
    }

    @FXML
    private void showProjects() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/manage_projects.fxml", "Gestion Projets");
    }

    @FXML
    private void openCreateProjectPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmaster/views/create_project_popup.fxml"));
            Parent root = loader.load();

            Stage popupStage = new Stage();
            popupStage.setTitle("Créer un nouveau projet");
            popupStage.setScene(new Scene(root));
            popupStage.initModality(Modality.APPLICATION_MODAL); // Bloque la fenêtre principale
            popupStage.setResizable(false);

            // Rafraîchir après fermeture du popup
            popupStage.setOnHidden(e -> {
                loadStatistics();
                showProjects(); // Rediriger vers la liste des projets
            });

            popupStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le formulaire de création de projet.\nVérifiez que le fichier create_project_popup.fxml existe.");
        }
    }

    @FXML
    private void showTasks() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/manage_tasks.fxml", "Gestion Tâches");
    }

    @FXML
    private void showProfile() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.logout();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmaster/views/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                stage.setScene(new Scene(root, 400, 550));
                stage.setTitle("TaskMaster - Connexion");
                stage.setResizable(false);
                stage.setFullScreen(false);
                stage.setMaximized(false);

            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur de déconnexion", "Impossible de revenir à l'écran de connexion");
            }
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}