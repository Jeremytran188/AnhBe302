module com.example.worldwise.worldwise {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires javafx.graphics;

    opens com.example.worldwise.worldwise to javafx.fxml;
    exports com.example.worldwise.worldwise;
}