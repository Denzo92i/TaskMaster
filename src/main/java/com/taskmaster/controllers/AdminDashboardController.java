package com.taskmaster.controllers;

import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.dao.TaskDAO;
import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.Task;
import com.taskmaster.models.User;
import com.taskmaster.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
        loadView("/com/taskmaster/views/manage_users.fxml", "Gestion Utilisateurs", 1200, 700, true);
    }

    @FXML
    private void showProjects() {
        loadView("/com/taskmaster/views/manage_projects.fxml", "Gestion Projets", 1200, 700, true);
    }

    @FXML
    private void showTasks() {
        loadView("/com/taskmaster/views/manage_tasks.fxml", "Gestion Tâches", 1200, 700, true);
    }

    @FXML
    private void showProfile() {
        loadView("/com/taskmaster/views/profile.fxml", "Mon Profil", 1200, 700, true);
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            SessionManager.logout();
            loadView("/com/taskmaster/views/login.fxml", "Connexion", 400, 550, false);
        }
    }

    private void loadView(String fxmlPath, String title, int width, int height, boolean maximized) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root, width, height));
            stage.setTitle("TaskMaster - " + title);
            stage.setMaximized(maximized);

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement", "Impossible de charger la vue : " + e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}