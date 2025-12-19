package com.taskmaster.controllers;

import com.taskmaster.dao.TaskDAO;
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

        // Styles avec couleurs du thème
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
                            setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-alignment: CENTER;");
                            break;
                        case "HIGH":
                            setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-alignment: CENTER;");
                            break;
                        case "MEDIUM":
                            setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-alignment: CENTER;");
                            break;
                        case "LOW":
                            setStyle("-fx-background-color: #64748B; -fx-text-fill: white; -fx-alignment: CENTER;");
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
                            setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-alignment: CENTER;");
                            break;
                        case "IN_PROGRESS":
                            setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-alignment: CENTER;");
                            break;
                        case "COMPLETED":
                            setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-alignment: CENTER;");
                            break;
                    }
                }
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<Task, Void>() {
            private final Button changeStatusButton = new Button("📝 Changer Statut");

            {
                changeStatusButton.getStyleClass().addAll("button");
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
        dialog.initOwner(welcomeLabel.getScene().getWindow());
        dialog.setTitle("Changer le statut");
        dialog.setHeaderText(null);
        dialog.setContentText("Tâche : " + task.getTitle() + "\nNouveau statut :");

        // 🎨 Appliquer le thème au dialog
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        dialog.showAndWait().ifPresent(newStatus -> {
            if (taskDAO.updateStatus(task.getId(), newStatus)) {
                showSuccess("✓ Statut mis à jour !");
                loadMyTasks();
                filterTasks();
            } else {
                showError("✗ Erreur lors de la mise à jour");
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
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/user_projects.fxml", "Mes Projets");
    }

    @FXML
    private void showProfile() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(welcomeLabel.getScene().getWindow());
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");

        // 🎨 Appliquer le thème CSS au dialog de confirmation
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        if (alert.showAndWait().get() == ButtonType.OK) {
            SessionManager.logout();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmaster/views/login.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                Scene scene = new Scene(root, 400, 550);

                // 🎨 CORRECTION: Appliquer le thème CSS à la page de connexion
                scene.getStylesheets().add(
                        getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
                );

                stage.setScene(scene);
                stage.setTitle("TaskMaster - Connexion");
                stage.setResizable(false);
                stage.setFullScreen(false);
                stage.setMaximized(false);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(welcomeLabel.getScene().getWindow());
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 🎨 Appliquer le thème
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(welcomeLabel.getScene().getWindow());
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 🎨 Appliquer le thème
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(welcomeLabel.getScene().getWindow());
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // 🎨 Appliquer le thème
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/taskmaster/views/theme.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");

        alert.showAndWait();
    }
}