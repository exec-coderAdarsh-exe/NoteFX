module com.example.notepad {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.graphics;
    requires org.fxmisc.richtext;

    opens com.example.notepad to javafx.fxml;
    exports com.example.notepad;
}