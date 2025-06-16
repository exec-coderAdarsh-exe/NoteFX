package com.example.notepad;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.controlsfx.dialog.FontSelectorDialog;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class core {

    public static String defaultFontSize;
    public static double fontSize=14;
    public static CodeArea codeArea;
    private final Map<Integer, String> lineColorMap = new HashMap<>();
    private final Map<String, List<Integer>> colorGroups = new HashMap<>() {{
        put("YELLOW", new ArrayList<>());
        put("GREEN", new ArrayList<>());
        put("RED", new ArrayList<>());
    }};
    private final List<String> colorCycle = List.of("YELLOW", "GREEN", "RED", "NONE");
    public StackPane suggestionBox;
    public RadioMenuItem osk;

    @FXML
    private BorderPane editorContainer;

    @FXML
    private suggestion_handler suggestionBoxController;

    @FXML
    private StackPane editorStack;

    private File currentFile = null;
    private boolean isModified = false;
    private OptFile_handler fileHandler;
    private OptEdit_handler editHandler;
    private int lastMatchIndex = -1;
    private int lastFindIndex = -1;  // Class-level variable to keep track

    @FXML
    public void initialize() {
        codeArea = new CodeArea();
        codeArea.setFocusTraversable(true);
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        codeArea.setParagraphGraphicFactory(createLineNumberFactory());
        VirtualizedScrollPane<CodeArea> vsPane = new VirtualizedScrollPane<>(codeArea);
        editorStack.getChildren().addFirst(vsPane);
        suggestionBoxController.setCodeArea(codeArea);
        fileHandler = new OptFile_handler(this);
        editHandler = new OptEdit_handler(this);
        codeArea.textProperty().addListener((_, _, newText) -> {
            isModified = true;
            Stage stage = (Stage) getEditorContainer().getScene().getWindow();
            if (newText.isEmpty()) {
                stage.setTitle("Notepad");
                setModified(false);
            } else {
                stage.setTitle("Unsaved Changes");
            }
        });
    }

    public java.util.function.IntFunction<Node> createLineNumberFactory() {
        return line -> {
            int lineIndex = line+1;
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

                refreshLineNumbers();
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

    public void FileMenu_Save() { OptFile_handler.saveFile(); }

    public void FileMenu_SaveAS() { OptFile_handler.saveFileAs(); }

    public void FileMenu_Print() { fileHandler.printFile(); }

    public void FileMenu_Exit() { OptFile_handler.exitApplication(); }

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

    public void refreshLineNumbers() { codeArea.setParagraphGraphicFactory(createLineNumberFactory());}

    public void EditMenu_date_time() { editHandler.EditMenu_date_time();}

    public void EditMenu_selectAll() { editHandler.EditMenu_selectAll();}

    public void EditMenu_goTo() { editHandler.EditMenu_goTo();}

    public void EditMenu_replace() { editHandler.EditMenu_replace();}

    public void EditMenu_copy_paste() { editHandler.EditMenu_copy_paste();}

    public void EditMenu_delete() { editHandler.EditMenu_delete();}

    public void EditMenu_cut() { editHandler.EditMenu_cut();}

    public void EditMenu_copy() { editHandler.EditMenu_copy();}

    public void EditMenu_paste() { editHandler.EditMenu_paste();}

    public void FormatMenu_font() {
        Font defaultFont=new Font("Consolas",14);
        defaultFontSize="-fx-font-family: 'Consolas'; -fx-font-size: 14px;";
        FontSelectorDialog fontSelectorDialog = new FontSelectorDialog(defaultFont);
        fontSelectorDialog.setResizable(true);
        fontSelectorDialog.setHeaderText("Select the Font");
        fontSelectorDialog.setContentText("Select the Font");
        fontSelectorDialog.setTitle("Font Chooser Dialog");
        fontSelectorDialog.showAndWait();
        Font selectedFont = fontSelectorDialog.getResult();
        String fontStyle;
        if (selectedFont != null){
            fontStyle = "-fx-font-family: '"+selectedFont.getFamily()+"'; -fx-font-size: "+selectedFont.getSize()+"px;";
            defaultFontSize=fontStyle;
            fontSize=selectedFont.getSize();
            codeArea.setStyle(fontStyle);
        }
    }

    public void ViewMenu_on_screen_kb() {
        try {
            new ProcessBuilder("cmd", "/c", "start", "osk").start();
        } catch (IOException _) {}
    }

    public void registerShortcutKeys(Scene scene) {
        shortcutKey_handler shortcutHandler = new shortcutKey_handler(new shortcutKey_handler.ShortcutListener() {
            @Override
            public void onNewFile() {
                fileHandler.newFile();
            }

            @Override
            public void onSave() {
                OptFile_handler.saveFile();
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

            }
        });
        shortcutHandler.registerShortcuts(scene);
    }

    private void showFindDialog() {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);

        VBox layout = new VBox(8);
        layout.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 10;");
        layout.setPrefWidth(320);

        TextField findField = new TextField();
        findField.setPromptText("Find");
        TextField replaceField = new TextField();
        replaceField.setPromptText("Replace");

        Button interchangeButton = new Button("⇄");
        interchangeButton.setOnAction(e -> {
            String temp = findField.getText();
            findField.setText(replaceField.getText());
            replaceField.setText(temp);
        });

        HBox findReplaceRow = new HBox(5, findField, interchangeButton, replaceField);
        findReplaceRow.setPrefWidth(300);
        findField.setPrefWidth(110);
        replaceField.setPrefWidth(110);
        interchangeButton.setPrefWidth(30);
        Button findButton = new Button("Find Next");
        Button replaceButton = new Button("Replace");
        Button replaceAllButton = new Button("Replace All");
        Button closeButton = new Button("Close");
        HBox buttons = new HBox(8, findButton, replaceButton, replaceAllButton);
        layout.getChildren().addAll(new Label("Find and Replace:"), findReplaceRow, buttons, closeButton);
        popup.getContent().add(layout);
        findField.setOnAction(e -> findNext(findField.getText()));
        findButton.setOnAction(e -> findNext(findField.getText()));
        replaceButton.setOnAction(e -> replaceSelected(replaceField.getText()));
        replaceAllButton.setOnAction(e -> replaceAll(findField.getText(), replaceField.getText()));
        closeButton.setOnAction(e -> popup.hide());
        Stage stage = (Stage) codeArea.getScene().getWindow();
        popup.show(stage);
    }

    private void findNext(String query) {
        if (query == null || query.isEmpty()) return;

        String text = codeArea.getText();
        int startIndex = codeArea.getCaretPosition();
        int index = text.indexOf(query, startIndex);
        if (index == -1 && startIndex > 0) {
            index = text.indexOf(query);
        }
        if (index == -1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Not Found");
            alert.setHeaderText(null);
            alert.setContentText("No occurrences of \"" + query + "\" found.");
            alert.show();
            return;
        }
        codeArea.selectRange(index, index + query.length());
        codeArea.requestFocus();
        lastFindIndex = index;
    }

    private void replaceSelected(String replacement) {
        if (codeArea.getSelectedText().isEmpty()) return;

        codeArea.replaceSelection(replacement);
        lastMatchIndex = -1;
    }

    private void replaceAll(String searchText, String replacement) {
        if (searchText == null || searchText.isEmpty()) return;

        String content = codeArea.getText();
        String updated = content.replace(searchText, replacement);
        codeArea.replaceText(updated);
        lastMatchIndex = -1;
    }
}
