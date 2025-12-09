package com.taskmaster.controllers;

import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.dao.TaskDAO;
import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.Project;
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

import java.time.LocalDate;

public class TaskManagementController {

    @FXML private Label welcomeLabel;
    @FXML private ComboBox<String> filterStatus;
    @FXML private ComboBox<String> filterPriority;
    @FXML private ComboBox<String> filterProject;
    @FXML private TableView<Task> tasksTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, String> projectColumn;
    @FXML private TableColumn<Task, String> assignedColumn;
    @FXML private TableColumn<Task, String> priorityColumn;
    @FXML private TableColumn<Task, String> statusColumn;
    @FXML private TableColumn<Task, String> dueDateColumn;
    @FXML private TableColumn<Task, Void> actionsColumn;

    private TaskDAO taskDAO = new TaskDAO();
    private ProjectDAO projectDAO = new ProjectDAO();
    private UserDAO userDAO = new UserDAO();
    private ObservableList<Task> allTasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Gestion des Tâches");

        // Remplir les filtres
        filterStatus.getItems().addAll("TOUS", "TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED");
        filterStatus.setValue("TOUS");

        filterPriority.getItems().addAll("TOUS", "LOW", "MEDIUM", "HIGH", "URGENT");
        filterPriority.setValue("TOUS");

        filterProject.getItems().add("TOUS");
        filterProject.setValue("TOUS");

        setupTable();
        loadTasks();
        loadFilters();
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        assignedColumn.setCellValueFactory(new PropertyValueFactory<>("assignedToName"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        // Colorer les priorités
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

        // Colorer les statuts
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
                        case "CANCELLED":
                            setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
                            break;
                    }
                }
            }
        });

        // Boutons d'actions
        actionsColumn.setCellFactory(column -> new TableCell<Task, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    editTask(task);
                });

                deleteButton.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    deleteTask(task);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void loadTasks() {
        allTasks = FXCollections.observableArrayList(taskDAO.findAll());
        tasksTable.setItems(allTasks);
    }

    private void loadFilters() {
        // Charger les projets dans le filtre
        var projects = projectDAO.findAll();
        for (Project p : projects) {
            filterProject.getItems().add(p.getName());
        }
    }

    @FXML
    private void applyFilters() {
        ObservableList<Task> filtered = FXCollections.observableArrayList();

        for (Task task : allTasks) {
            boolean matchStatus = filterStatus.getValue().equals("TOUS") || task.getStatus().equals(filterStatus.getValue());
            boolean matchPriority = filterPriority.getValue().equals("TOUS") || task.getPriority().equals(filterPriority.getValue());
            boolean matchProject = filterProject.getValue().equals("TOUS") || task.getProjectName().equals(filterProject.getValue());

            if (matchStatus && matchPriority && matchProject) {
                filtered.add(task);
            }
        }

        tasksTable.setItems(filtered);
    }

    @FXML
    private void createNewTask() {
        Dialog<Task> dialog = new Dialog<>();
        dialog.setTitle("Créer une Tâche");
        dialog.setHeaderText("Nouvelle tâche");

        ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Formulaire
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField titleField = new TextField();
        titleField.setPromptText("Titre de la tâche");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Description");
        descArea.setPrefRowCount(3);

        ComboBox<String> projectCombo = new ComboBox<>();
        var projects = projectDAO.findAll();
        for (Project p : projects) {
            projectCombo.getItems().add(p.getName());
        }
        projectCombo.setPromptText("Sélectionner un projet");

        ComboBox<String> userCombo = new ComboBox<>();
        var users = userDAO.findAll();
        userCombo.getItems().add("Non assigné");
        for (User u : users) {
            userCombo.getItems().add(u.getUsername());
        }
        userCombo.setValue("Non assigné");

        ComboBox<String> priorityCombo = new ComboBox<>();
        priorityCombo.getItems().addAll("LOW", "MEDIUM", "HIGH", "URGENT");
        priorityCombo.setValue("MEDIUM");

        DatePicker dueDatePicker = new DatePicker();
        dueDatePicker.setValue(LocalDate.now().plusDays(7));

        grid.add(new Label("Titre :"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Description :"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Projet :"), 0, 2);
        grid.add(projectCombo, 1, 2);
        grid.add(new Label("Assigner à :"), 0, 3);
        grid.add(userCombo, 1, 3);
        grid.add(new Label("Priorité :"), 0, 4);
        grid.add(priorityCombo, 1, 4);
        grid.add(new Label("Date limite :"), 0, 5);
        grid.add(dueDatePicker, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (titleField.getText().isEmpty() || projectCombo.getValue() == null) {
                    showError("Veuillez remplir tous les champs obligatoires");
                    return null;
                }

                // Trouver l'ID du projet
                int projectId = -1;
                for (Project p : projects) {
                    if (p.getName().equals(projectCombo.getValue())) {
                        projectId = p.getId();
                        break;
                    }
                }

                // Trouver l'ID de l'utilisateur assigné
                Integer assignedTo = null;
                if (!userCombo.getValue().equals("Non assigné")) {
                    for (User u : users) {
                        if (u.getUsername().equals(userCombo.getValue())) {
                            assignedTo = u.getId();
                            break;
                        }
                    }
                }

                Task newTask = new Task();
                newTask.setTitle(titleField.getText());
                newTask.setDescription(descArea.getText());
                newTask.setProjectId(projectId);
                newTask.setAssignedTo(assignedTo);
                newTask.setPriority(priorityCombo.getValue());
                newTask.setStatus("TODO");
                newTask.setDueDate(dueDatePicker.getValue());
                newTask.setCreatedBy(SessionManager.getCurrentUserId());

                if (taskDAO.create(newTask)) {
                    showSuccess("Tâche créée avec succès !");
                    loadTasks();
                } else {
                    showError("Erreur lors de la création");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editTask(Task task) {
        showInfo("En cours", "Fonction de modification en cours de développement");
    }

    private void deleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la tâche : " + task.getTitle());
        alert.setContentText("Cette action est irréversible !");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (taskDAO.delete(task.getId())) {
                showSuccess("Tâche supprimée");
                loadTasks();
            } else {
                showError("Erreur lors de la suppression");
            }
        }
    }

    @FXML
    private void goBack() {
        loadView("/com/taskmaster/views/admin_dashboard.fxml", "Admin", 1200, 700, true);
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
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}