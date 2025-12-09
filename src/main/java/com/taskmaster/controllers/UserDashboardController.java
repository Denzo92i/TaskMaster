package com.taskmaster.controllers;

import com.taskmaster.dao.TaskDAO;
import com.taskmaster.models.Task;
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

public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> projectColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, Void> actionsColumn;

    private TaskDAO taskDAO = new TaskDAO();
    private ObservableList<Task> allMyTasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Bienvenue, " + SessionManager.getCurrentUserFullName());
        }

        statusFilter.setValue("TOUS");
        setupTasksTable();
        loadMyTasks();
    }

    private void setupTasksTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        priorityColumn.setCellFactory(column -> new TableCell<Task, String>() {
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

        statusColumn.setCellFactory(column -> new TableCell<Task, String>() {
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

        actionsColumn.setCellFactory(column -> new TableCell<Task, Void>() {
            private final Button changeStatusButton = new Button("Changer Statut");

            {
                changeStatusButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                changeStatusButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    changeTaskStatus(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(changeStatusButton);
                }
            }
        });
    }

    private void loadMyTasks() {
        int userId = SessionManager.getCurrentUserId();
        allMyTasks = FXCollections.observableArrayList(taskDAO.findByUserId(userId));
        tasksTable.setItems(allMyTasks);
    }

    @FXML
    private void filterTasks() {
        String selectedStatus = statusFilter.getValue();

        if (selectedStatus.equals("TOUS")) {
            tasksTable.setItems(allMyTasks);
        } else {
            ObservableList<Task> filteredTasks = FXCollections.observableArrayList();
            for (Task task : allMyTasks) {
                if (task.getStatus().equals(selectedStatus)) {
                    filteredTasks.add(task);
                }
            }
            tasksTable.setItems(filteredTasks);
        }
    }

    private void changeTaskStatus(Task task) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("IN_PROGRESS", "TODO", "IN_PROGRESS", "COMPLETED");
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText("Tâche : " + task.getTitle());
        dialog.setContentText("Nouveau statut :");

        dialog.showAndWait().ifPresent(newStatus -> {
            if (taskDAO.updateStatus(task.getId(), newStatus)) {
                showSuccess("Statut mis à jour !");
                loadMyTasks();
                filterTasks();
            } else {
                showError("Erreur lors de la mise à jour");
            }
        });
    }

    @FXML
    private void showMyTasks() {
        loadMyTasks();
        filterTasks();
    }

    @FXML
    private void showProjects() {
        showInfo("Fonctionnalité à venir", "La consultation des projets sera disponible prochainement.");
    }

    @FXML
    private void showProfile() {
        showInfo("Fonctionnalité à venir", "Le profil utilisateur sera disponible prochainement.");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Voulez-vous vraiment vous déconnecter ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
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
            showError("Erreur de chargement");
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}