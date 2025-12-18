package com.taskmaster.controllers;

import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.dao.TaskDAO;
import com.taskmaster.models.Project;
import com.taskmaster.models.Task;
import com.taskmaster.utils.SessionManager;
import com.taskmaster.utils.NavigationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserProjectsController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, String> descriptionColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, String> startDateColumn;
    @FXML private TableColumn<Project, String> endDateColumn;
    @FXML private TableColumn<Project, Void> actionsColumn;

    @FXML private Label selectedProjectLabel;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> taskTitleColumn;
    @FXML private TableColumn<Task, String> taskPriorityColumn;
    @FXML private TableColumn<Task, String> taskStatusColumn;
    @FXML private TableColumn<Task, String> taskDueDateColumn;

    private ProjectDAO projectDAO = new ProjectDAO();
    private TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Project> userProjects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Mes Projets");
        setupProjectsTable();
        setupTasksTable();
        loadUserProjects();
    }

    private void setupProjectsTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        // Colorer les statuts
        statusColumn.setCellFactory(column -> new TableCell<Project, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "PLANNED":
                            setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                            break;
                        case "IN_PROGRESS":
                            setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                            break;
                        case "COMPLETED":
                            setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                            break;
                        case "ON_HOLD":
                            setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                            break;
                    }
                }
            }
        });

        // Bouton pour voir les détails
        actionsColumn.setCellFactory(column -> new TableCell<Project, Void>() {
            private final Button viewButton = new Button("Voir mes tâches");

            {
                viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                viewButton.setOnAction(event -> {
                    Project project = getTableView().getItems().get(getIndex());
                    showProjectTasks(project);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });

        // Listener pour sélection de projet
        projectsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showProjectTasks(newSelection);
            }
        });
    }

    private void setupTasksTable() {
        taskTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        taskPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        taskStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        taskDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        // Colorer les priorités
        taskPriorityColumn.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    switch (priority) {
                        case "URGENT":
                            setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                            break;
                        case "HIGH":
                            setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
                            break;
                        case "MEDIUM":
                            setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                            break;
                        case "LOW":
                            setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                            break;
                    }
                }
            }
        });

        // Colorer les statuts
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
                        case "TODO":
                            setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                            break;
                        case "IN_PROGRESS":
                            setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                            break;
                        case "COMPLETED":
                            setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
                            break;
                    }
                }
            }
        });
    }

    private void loadUserProjects() {
        int userId = SessionManager.getCurrentUserId();

        // Récupérer tous les projets où l'utilisateur a des tâches
        var allTasks = taskDAO.findByUserId(userId);

        // Extraire les IDs de projets uniques
        var projectIds = allTasks.stream()
                .map(Task::getProjectId)
                .distinct()
                .toList();

        // Charger les projets correspondants
        userProjects.clear();
        for (Integer projectId : projectIds) {
            Project project = projectDAO.findById(projectId);
            if (project != null) {
                userProjects.add(project);
            }
        }

        projectsTable.setItems(userProjects);

        if (userProjects.isEmpty()) {
            selectedProjectLabel.setText("Vous n'êtes assigné à aucun projet pour le moment");
        }
    }

    private void showProjectTasks(Project project) {
        selectedProjectLabel.setText("Projet : " + project.getName() + " - Mes tâches");

        int userId = SessionManager.getCurrentUserId();

        // Récupérer les tâches de l'utilisateur pour ce projet
        var allUserTasks = taskDAO.findByUserId(userId);
        var projectTasks = allUserTasks.stream()
                .filter(task -> task.getProjectId() == project.getId())
                .toList();

        tasksTable.setItems(FXCollections.observableArrayList(projectTasks));
    }

    @FXML
    private void goBack() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/user_dashboard.fxml", "Dashboard Utilisateur");
    }

    @FXML
    private void refreshProjects() {
        loadUserProjects();
        selectedProjectLabel.setText("Sélectionnez un projet pour voir vos tâches");
        tasksTable.setItems(FXCollections.observableArrayList());
    }
}