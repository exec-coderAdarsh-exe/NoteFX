package com.example.notepad;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.util.*;

public class core {

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
    private final List<String> colorCycle = List.of("YELLOW", "GREEN", "RED", "NONE");

    private OptFile_handler fileHandler;

    @FXML
    public void initialize() {
        // Set up CodeArea with styling and line numbering
        codeArea = new CodeArea();
        codeArea.setFocusTraversable(true);
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        codeArea.setParagraphGraphicFactory(createLineNumberFactory());
        codeArea.textProperty().addListener((obs, oldText, newText) -> isModified = true);

        // Place CodeArea in a VirtualizedScrollPane
        VirtualizedScrollPane<CodeArea> vsPane = new VirtualizedScrollPane<>(codeArea);

        // Bind the scroll pane size to the container for responsiveness
        vsPane.prefWidthProperty().bind(editorContainer.widthProperty());
        vsPane.prefHeightProperty().bind(editorContainer.heightProperty());

        VBox.setVgrow(vsPane, Priority.ALWAYS);
        HBox.setHgrow(vsPane, Priority.ALWAYS);

        editorContainer.getChildren().add(vsPane);

        fileHandler = new OptFile_handler(this);
    }

    public java.util.function.IntFunction<Node> createLineNumberFactory() {
        return line -> {
            int lineIndex = line + 1;
            Label label = new Label(String.valueOf(lineIndex));
            label.setMinWidth(40);
            label.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));
            label.setStyle(getLineNumberStyle(lineIndex));

            label.setOnMouseClicked(event -> {
                String current = lineColorMap.get(lineIndex);
                String next = getNextColor(current);

                if (current != null) {
                    colorGroups.get(current).remove((Integer) lineIndex);
                }
                if (!"NONE".equals(next)) {
                    lineColorMap.put(lineIndex, next);
                    colorGroups.get(next).add(lineIndex);
                } else {
                    lineColorMap.remove(lineIndex);
                }
                // Refresh the line number styling
                codeArea.setParagraphGraphicFactory(createLineNumberFactory());
            });

            return label;
        };
    }

    private String getNextColor(String current) {
        // Use get(0) instead of getFirst()
        if (current == null || !colorCycle.contains(current)) return colorCycle.get(0);
        int index = colorCycle.indexOf(current);
        return colorCycle.get((index + 1) % colorCycle.size());
    }

    private String getLineNumberStyle(int line) {
        return switch (lineColorMap.getOrDefault(line, "NONE")) {
            case "YELLOW" -> "-fx-background-color: yellow; -fx-text-fill: black;";
            case "GREEN"  -> "-fx-background-color: lightgreen; -fx-text-fill: black;";
            case "RED"    -> "-fx-background-color: tomato; -fx-text-fill: white;";
            default       -> "-fx-background-color: transparent; -fx-text-fill: grey;";
        };
    }

    public void FileMenu_New() { fileHandler.newFile(); }
    public void FileMenu_Open() { fileHandler.openFile(); }
    public void FileMenu_Save() { fileHandler.saveFile(); }
    public void FileMenu_SaveAS() { fileHandler.saveFileAs(); }
    public void FileMenu_Print() { fileHandler.printFile(); }
    public void FileMenu_Exit() { fileHandler.exitApplication(); }

    public void highlightsMenu_yellow() { showHighlightDialog("YELLOW"); }
    public void highlightsMenu_green()  { showHighlightDialog("GREEN"); }
    public void highlightsMenu_red()    { showHighlightDialog("RED"); }

    private void showHighlightDialog(String color) {
        List<Integer> lines = colorGroups.getOrDefault(color, List.of());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Highlights: " + color);
        alert.setHeaderText(null);

        if (lines.isEmpty()) {
            alert.setContentText("No lines highlighted in " + color + ".");
        } else {
            StringBuilder content = new StringBuilder("Lines highlighted in " + color + ":\n");
            for (Integer line : lines) {
                content.append("Line ").append(line).append("\n");
            }
            alert.setContentText(content.toString());
        }
        alert.showAndWait();
    }

    // Accessors for OptFile_handler
    public CodeArea getCodeArea() { return codeArea; }
    public File getCurrentFile() { return currentFile; }
    public void setCurrentFile(File file) { this.currentFile = file; }
    public boolean isModified() { return isModified; }
    public void setModified(boolean value) { this.isModified = value; }
    public VBox getEditorContainer() { return editorContainer; }

    public void refreshLineNumbers() {
        codeArea.setParagraphGraphicFactory(createLineNumberFactory());
    }
}
