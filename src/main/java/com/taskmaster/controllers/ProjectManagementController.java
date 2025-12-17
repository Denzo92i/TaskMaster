package com.taskmaster.controllers;

import com.taskmaster.dao.ProjectDAO;
import com.taskmaster.models.Project;
import com.taskmaster.utils.NavigationUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDate;

public class ProjectManagementController {

    @FXML private Label welcomeLabel;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<Project> projectsTable;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, String> statusColumn;
    @FXML private TableColumn<Project, LocalDate> startDateColumn;
    @FXML private TableColumn<Project, LocalDate> endDateColumn;
    @FXML private TableColumn<Project, Void> actionsColumn;

    private ProjectDAO projectDAO = new ProjectDAO();
    private ObservableList<Project> allProjects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Gestion des Projets");

        statusFilter.getItems().addAll("TOUS", "PLANNED", "IN_PROGRESS", "COMPLETED", "ON_HOLD");
        statusFilter.setValue("TOUS");

        setupTable();
        loadProjects();
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));

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

        actionsColumn.setCellFactory(column -> new TableCell<Project, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    Project project = getTableView().getItems().get(getIndex());
                    editProject(project);
                });

                deleteButton.setOnAction(event -> {
                    Project project = getTableView().getItems().get(getIndex());
                    deleteProject(project);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(5, editButton, deleteButton);
                    setGraphic(box);
                }
            }
        });
    }

    private void loadProjects() {
        allProjects = FXCollections.observableArrayList(projectDAO.findAll());
        projectsTable.setItems(allProjects);
    }

    @FXML
    private void applyFilter() {
        String selected = statusFilter.getValue();

        if (selected == null || selected.equals("TOUS")) {
            projectsTable.setItems(allProjects);
            return;
        }

        ObservableList<Project> filtered = allProjects.filtered(
                p -> p.getStatus().equalsIgnoreCase(selected)
        );

        projectsTable.setItems(filtered);
    }

    @FXML
    private void createNewProject() {
        showError("Fonctionnalité en développement (à implémenter)");
    }

    private void editProject(Project project) {
        showError("Fonctionnalité en développement (à implémenter)");
    }

    private void deleteProject(Project project) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le projet : " + project.getName());
        alert.setContentText("Cette action est irréversible !");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (projectDAO.delete(project.getId())) {
                showSuccess("Projet supprimé !");
                loadProjects();
            } else {
                showError("Erreur lors de la suppression");
            }
        }
    }

    @FXML
    private void goBack() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/admin_dashboard.fxml", "Dashboard");
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}