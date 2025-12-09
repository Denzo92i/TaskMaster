module com.taskmaster {
    // Modules JavaFX
    requires javafx.controls;
    requires javafx.fxml;

    // Module SQL pour MySQL
    requires java.sql;

    // Module pour BCrypt (hashage mots de passe)
    requires jbcrypt;

    // Ouvrir les packages pour JavaFX (reflexion)
    opens com.taskmaster to javafx.fxml;
    opens com.taskmaster.controllers to javafx.fxml;
    opens com.taskmaster.models to javafx.base;

    // Export des packages
    exports com.taskmaster;
    exports com.taskmaster.controllers;
    exports com.taskmaster.models;
}