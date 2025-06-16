package com.example.notepad;

import javafx.application.Platform;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class OptFile_handler {

    private static core controller;

    public OptFile_handler(core controller) {
        OptFile_handler.controller = controller;
    }

    public static void saveFile() {
        File file = controller.getCurrentFile();
        if (file == null) saveFileAs();
        else saveToFile(file);
    }

    public static void draftSave(Path originalPath){
        try {
            String userHome=System.getProperty("user.home");
            Path draftDir=Paths.get(userHome,"Documents","Notepad Drafts");
            Files.createDirectories(draftDir);

            String originalFileName=originalPath!=null?originalPath.getFileName().toString():"Untitled";
            String draftFileName= originalFileName.replaceAll("\\.txt$","")+".draft.txt";
            Path draftFile=draftDir.resolve(draftFileName);
            String content=controller.getCodeArea().getText();
            Files.writeString(draftFile,content, StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException _){}
    }

    public static void saveFileAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save File As");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showSaveDialog(controller.getEditorContainer().getScene().getWindow());
        if (file != null) {
            controller.setCurrentFile(file);
            saveToFile(file);
        }
    }

    private static void saveToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(controller.getCodeArea().getText());
            controller.setModified(false);
        } catch (IOException e) {
            showError("Save Error", "Could not save file", e.getMessage());
        }
    }

    public static void exitApplication() {
        draftSave(Path.of("/home/aditya/Pictures"));
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded()) return;
        Platform.exit();
    }

    public static boolean hasUnsavedChanges() {
        return controller.isModified();
    }

    private static boolean confirmAndSaveIfNeeded() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Unsaved Changes");
        alert.setHeaderText("You have unsaved changes. Save before proceeding?");
        alert.getButtonTypes().setAll(
                new ButtonType("Save"),
                new ButtonType("Don't Save"),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            String choice = result.get().getText();
            if ("Save".equals(choice)) {
                saveFile();
                return false;
            } else return "Cancel".equals(choice);
        }
        return true;
    }

    private static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void newFile() {
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded()) return;
        controller.getCodeArea().clear();
        controller.setCurrentFile(null);
        controller.setModified(false);
        controller.refreshLineNumbers();
    }

    public void openFile() {
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded()) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.java", "*.log", "*.md")
        );

        File file = chooser.showOpenDialog(controller.getEditorContainer().getScene().getWindow());
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                controller.getCodeArea().replaceText(sb.toString());

                controller.setCurrentFile(file);
                controller.setModified(false);
                controller.refreshLineNumbers();
            } catch (IOException e) {
                showError("Open Error", "Could not open file", e.getMessage());
            }
        }
    }

    public void printFile() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showError("Print Error", "Printer unavailable", "No printer found.");
            return;
        }

        if (job.showPrintDialog(controller.getEditorContainer().getScene().getWindow())) {
            if (job.printPage(controller.getCodeArea())) {
                job.endJob();
            } else {
                showError("Print Error", "Printing failed", "Could not print the document.");
            }
        }
    }
}
