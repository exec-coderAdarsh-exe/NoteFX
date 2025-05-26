package com.example.notepad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/*
    Launching everything and attaching to the stage.
 */

public class Main extends Application {

    public static final int width=800;
    public static final int height=450;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main_ui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle("Notepad");
        stage.setScene(scene);
        stage.show();
    }

    public Main() {
        launch();
    }
}