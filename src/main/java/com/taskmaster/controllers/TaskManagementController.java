package com.taskmaster.controllers;

import javafx.stage.Modality;
import javafx.event.ActionEvent;
import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.dao.TaskDAO;
import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.Project;
import com.taskmaster.models.Task;
import com.taskmaster.models.User;
import com.taskmaster.utils.NavigationUtils;
import com.taskmaster.utils.SessionManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import java.time.LocalDate;

public class TaskManagementController {

    @FXML private Label welcomeLabel;
    @FXML private Label feedbackLabel;
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

    private final TaskDAO taskDAO = new TaskDAO();
    private final ProjectDAO projectDAO = new ProjectDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ObservableList<Task> allTasks = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Gestion des Tâches");
        feedbackLabel.setVisible(false);

        filterStatus.getItems().addAll("TOUS", "TODO", "IN_PROGRESS", "COMPLETED", "CANCELLED");
        filterStatus.setValue("TOUS");

        filterPriority.getItems().addAll("TOUS", "LOW", "MEDIUM", "HIGH", "URGENT");
        filterPriority.setValue("TOUS");

        filterProject.getItems().add("TOUS");
        filterProject.setValue("TOUS");

        setupTable();
        loadFilters();
        loadTasks();
    }

    private void setupTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        projectColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getProjectName() != null ? cellData.getValue().getProjectName() : "Non défini"
                )
        );

        assignedColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getAssignedToName() != null ? cellData.getValue().getAssignedToName() : "Non assigné"
                )
        );

        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        // Styles priority avec classes CSS du thème
        priorityColumn.setCellFactory(col -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(value);
                // Utilisation des couleurs du thème
                switch (value) {
                    case "URGENT" -> setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-alignment: CENTER;");
                    case "HIGH" -> setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-alignment: CENTER;");
                    case "MEDIUM" -> setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-alignment: CENTER;");
                    case "LOW" -> setStyle("-fx-background-color: #64748B; -fx-text-fill: white; -fx-alignment: CENTER;");
                }
            }
        });

        // Styles status avec classes CSS du thème
        statusColumn.setCellFactory(col -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(value);
                switch (value) {
                    case "TODO" -> setStyle("-fx-background-color: #6366F1; -fx-text-fill: white; -fx-alignment: CENTER;");
                    case "IN_PROGRESS" -> setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-alignment: CENTER;");
                    case "COMPLETED" -> setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-alignment: CENTER;");
                    case "CANCELLED" -> setStyle("-fx-background-color: #64748B; -fx-text-fill: white; -fx-alignment: CENTER;");
                }
            }
        });

        // Actions buttons avec style du thème
        actionsColumn.setCellFactory(col -> new TableCell<Task, Void>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button deleteBtn = new Button("🗑️ Supprimer");
            {
                // Appliquer les classes CSS du thème
                editBtn.getStyleClass().addAll("button");
                deleteBtn.getStyleClass().addAll("button", "danger");

                editBtn.setOnAction(e -> editTask(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> deleteTask(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8, editBtn, deleteBtn);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadTasks() {
        allTasks.setAll(taskDAO.findAll());
        tasksTable.setItems(allTasks);
    }

    private void loadFilters() {
        for (Project p : projectDAO.findAll()) {
            filterProject.getItems().add(p.getName());
        }
    }

    @FXML
    private void applyFilters() {
        ObservableList<Task> filtered = FXCollections.observableArrayList();
        for (Task t : allTasks) {
            boolean ok =
                    (filterStatus.getValue().equals("TOUS") || (t.getStatus() != null && t.getStatus().equals(filterStatus.getValue()))) &&
                            (filterPriority.getValue().equals("TOUS") || (t.getPriority() != null && t.getPriority().equals(filterPriority.getValue()))) &&
                            (filterProject.getValue().equals("TOUS") || (t.getProjectName() != null && t.getProjectName().equals(filterProject.getValue())));
            if (ok) filtered.add(t);
        }
        tasksTable.setItems(filtered);
    }

    @FXML
    private void createNewTask() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Créer une tâche");
        dialog.initOwner(welcomeLabel.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);

        // Appliquer le style du thème à la dialog
        dialog.getDialogPane().getStyleClass().add("dialog-pane");

        ButtonType createBtnType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        TextField titleField = new TextField();
        titleField.setPromptText("Titre de la tâche");
        TextArea descArea = new TextArea();
        descArea.setPromptText("Description détaillée");
        descArea.setPrefRowCount(4);
        ComboBox<String> projectCombo = new ComboBox<>();
        ComboBox<String> userCombo = new ComboBox<>();
        ComboBox<String> priorityCombo = new ComboBox<>();
        DatePicker dueDatePicker = new DatePicker(LocalDate.now().plusDays(7));

        var projects = projectDAO.findAll();
        var users = userDAO.findAll();
        projects.forEach(p -> projectCombo.getItems().add(p.getName()));
        userCombo.getItems().add("Non assigné");
        users.forEach(u -> userCombo.getItems().add(u.getUsername()));
        userCombo.setValue("Non assigné");
        priorityCombo.getItems().addAll("LOW", "MEDIUM", "HIGH", "URGENT");
        priorityCombo.setValue("MEDIUM");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.addRow(0, new Label("Titre :"), titleField);
        grid.addRow(1, new Label("Description :"), descArea);
        grid.addRow(2, new Label("Projet :"), projectCombo);
        grid.addRow(3, new Label("Assigné à :"), userCombo);
        grid.addRow(4, new Label("Priorité :"), priorityCombo);
        grid.addRow(5, new Label("Date limite :"), dueDatePicker);

        // Appliquer les styles aux labels
        grid.getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(label -> ((Label) label).getStyleClass().add("form-label"));

        dialog.getDialogPane().setContent(grid);

        Button createButton = (Button) dialog.getDialogPane().lookupButton(createBtnType);
        createButton.addEventFilter(ActionEvent.ACTION, e -> {
            if (titleField.getText().isBlank() || projectCombo.getValue() == null) {
                showError("Titre et projet sont obligatoires !");
                e.consume();
                return;
            }

            int projectId = projects.stream()
                    .filter(p -> p.getName().equals(projectCombo.getValue()))
                    .findFirst().map(Project::getId).orElse(-1);

            Integer assignedTo = users.stream()
                    .filter(u -> u.getUsername().equals(userCombo.getValue()))
                    .findFirst().map(User::getId).orElse(null);

            Task task = new Task();
            task.setTitle(titleField.getText());
            task.setDescription(descArea.getText());
            task.setProjectId(projectId);
            task.setAssignedTo(assignedTo);
            task.setPriority(priorityCombo.getValue());
            task.setStatus("TODO");
            task.setDueDate(dueDatePicker.getValue());
            task.setCreatedBy(SessionManager.getCurrentUserId());

            if (taskDAO.create(task)) {
                dialog.close();
                loadTasks();
                showSuccess("✓ Tâche créée avec succès !");
            } else {
                showError("✗ Erreur lors de la création");
                e.consume();
            }
        });

        dialog.showAndWait();
    }

    private void editTask(Task task) {
        showInfo("Information", "Fonction d'édition en cours de développement");
    }

    private void deleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer la tâche « " + task.getTitle() + " » ?",
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText(null);
        alert.initOwner(welcomeLabel.getScene().getWindow());
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (taskDAO.delete(task.getId())) {
                loadTasks();
                showSuccess("✓ Tâche supprimée avec succès");
            } else {
                showError("✗ Erreur lors de la suppression");
            }
        }
    }

    @FXML
    private void goBack() {
        NavigationUtils.navigateTo(welcomeLabel,
                "/com/taskmaster/views/admin_dashboard.fxml",
                "Dashboard");
    }

    // Messages temporaires avec style du thème
    private void showFeedback(String msg, String styleClass) {
        feedbackLabel.setText(msg);
        feedbackLabel.getStyleClass().clear();
        feedbackLabel.getStyleClass().add(styleClass);
        feedbackLabel.setVisible(true);
        PauseTransition pt = new PauseTransition(Duration.seconds(3));
        pt.setOnFinished(e -> feedbackLabel.setVisible(false));
        pt.play();
    }

    private void showSuccess(String msg) {
        showFeedback(msg, "success-message");
        feedbackLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 14px;");
    }

    private void showError(String msg) {
        showFeedback(msg, "error-message");
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title);
        a.setHeaderText(null);
        a.getDialogPane().getStyleClass().add("dialog-pane");
        a.showAndWait();
    }
}