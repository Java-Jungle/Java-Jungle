module java.jungle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports org.javajungle;
    opens org.javajungle to javafx.fxml, com.fasterxml.jackson.databind;
}