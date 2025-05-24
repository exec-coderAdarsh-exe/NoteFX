package com.example.notepad;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.fxmisc.richtext.CodeArea;

import java.io.*;
import java.util.*;
import java.util.function.IntFunction;

public class HelloController {

    @FXML
    private VBox editorContainer;

    private CodeArea codeArea;
    private File currentFile = null;
    private boolean isModified = false;

    private final Map<Integer, String> lineColorMap = new HashMap<>();
    private final Map<String, List<Integer>> colorGroups = new HashMap<>() {{
        put("YELLOW", new ArrayList<>());
        put("GREEN", new ArrayList<>());
        put("RED", new ArrayList<>());
    }};
    private final List<String> colorCycle = Arrays.asList("YELLOW", "GREEN", "RED", "NONE");

    @FXML
    public void initialize() {
        codeArea = new CodeArea();
        VBox.setVgrow(codeArea, Priority.ALWAYS);
        codeArea.setPrefHeight(1080);
        codeArea.setPrefWidth(1920);

        codeArea.setParagraphGraphicFactory(lineNumberGraphicFactory());
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        editorContainer.getChildren().add(codeArea);

        codeArea.textProperty().addListener((_, _, _) -> isModified = true);
    }

    private IntFunction<Node> lineNumberGraphicFactory() {
        return line -> {
            int lineIndex = line + 1;
            Label label = new Label(String.valueOf(lineIndex));
            label.setStyle(getLineNumberStyle(lineIndex));
            label.setMinWidth(40);
            label.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));

            label.setOnMouseClicked(e -> {
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

    private String getNextColor(String current) {
        if (current == null || !colorCycle.contains(current)) return colorCycle.get(0);
        int index = colorCycle.indexOf(current);
        return colorCycle.get((index + 1) % colorCycle.size());
    }

    private String getLineNumberStyle(int line) {
        return switch (lineColorMap.getOrDefault(line, "NONE")) {
            case "YELLOW" -> "-fx-background-color: yellow; -fx-text-fill: black;";
            case "GREEN" -> "-fx-background-color: lightgreen; -fx-text-fill: black;";
            case "RED" -> "-fx-background-color: tomato; -fx-text-fill: white;";
            default -> "-fx-background-color: transparent; -fx-text-fill: grey;";
        };
    }

    public void FileMenu_New(ActionEvent event) {
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded(event)) return;

        codeArea.clear();
        lineColorMap.clear();
        colorGroups.values().forEach(List::clear);
        currentFile = null;
        isModified = false;

        codeArea.setParagraphGraphicFactory(lineNumberGraphicFactory());
    }

    public void FileMenu_Open(ActionEvent event) {
        if (hasUnsavedChanges() && confirmAndSaveIfNeeded(event)) return;

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
                codeArea.setParagraphGraphicFactory(lineNumberGraphicFactory());
            } catch (IOException e) {
                showError("Open Error", "Could not open file", e.getMessage());
            }
        }
    }

    public void FileMenu_Save(ActionEvent event) {
        if (currentFile == null) {
            FileMenu_SaveAS(event);
        } else {
            saveToFile(currentFile);
        }
    }

    public void FileMenu_SaveAS(ActionEvent event) {
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

    public void FileMenu_Print(ActionEvent event) {
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

    public void FileMenu_Exit(ActionEvent event) {
        if (hasUnsavedChanges()) {
            if (confirmAndSaveIfNeeded(event)) return;
        }
        Platform.exit();
    }

    private boolean hasUnsavedChanges() {
        return isModified;
    }

    private boolean confirmAndSaveIfNeeded(ActionEvent event) {
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
                FileMenu_Save(event);
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

    // Highlight menus
    public void highlightsMenu_yellow(ActionEvent event) { showHighlightDialog("YELLOW"); }
    public void highlightsMenu_green(ActionEvent event) { showHighlightDialog("GREEN"); }
    public void highlightsMenu_red(ActionEvent event) { showHighlightDialog("RED"); }

    private void showHighlightDialog(String color) {
        List<Integer> lines = colorGroups.getOrDefault(color, List.of());
        StringBuilder content = new StringBuilder();

        if (lines.isEmpty()) {
            content.append("No lines highlighted in ").append(color).append(".");
        } else {
            content.append("Lines highlighted in ").append(color).append(":\n");
            lines.forEach(line -> content.append("Line ").append(line).append("\n"));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Highlights: " + color);
        alert.setHeaderText(null);
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    // Stubbed methods (optional to implement later)
    public void EditMenu_date_time(ActionEvent e) {}
    public void EditMenu_selectAll(ActionEvent e) { codeArea.selectAll(); }
    public void EditMenu_goTo(ActionEvent e) {}
    public void EditMenu_replace(ActionEvent e) {}
    public void EditMenu_copy_paste(ActionEvent e) {}
    public void EditMenu_delete(ActionEvent e) { codeArea.deleteText(codeArea.getSelection()); }
    public void EditMenu_cut(ActionEvent e) { codeArea.cut(); }
    public void EditMenu_copy(ActionEvent e) { codeArea.copy(); }
    public void EditMenu_paste(ActionEvent e) { codeArea.paste(); }
    public void FormatMenu_font(ActionEvent e) {}
    public void FormatMenu_wordWrap(ActionEvent e) {}
    public void ViewMenu_on_screen_kb(ActionEvent e) {}
    public void HelpMenu_aboutNtpd(ActionEvent e) {}
    public void HelpMenu_viewHelp(ActionEvent e) {}
}
