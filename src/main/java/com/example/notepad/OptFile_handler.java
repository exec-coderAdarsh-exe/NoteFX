package com.example.notepad;

/*
    Handling file option in main stage.
 */

import javafx.application.Platform;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.CodeArea;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

public class OptFile_handler {
    private static final VBox editorContainer=core.editorContainer;
    private static final CodeArea codeArea=core.codeArea;
    private static final Map<Integer, String> lineColorMap=core.lineColorMap;
    private static final Map<String, List<Integer>> colorGroups=core.colorGroups;
    private static File currentFile=core.currentFile;
    private static boolean isModified=core.isModified;
    private static final List<String> colorCycle=core.colorCycle;


    public void FileMenu_New() {
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded()) return;

        codeArea.clear();
        lineColorMap.clear();
        colorGroups.values().forEach(List::clear);
        currentFile = null;
        isModified = false;

        codeArea.setParagraphGraphicFactory(core.lineNumberGraphicFactory());
    }

    private static String getNextColor(String current) {
        if (current == null || !colorCycle.contains(current)) return colorCycle.getFirst();
        int index = colorCycle.indexOf(current);
        return colorCycle.get((index + 1) % colorCycle.size());
    }

    private static String getLineNumberStyle(int line) {
        return switch (lineColorMap.getOrDefault(line, "NONE")) {
            case "YELLOW" -> "-fx-background-color: yellow; -fx-text-fill: black;";
            case "GREEN" -> "-fx-background-color: lightgreen; -fx-text-fill: black;";
            case "RED" -> "-fx-background-color: tomato; -fx-text-fill: white;";
            default -> "-fx-background-color: transparent; -fx-text-fill: grey;";
        };
    }

    public static IntFunction<Node> lineNumberGraphicFactory() {
        return line -> {
            int lineIndex = line + 1;
            Label label = new Label(String.valueOf(lineIndex));
            label.setStyle(getLineNumberStyle(lineIndex));
            label.setMinWidth(40);
            label.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));

            label.setOnMouseClicked(_ -> {
                String current = lineColorMap.get(lineIndex);
                String next = getNextColor(current);

                if (current != null && colorGroups.containsKey(current)) {
                    colorGroups.get(current).remove((Integer) lineIndex);
                }

                if (!"NONE".equals(next)) {
                    lineColorMap.put(lineIndex, next);
                    colorGroups.get(next).add(lineIndex);
                } else {
                    lineColorMap.remove(lineIndex);
                }

                codeArea.setParagraphGraphicFactory(lineNumberGraphicFactory());
            });

            return label;
        };
    }

    public void FileMenu_Open() {
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.java", "*.log", "*.md")
        );

        File file = fileChooser.showOpenDialog(editorContainer.getScene().getWindow());
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                codeArea.replaceText(sb.toString());

                lineColorMap.clear();
                colorGroups.values().forEach(List::clear);
                currentFile = file;
                isModified = false;
                codeArea.setParagraphGraphicFactory(core.lineNumberGraphicFactory());
            } catch (IOException e) {
                showError("Open Error", "Could not open file", e.getMessage());
            }
        }
    }

    public void FileMenu_Save() {
        if (currentFile == null) {
            FileMenu_SaveAS();
        } else {
            saveToFile(currentFile);
        }
    }

    public void FileMenu_SaveAS() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(editorContainer.getScene().getWindow());
        if (file != null) {
            currentFile = file;
            saveToFile(file);
        }
    }

    private void saveToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(codeArea.getText());
            isModified = false;
        } catch (IOException e) {
            showError("Save Error", "Could not save file", e.getMessage());
        }
    }

    public void FileMenu_Print() {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            showError("Print Error", "Printer unavailable", "No printer found.");
            return;
        }

        if (job.showPrintDialog(editorContainer.getScene().getWindow())) {
            if (job.printPage(codeArea)) {
                job.endJob();
            } else {
                showError("Print Error", "Printing failed", "Could not print the document.");
            }
        }
    }

    public void FileMenu_Exit() {
        if (hasUnsavedChanges()) {
            if (confirmAndSaveIfNeeded()) return;
        }
        Platform.exit();
    }

    private boolean hasUnsavedChanges() {
        return isModified;
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
            if (result.get().getText().equals("Save")) {
                FileMenu_Save();
                return false;
            } else return result.get().getText().equals("Cancel");
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
