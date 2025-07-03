package com.example.notepad;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        Image icon=new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/example/notepad/notepad.png")));
        stage.getIcons().add(icon);

        core controller = fxmlLoader.getController();
        stage.setTitle("NoteFx");
        stage.setScene(scene);
        stage.show();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            Path currentPath = controller.getCurrentFile() != null
                    ? controller.getCurrentFile().toPath()
                    : Paths.get("Untitled");
            OptFile_handler.draftSave(currentPath);
        }), 2, controller.getAutoSaveDelay(), TimeUnit.MINUTES); // ✅ controller.getAutoSaveDelay()
    }

    @Override
    public  void stop() {
        Platform.exit();
        System.exit(0);
    }

}