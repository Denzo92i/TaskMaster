package com.taskmaster.controllers;

import javafx.stage.Modality;
import javafx.event.ActionEvent;

import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.Project;
import com.taskmaster.models.User;
import com.taskmaster.utils.NavigationUtils;
import com.taskmaster.utils.SessionManager;

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
import java.util.List;

public class ProjectManagementController {

    @FXML private Label welcomeLabel;
    @FXML private Label feedbackLabel;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, String> startDateColumn;
    @FXML private TableColumn<Project, String> endDateColumn;
    @FXML private TableColumn<Project, Void> actionsColumn;

    private final ProjectDAO projectDAO = new ProjectDAO();
    private final UserDAO userDAO = new UserDAO();
    private ObservableList<Project> allProjects;

    // =========================
    // INITIALISATION
    // =========================
    @FXML
    public void initialize() {
        if (SessionManager.isLoggedIn()) {
            welcomeLabel.setText("Projets - " + SessionManager.getCurrentUserFullName());
        }

        feedbackLabel.setVisible(false);

        statusFilter.setItems(FXCollections.observableArrayList(
                "TOUS",
                "TODO",
                "IN_PROGRESS",
                "COMPLETED",
                "ON_HOLD"
        ));
        statusFilter.setValue("TOUS");

        setupTableColumns();
        loadProjects();
    }

    // =========================
    // TABLE CONFIG
    // =========================
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "TODO" -> setStyle("-fx-background-color:#3498db; -fx-text-fill:white;");
                        case "IN_PROGRESS" -> setStyle("-fx-background-color:#f39c12; -fx-text-fill:white;");
                        case "COMPLETED" -> setStyle("-fx-background-color:#2ecc71; -fx-text-fill:white;");
                        case "ON_HOLD" -> setStyle("-fx-background-color:#95a5a6; -fx-text-fill:white;");
                        default -> setStyle("");
                    }
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox box = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color:#3498db; -fx-text-fill:white;");
                deleteBtn.setStyle("-fx-background-color:#e74c3c; -fx-text-fill:white;");

                editBtn.setOnAction(e -> {
                    Project p = getTableView().getItems().get(getIndex());
                    if (p != null) editProject(p);
                });

                deleteBtn.setOnAction(e -> {
                    Project p = getTableView().getItems().get(getIndex());
                    if (p != null) deleteProject(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // =========================
    // DATA
    // =========================
    private void loadProjects() {
        allProjects = FXCollections.observableArrayList(projectDAO.findAll());
        projectsTable.setItems(allProjects);
    }

    // 🔥 MÉTHODE CLÉ POUR LE POPUP
    public void refreshProjects() {
        loadProjects();
    }

    @FXML
    private void applyFilter() {
        String status = statusFilter.getValue();

        if (status == null || "TOUS".equals(status)) {
            projectsTable.setItems(allProjects);
            return;
        }

        ObservableList<Project> filtered = FXCollections.observableArrayList();
        for (Project p : allProjects) {
            if (status.equals(p.getStatus())) {
                filtered.add(p);
            }
        }
        projectsTable.setItems(filtered);
    }

    // =========================
    // CREATE PROJECT
    // =========================
    @FXML
    private void createNewProject() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Créer un projet");
        dialog.initOwner(welcomeLabel.getScene().getWindow());
        dialog.initModality(Modality.WINDOW_MODAL);

        ButtonType createBtnType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        TextArea descArea = new TextArea();
        DatePicker startPicker = new DatePicker(LocalDate.now());
        DatePicker endPicker = new DatePicker(LocalDate.now().plusMonths(1));
        ComboBox<String> statusCombo = new ComboBox<>();
        ComboBox<String> managerCombo = new ComboBox<>();

        statusCombo.getItems().addAll("TODO", "IN_PROGRESS", "COMPLETED", "ON_HOLD");
        statusCombo.setValue("TODO");

        List<User> users = userDAO.findAll();
        managerCombo.getItems().add("Non assigné");
        users.forEach(u -> managerCombo.getItems().add(u.getFirstName() + " " + u.getLastName()));
        managerCombo.setValue("Non assigné");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Nom :"), nameField);
        grid.addRow(1, new Label("Description :"), descArea);
        grid.addRow(2, new Label("Début :"), startPicker);
        grid.addRow(3, new Label("Fin :"), endPicker);
        grid.addRow(4, new Label("Statut :"), statusCombo);
        grid.addRow(5, new Label("Chef de projet :"), managerCombo);

        dialog.getDialogPane().setContent(grid);

        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createBtnType);
        createBtn.addEventFilter(ActionEvent.ACTION, e -> {
            if (nameField.getText().isBlank() || descArea.getText().isBlank()) {
                showError("Champs obligatoires manquants");
                e.consume();
                return;
            }

            if (endPicker.getValue().isBefore(startPicker.getValue())) {
                showError("Dates invalides");
                e.consume();
                return;
            }

            Integer managerId = null;
            if (!"Non assigné".equals(managerCombo.getValue())) {
                managerId = users.stream()
                        .filter(u -> (u.getFirstName() + " " + u.getLastName()).equals(managerCombo.getValue()))
                        .map(User::getId)
                        .findFirst()
                        .orElse(null);
            }

            Project p = new Project();
            p.setName(nameField.getText());
            p.setDescription(descArea.getText());
            p.setStartDate(startPicker.getValue());
            p.setEndDate(endPicker.getValue());
            p.setStatus(statusCombo.getValue());
            p.setManagerId(managerId);
            p.setCreatedBy(SessionManager.getCurrentUserId());

            if (projectDAO.create(p)) {
                dialog.close();
                refreshProjects();
                showSuccess("Projet créé avec succès");
            } else {
                showError("Erreur lors de la création");
                e.consume();
            }
        });

        dialog.showAndWait();
    }

    // =========================
    // ACTIONS
    // =========================
    private void editProject(Project project) {
        showInfo("Info", "Fonction en cours de développement");
    }

    private void deleteProject(Project project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer « " + project.getName() + " » ?",
                ButtonType.OK, ButtonType.CANCEL);

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            if (projectDAO.delete(project.getId())) {
                refreshProjects();
                showSuccess("Projet supprimé");
            } else {
                showError("Erreur suppression");
            }
        }
    }

    // =========================
    // NAV
    // =========================
    @FXML
    private void goBack() {
        if (SessionManager.getCurrentUser() != null && SessionManager.getCurrentUser().isAdmin()) {
            NavigationUtils.navigateTo(welcomeLabel,
                    "/com/taskmaster/views/admin_dashboard.fxml",
                    "Dashboard Admin");
        } else {
            NavigationUtils.navigateTo(welcomeLabel,
                    "/com/taskmaster/views/user_dashboard.fxml",
                    "Dashboard");
        }
    }

    // =========================
    // FEEDBACK
    // =========================
    private void showFeedback(String msg, String color) {
        feedbackLabel.setText(msg);
        feedbackLabel.setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;");
        feedbackLabel.setVisible(true);

        PauseTransition pt = new PauseTransition(Duration.seconds(2.5));
        pt.setOnFinished(e -> feedbackLabel.setVisible(false));
        pt.play();
    }

    private void showSuccess(String msg) {
        showFeedback(msg, "green");
    }

    private void showError(String msg) {
        showFeedback(msg, "red");
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg);
        a.setTitle(title);
        a.showAndWait();
    }
}
