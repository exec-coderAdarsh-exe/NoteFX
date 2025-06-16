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

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main_ui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), width, height);

        core controller = fxmlLoader.getController();
        controller.registerShortcutKeys(scene);
        stage.setTitle("NoteFx");
        stage.setScene(scene);
        stage.show();
    }
}