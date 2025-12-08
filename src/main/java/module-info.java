module com.example.taskmaster {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.taskmaster to javafx.fxml;
    exports com.example.taskmaster;
}