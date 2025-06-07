module com.example.notepad {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.graphics;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires java.logging;
    requires java.desktop;
    requires org.controlsfx.controls;

    opens com.example.notepad to javafx.fxml;
    exports com.example.notepad;
}