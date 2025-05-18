module com.example.notepad {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    opens com.example.notepad to javafx.fxml;
    exports com.example.notepad;
}