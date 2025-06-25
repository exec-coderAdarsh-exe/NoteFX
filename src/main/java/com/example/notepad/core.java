package com.example.notepad;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.controlsfx.dialog.FontSelectorDialog;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
    public CustomMenuItem autoSaveDelayItem;
    public TextField autoSaveDelayField;
    public RadioMenuItem toggleLineNumbers;
    public RadioMenuItem toggleWrapText;
    // Bottom Fields
    public Label currentCaretPosition;
    public Label showSelectedCharacters;
    public Label showTotal;
    public Separator separator1;
    public Menu backgroundMenu;
    public VBox fontSizeVBoxTop;
    public TextField fontSizeTF_top;
    //    private ColorPicker colorPickDialog;
    private Popup formatPopup;
    @FXML
    private BorderPane editorContainer;
    @FXML
    private suggestion_handler suggestionBoxController;
    @FXML
    private StackPane editorStack;
    private File currentFile = null;
    private boolean isModified = false;
    private OptFile_handler fileHandler;
    // Class-level variable to keep track
    private OptEdit_handler editHandler;

    public static void showFindDialog() {
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
        interchangeButton.setOnAction(_ -> {
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
        findField.setOnAction(_ -> findNext(findField.getText()));
        findButton.setOnAction(_ -> findNext(findField.getText()));
        replaceButton.setOnAction(_ -> replaceSelected(replaceField.getText()));
        replaceAllButton.setOnAction(_ -> replaceAll(findField.getText(), replaceField.getText()));
        closeButton.setOnAction(_ -> popup.hide());
        Stage stage = (Stage) codeArea.getScene().getWindow();
        popup.show(stage);
    }

    private static void findNext(String query) {
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
    }

    private static void replaceSelected(String replacement) {
        if (codeArea.getSelectedText().isEmpty()) return;

        codeArea.replaceSelection(replacement);
    }

    private static void replaceAll(String searchText, String replacement) {
        if (searchText == null || searchText.isEmpty()) return;

        String content = codeArea.getText();
        String updated = content.replace(searchText, replacement);
        codeArea.replaceText(updated);
    }

    // REPLACE THIS METHOD
    public int getAutoSaveDelay() {
        String input = autoSaveDelayField.getText();
        try {
            int val = Integer.parseInt(input);
            return Math.max(1, Math.min(val, 30));
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    @FXML
    public void initialize() {
        codeArea = new CodeArea();
        codeArea.setFocusTraversable(true);
        codeArea.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");


        toggleLineNum();

        toggleWrapText.setOnAction(_ -> codeArea.setWrapText(toggleWrapText.isSelected()));


        VirtualizedScrollPane<CodeArea> vsPane = new VirtualizedScrollPane<>(codeArea);
        editorStack.getChildren().addFirst(vsPane);

        suggestionBoxController.setCodeArea(codeArea);
        fileHandler = new OptFile_handler(this);
        editHandler = new OptEdit_handler(this);

        codeArea.textProperty().addListener((_, _, newText) -> {
            isModified = true;
            Stage stage = (Stage) getEditorContainer().getScene().getWindow();
            if (newText.isEmpty()) {
                stage.setTitle("NoteFX");
                setModified(false);
            } else {
                updateStatusBar();
                stage.setTitle("Unsaved Changes");
            }
        });
        codeArea.caretPositionProperty().addListener((_, _, _) -> {
            updateStatusBar();
            codeArea.requestFocus();
        });

        autoSaveDelayField.textProperty().addListener((_, _, newText) -> {
            if (!newText.matches("\\d*")) {
                autoSaveDelayField.setText(newText.replaceAll("\\D",""));
            } else if (!newText.isEmpty()) {
                try{
                    int val=Integer.parseInt(newText);
                    if (val>30)autoSaveDelayField.setText("30");
                    else if (val==0)autoSaveDelayField.setText("1");
                } catch (NumberFormatException ignored){}
            }
        });
        autoSaveDelayField.setOnAction(_ -> {
            String text = autoSaveDelayField.getText().trim();
            if (!text.matches("\\d+")) {
                autoSaveDelayField.setText("2");
            } else {
                int val = Integer.parseInt(text);
                if (val > 30) val = 30;
                else if (val == 0) val = 1;
                autoSaveDelayField.setText(String.valueOf(val));
            }
            autoSaveDelayField.getParent().requestFocus();
        });


        codeArea.selectedTextProperty().addListener((_, _, _) -> {
            updateStatusBar();

                if (formatPopup == null) {
                    showTextStylesFormat(codeArea, (Stage) codeArea.getScene().getWindow());
                }

                codeArea.getCaretBounds().ifPresent(bounds -> Platform.runLater(() -> {
                    if (formatPopup != null && !formatPopup.isShowing()) {
                        formatPopup.show(codeArea, bounds.getMaxX(), bounds.getMaxY());
                    }
                }));

                codeArea.requestFocus();
            });


            codeArea.requestFocus();


        setupBackgroundMenu();

        fontSizeTF_top.setOnAction(_->{
            codeArea.setStyle("-fx-font-size: "+fontSizeTF_top.getText());
            codeArea.requestFocus();
        });

        fontSizeTopHandle();


        Platform.runLater(() -> codeArea.requestFocus());



    }

    private void updateStatusBar() {
        int selStart = codeArea.getSelection().getStart();
        int selEnd = codeArea.getSelection().getEnd();

        int selectedChars = Math.abs(selEnd - selStart);
        int selectedWords = countFullySelectedWords(codeArea,"selected");
        showSelectedCharacters.setText("Selected: "+selectedChars+" chars, "+selectedWords+" words");

        int totalChars=countFullySelectedWords(codeArea,"fullChars");
        int totalWords = countFullySelectedWords(codeArea,"fullWords");
        showTotal.setText(totalChars+" characters, "+totalWords+" words");

        int currentLine=codeArea.getCurrentParagraph()+1;
        int caretColumn =codeArea.getCaretColumn()+1;
        currentCaretPosition.setText("At: "+currentLine+" Line, "+ caretColumn +" Col");
    }

    private int countFullySelectedWords(CodeArea codeArea,String whichOne) {
        String fullText = codeArea.getText();
        IndexRange sel = codeArea.getSelection();
        int start = sel.getStart();
        int end = sel.getEnd();

        String selectedText = fullText.substring(start, end).trim();

        if (fullText.isBlank()) {
            return 0;
        }


        if (whichOne.equals("fullWords")){
            selectedText=fullText;
            start=0;
            end=selectedText.length();
        }else if (whichOne.equals("fullChars")){
            return fullText.length();
        }
        else if (start == end) {
            return 0;
        }





        if (selectedText.isEmpty()) return 0;

        StringTokenizer tokenizer=new StringTokenizer(selectedText," ");
        int wordCount =tokenizer.countTokens();

        boolean startMidWord = start > 0 &&
                Character.isLetterOrDigit(fullText.charAt(start - 1)) &&
                Character.isLetterOrDigit(fullText.charAt(start));

        boolean endMidWord = end < fullText.length() &&
                Character.isLetterOrDigit(fullText.charAt(end - 1)) &&
                Character.isLetterOrDigit(fullText.charAt(end));

        if (startMidWord) wordCount--;
        if (endMidWord) wordCount--;

        return Math.max(wordCount, 0);
    }

    public java.util.function.IntFunction<Node> createLineNumberFactory() {
        return line -> {
            int lineIndex = line+1;
            Label label = new Label(String.valueOf(lineIndex));
            label.setMinWidth(40);
            label.setPadding(new Insets(2, 8, 2, 8));
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

        if (lines.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Highlights: " + color);
            alert.setHeaderText(null);
            alert.setContentText("No lines highlighted in " + color + ".");
            alert.showAndWait();
            return;
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(lines.getFirst(), lines);
        dialog.setTitle("Go to Highlighted Line");
        dialog.setHeaderText("Highlighted lines in " + color);
        dialog.setContentText("Select a line:");

        dialog.showAndWait().ifPresent(line -> {
            // Navigate to the selected line
            codeArea.moveTo(line - 1, 0);  // 0-based index
            codeArea.requestFollowCaret();
        });
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

    public void EditMenu_date_time() { editHandler.EditMenu_date_time();}

    public void EditMenu_selectAll() { editHandler.EditMenu_selectAll();}

    public void EditMenu_goTo() { editHandler.EditMenu_goTo();}

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

    public void toggleLineNum() {
        if (toggleLineNumbers.isSelected())
            codeArea.setParagraphGraphicFactory(createLineNumberFactory());
        else codeArea.setParagraphGraphicFactory(null);
    }

    public void showTextStylesFormat(CodeArea codeArea, Stage primaryStage) {
        // If popup already exists, hide it first
        if (formatPopup != null && formatPopup.isShowing()) {
            formatPopup.hide();
        }

        formatPopup = new Popup();

        // spacing = 6
        HBox toolBar = new HBox(6);
        toolBar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #aaa; -fx-border-width: 1; -fx-padding: 4;");
        toolBar.setAlignment(Pos.CENTER_LEFT);


        // --- Buttons ---
        Button bold = new Button("B");
        bold.setOnAction(_ -> applyStyle(codeArea,"bold"));

        Button italic = new Button("I");
        italic.setStyle("-fx-font-style: italic; -fx-padding: 2 6; -fx-font-size: 12;");
        italic.setOnAction(_ -> applyStyle(codeArea, "italic"));

        Button underline = new Button("U");
        underline.setStyle("-fx-underline: true; -fx-padding: 2 6; -fx-font-size: 12;");
        underline.setOnAction(_ -> applyStyle(codeArea, "underline"));


        ColorPicker fontColorPicker = new ColorPicker();
        fontColorPicker.setPrefWidth(65);
        fontColorPicker.setOnAction(_ -> {
            String color = toRgbCode(fontColorPicker.getValue());
            applyStyle(codeArea, "-fx-fill: " + color);
        });

        ColorPicker highlightColorPicker = new ColorPicker();
        highlightColorPicker.setPrefWidth(65);
        highlightColorPicker.setOnAction(_ -> {
            String color = toRgbCode(highlightColorPicker.getValue());
            applyStyle(codeArea, "-fx-background-color: " + color);
        });


        toolBar.getChildren().addAll(bold, italic, underline, fontColorPicker, highlightColorPicker);
        formatPopup.getContent().add(toolBar);

        formatPopup.setAutoFix(true);
        formatPopup.setAutoHide(true);
        formatPopup.setHideOnEscape(true);

        codeArea.selectedTextProperty().addListener((_, _, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                formatPopup.hide();
            }
        });
        // Hide when window is unfocused
        primaryStage.focusedProperty().addListener((_, _, isFocused) -> {
            if (!isFocused) formatPopup.hide();
        });

        // Hide when selection is cleared

        // Show at selection position
        codeArea.requestFocus();
        codeArea.getCaretBounds().ifPresent(bounds -> formatPopup.show(codeArea, bounds.getMaxX(), bounds.getMaxY()));

    }

    private void applyStyle(CodeArea codeArea, String styleClass) {
        int start = codeArea.getSelection().getStart();
        int end = codeArea.getSelection().getEnd();
        if (start != end) {
            codeArea.setStyle(start, end, Collections.singleton(styleClass)); // Now it’s a class
        }
    }





    private String toRgbCode(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private void setupBackgroundMenu() {
        Label label = new Label("Background:");
        label.setStyle("-fx-text-fill: black");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setPrefWidth(100);

        HBox colorBox = new HBox(5, label, colorPicker);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        colorBox.setPadding(new Insets(1, 1, 1, 1));

        CustomMenuItem colorPickerItem = new CustomMenuItem(colorBox);
        colorPickerItem.setHideOnClick(true);

        colorPicker.setOnAction(_ -> {
            Color selected = colorPicker.getValue();
            codeArea.setStyle("-fx-background-color: " + toRgba(selected) + ";");
            codeArea.requestFocus();
            colorPickerItem.hideOnClickProperty().set(true);
        });

        MenuItem resetItem = new MenuItem("Reset Background");
        resetItem.setOnAction(_ -> codeArea.setStyle(""));


        backgroundMenu.getItems().clear();
        backgroundMenu.getItems().addAll(colorPickerItem, resetItem);
    }
    private String toRgba(Color color) {
        return String.format("rgba(%d,%d,%d,%.2f)",
                (int)(color.getRed()*255),
                (int)(color.getGreen()*255),
                (int)(color.getBlue()*255),
                color.getOpacity());
    }

    public void fontSizeTopHandle() {
        fontSizeVBoxTop.addEventFilter(MouseEvent.MOUSE_PRESSED, _ -> fontSizeVBoxTop.setStyle("-fx-background-color: #d6d6d6"));
        fontSizeVBoxTop.addEventFilter(MouseEvent.MOUSE_RELEASED,_ -> fontSizeVBoxTop.setStyle("-fx-background-color: transparent"));
    }
}
