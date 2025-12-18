package com.taskmaster.controllers;

import com.taskmaster.utils.NavigationUtils;
import com.taskmaster.dao.UserDAO;
import com.taskmaster.models.User;
import com.taskmaster.utils.PasswordHasher;
import com.taskmaster.utils.Validator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class UserManagementController {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilter;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, Boolean> activeColumn;
    @FXML private TableColumn<User, Void> actionsColumn;

    private UserDAO userDAO = new UserDAO();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Gestion des Utilisateurs");

        // Remplir le filtre de rôle
        roleFilter.getItems().addAll("TOUS", "ADMIN", "USER");
        roleFilter.setValue("TOUS");

        setupTable();
        loadUsers();
    }

    private void setupTable() {
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getFirstName() + " " + cellData.getValue().getLastName()
                )
        );
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));

        activeColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeColumn.setCellFactory(column -> new TableCell<User, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(active ? "✅ Actif" : "❌ Inactif");
                    setStyle(active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
                }
            }
        });

        roleColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    if (role.equals("ADMIN")) {
                        setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                    }
                }
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<User, Void>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    editUser(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
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

    private void loadUsers() {
        allUsers = FXCollections.observableArrayList(userDAO.findAll());
        usersTable.setItems(allUsers);
    }

    @FXML
    private void searchUsers() {
        String searchTerm = searchField.getText().trim();
        String role = roleFilter.getValue().equals("TOUS") ? null : roleFilter.getValue();

        if (searchTerm.isEmpty() && (role == null || role.equals("TOUS"))) {
            loadUsers();
        } else {
            var results = userDAO.search(searchTerm, role);
            usersTable.setItems(FXCollections.observableArrayList(results));
        }
    }

    @FXML
    private void createNewUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.initOwner(welcomeLabel.getScene().getWindow()); // Attacher à la fenêtre parent
        dialog.setTitle("Créer un Utilisateur");
        dialog.setHeaderText("Nouvel utilisateur");

        ButtonType createButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("USER", "ADMIN");
        roleCombo.setValue("USER");

        // Label d'erreur en rouge
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

        grid.add(errorLabel, 0, 0, 2, 1); // Span sur 2 colonnes
        grid.add(new Label("Prénom :"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Nom :"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Email :"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Username :"), 0, 4);
        grid.add(usernameField, 1, 4);
        grid.add(new Label("Mot de passe :"), 0, 5);
        grid.add(passwordField, 1, 5);
        grid.add(new Label("Rôle :"), 0, 6);
        grid.add(roleCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Empêcher la fermeture du dialog en cas d'erreur de validation
        javafx.scene.control.Button createButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(createButtonType);
        createButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Réinitialiser le message d'erreur et son style
            errorLabel.setText("");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

            // Validation
            if (!Validator.isValidName(firstNameField.getText())) {
                errorLabel.setText("❌ Prénom invalide");
                event.consume();
                return;
            }
            if (!Validator.isValidName(lastNameField.getText())) {
                errorLabel.setText("❌ Nom invalide");
                event.consume();
                return;
            }
            if (!Validator.isValidEmail(emailField.getText())) {
                errorLabel.setText("❌ Email invalide");
                event.consume();
                return;
            }
            if (!Validator.isValidUsername(usernameField.getText())) {
                errorLabel.setText("❌ Username invalide (3-20 caractères)");
                event.consume();
                return;
            }
            if (!Validator.isStrongPassword(passwordField.getText())) {
                errorLabel.setText("❌ " + Validator.getPasswordError(passwordField.getText()));
                event.consume();
                return;
            }

            if (userDAO.emailExists(emailField.getText())) {
                errorLabel.setText("❌ Cet email existe déjà");
                event.consume();
                return;
            }
            if (userDAO.usernameExists(usernameField.getText())) {
                errorLabel.setText("❌ Ce username existe déjà");
                event.consume();
                return;
            }

            // Si toutes les validations passent, créer l'utilisateur
            User newUser = new User();
            newUser.setFirstName(firstNameField.getText().trim());
            newUser.setLastName(lastNameField.getText().trim());
            newUser.setEmail(emailField.getText().trim());
            newUser.setUsername(usernameField.getText().trim());
            newUser.setPassword(PasswordHasher.hashPassword(passwordField.getText()));
            newUser.setRole(roleCombo.getValue());
            newUser.setActive(true);

            if (userDAO.create(newUser)) {
                errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                errorLabel.setText("✅ Utilisateur créé avec succès !");
                loadUsers();

                // Fermer le dialog après 1 seconde
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                pause.setOnFinished(e -> dialog.close());
                pause.play();
            } else {
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                errorLabel.setText("❌ Erreur lors de la création");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void editUser(User user) {
        Dialog<User> dialog = new Dialog<>();
        dialog.initOwner(welcomeLabel.getScene().getWindow()); // Attacher à la fenêtre parent
        dialog.setTitle("Modifier l'utilisateur");
        dialog.setHeaderText("Modifier : " + user.getUsername());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Label d'erreur en rouge
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

        TextField firstNameField = new TextField(user.getFirstName());
        TextField lastNameField = new TextField(user.getLastName());
        TextField emailField = new TextField(user.getEmail());
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("USER", "ADMIN");
        roleCombo.setValue(user.getRole());
        CheckBox activeCheck = new CheckBox();
        activeCheck.setSelected(user.isActive());

        grid.add(errorLabel, 0, 0, 2, 1); // Span sur 2 colonnes
        grid.add(new Label("Prénom :"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Nom :"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Email :"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Rôle :"), 0, 4);
        grid.add(roleCombo, 1, 4);
        grid.add(new Label("Actif :"), 0, 5);
        grid.add(activeCheck, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Empêcher la fermeture du dialog en cas d'erreur de validation
        javafx.scene.control.Button saveButton = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Réinitialiser le message d'erreur et son style
            errorLabel.setText("");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

            // Validation
            if (!Validator.isValidName(firstNameField.getText())) {
                errorLabel.setText("❌ Prénom invalide");
                event.consume();
                return;
            }
            if (!Validator.isValidName(lastNameField.getText())) {
                errorLabel.setText("❌ Nom invalide");
                event.consume();
                return;
            }
            if (!Validator.isValidEmail(emailField.getText())) {
                errorLabel.setText("❌ Email invalide");
                event.consume();
                return;
            }

            user.setFirstName(firstNameField.getText().trim());
            user.setLastName(lastNameField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setRole(roleCombo.getValue());
            user.setActive(activeCheck.isSelected());

            if (userDAO.update(user)) {
                errorLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                errorLabel.setText("✅ Utilisateur modifié avec succès !");
                loadUsers();

                // Fermer le dialog après 1 seconde
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
                pause.setOnFinished(e -> dialog.close());
                pause.play();
            } else {
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                errorLabel.setText("❌ Erreur lors de la modification");
                event.consume();
            }
        });

        dialog.showAndWait();
    }

    private void deleteUser(User user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(welcomeLabel.getScene().getWindow()); // Attacher à la fenêtre parent
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer l'utilisateur : " + user.getUsername());
        alert.setContentText("Cette action est irréversible !");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (userDAO.delete(user.getId())) {
                showSuccess("Utilisateur supprimé");
                loadUsers();
            } else {
                showError("Erreur lors de la suppression");
            }
        }
    }

    @FXML
    private void goBack() {
        NavigationUtils.navigateTo(welcomeLabel, "/com/taskmaster/views/admin_dashboard.fxml", "Dashboard");
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(welcomeLabel.getScene().getWindow()); // Attacher à la fenêtre parent
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(welcomeLabel.getScene().getWindow()); // Attacher à la fenêtre parent
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}