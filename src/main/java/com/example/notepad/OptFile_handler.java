package com.example.notepad;

import javafx.application.Platform;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.Optional;

public class OptFile_handler {

    private final core controller;

    public OptFile_handler(core controller) {
        this.controller = controller;
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

    public void saveFile() {
        File file = controller.getCurrentFile();
        if (file == null) saveFileAs();
        else saveToFile(file);
    }

    public void saveFileAs() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save File As");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = chooser.showSaveDialog(controller.getEditorContainer().getScene().getWindow());
        if (file != null) {
            controller.setCurrentFile(file);
            saveToFile(file);
        }
    }

    private void saveToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(controller.getCodeArea().getText());
            controller.setModified(false);
        } catch (IOException e) {
            showError("Save Error", "Could not save file", e.getMessage());
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

    public void exitApplication() {
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded()) return;
        Platform.exit();
    }

    private boolean hasUnsavedChanges() {
        return controller.isModified();
    }

    private boolean confirmAndSaveIfNeeded() {
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

    private void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
