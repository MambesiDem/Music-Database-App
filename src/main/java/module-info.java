module com.example.task2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;

    opens com.example.task2 to javafx.fxml;
    exports com.example.task2;
}