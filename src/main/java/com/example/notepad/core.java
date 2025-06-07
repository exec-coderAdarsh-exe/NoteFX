package com.example.notepad;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.controlsfx.dialog.FontSelectorDialog;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;

public class core {

    public StackPane suggestionBox;
    public RadioMenuItem osk;
    @FXML
    private BorderPane editorContainer;
    @FXML
    private suggestion_handler suggestionBoxController;
    @FXML
    private StackPane editorStack;




    public static CodeArea codeArea;
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
    private OptEdit_handler editHandler;


    @FXML
    public void initialize() {
        codeArea = new CodeArea();
        codeArea.setFocusTraversable(true);
        codeArea.setStyle("-fx-font-family: 'Calibri'; -fx-font-size: 14px;");
        codeArea.setParagraphGraphicFactory(createLineNumberFactory());
        codeArea.textProperty().addListener((_, _, _) -> isModified = true);

        VirtualizedScrollPane<CodeArea> vsPane = new VirtualizedScrollPane<>(codeArea);
        editorStack.getChildren().addFirst(vsPane);


        suggestionBoxController.setCodeArea(codeArea);

        fileHandler = new OptFile_handler(this);

        editHandler = new OptEdit_handler(this);

        Platform.runLater(() -> {
            Stage stage = (Stage) codeArea.getScene().getWindow();
            stage.setOnCloseRequest(e -> {
                e.consume();
                fileHandler.exitApplication();
            });
        });
    }




    public java.util.function.IntFunction<Node> createLineNumberFactory() {
        return line -> {
            int lineIndex = line + 1;
            Label label = new Label(String.valueOf(lineIndex));
            label.setMinWidth(40);
            label.setPadding(new javafx.geometry.Insets(2, 8, 2, 8));
            label.setStyle(getLineNumberStyle(lineIndex));

            label.setOnMouseClicked(_ -> {
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

                codeArea.setParagraphGraphicFactory(createLineNumberFactory());
            });

            return label;
        };
    }

    private String getNextColor(String current) {
        if (current == null || !colorCycle.contains(current)) return colorCycle.getFirst();
        int index = colorCycle.indexOf(current);
        return colorCycle.get((index + 1) % colorCycle.size());
    }

    private String getLineNumberStyle(int line) {
        return switch (lineColorMap.getOrDefault(line, "NONE")) {
            case "YELLOW" -> "-fx-background-color: yellow; -fx-text-fill: black;";
            case "GREEN"  -> "-fx-background-color: lightgreen; -fx-text-fill: black;";
            case "RED"    -> "-fx-background-color: tomato; -fx-text-fill: black;";
            default       -> "-fx-background-color: lightgrey; -fx-text-fill: black;";
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
    public BorderPane getEditorContainer() { return editorContainer; }

    public void refreshLineNumbers() {
        codeArea.setParagraphGraphicFactory(createLineNumberFactory());
    }

    public void EditMenu_date_time() { editHandler.EditMenu_date_time();
    }

    public void EditMenu_selectAll() { editHandler.EditMenu_selectAll();
    }

    public void EditMenu_goTo() { editHandler.EditMenu_goTo();
    }

    public void EditMenu_replace() { editHandler.EditMenu_replace();
    }

    public void EditMenu_copy_paste() { editHandler.EditMenu_copy_paste();
    }

    public void EditMenu_delete() { editHandler.EditMenu_delete();
    }

    public void EditMenu_cut() { editHandler.EditMenu_cut();
    }

    public void EditMenu_copy() { editHandler.EditMenu_copy();
    }

    public void EditMenu_paste() { editHandler.EditMenu_paste();
    }

    public void FormatMenu_font() {
        FontSelectorDialog fontSelectorDialog = new FontSelectorDialog(Font.font("Calibre", 5));
        fontSelectorDialog.setResizable(true);
        fontSelectorDialog.setHeaderText("Select the Font");
        fontSelectorDialog.setContentText("Select the Font");
        fontSelectorDialog.setTitle("Font Chooser Dialog");
        fontSelectorDialog.showAndWait();
        Font selectedFont = fontSelectorDialog.getResult();
        String fontStyle;
        if (selectedFont != null){
            fontStyle = String.format("-fx-font-family: '%s'; -fx-font-size: %.1f;", selectedFont.getFamily(), selectedFont.getSize());
            codeArea.setStyle(fontStyle);
        }
    }


    public void ViewMenu_on_screen_kb() {
        if (osk.isSelected()) {
            try {
                new ProcessBuilder("cmd", "/c", "start", "osk").start();
            } catch (IOException _) {}
        } else {
            try {
                new ProcessBuilder("cmd", "/c", "start", "osk").start();
            } catch (IOException _) {}
        }
    }

    public void registerShortcutKeys(Scene scene) {
        shortcutKey_handler shortcutHandler = new shortcutKey_handler(new shortcutKey_handler.ShortcutListener() {
            @Override
            public void onNewFile() {
                fileHandler.newFile();
            }

            @Override
            public void onSave() {
                fileHandler.saveFile();
            }

            @Override
            public void onOpen() {
                fileHandler.openFile();
            }

            @Override
            public void onFind() {
                showFindDialog();
            }

            @Override
            public void onUndo() {
                if (codeArea.isUndoAvailable()) {
                    codeArea.undo();
                }
            }

            @Override
            public void onRedo() {
                if (isModified()) {

                }
            }
        });

        shortcutHandler.registerShortcuts(scene);
    }
    private void showFindDialog() {
        // Your implementation for the Find dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Find");
        alert.setHeaderText(null);
        alert.setContentText("Find dialog not implemented yet.");
        alert.showAndWait();
    }

}
