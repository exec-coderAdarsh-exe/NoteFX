package com.example.notepad;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

import java.awt.event.TextEvent;
import java.util.*;
import java.util.stream.Collectors;

public class suggestion_handler {

    public VBox codeContainer;
    @FXML private StackPane editorRoot;
    @FXML public ListView<String> suggestionList;

    private CodeArea codeArea;

    @FXML
    public void initialize() {

        editorRoot.setMouseTransparent(false);
        suggestionList.setCursor(Cursor.DEFAULT);

        configureSuggestionList();
        editorRoot.setCursor(Cursor.TEXT);
        suggestionList.setCursor(Cursor.DEFAULT);

    }

    public void setCodeArea(CodeArea area) {
        this.codeArea = area;
        attachListeners();  // CodeArea is assigned
    }



    private void configureSuggestionList() {
        suggestionList.setFixedCellSize(24);

        suggestionList.setFocusTraversable(false);

        suggestionList.setOnMouseReleased(event -> {
            acceptSelected();
            event.consume();
        });
        suggestionList.setOnScroll(event -> {
            if (codeArea != null) {
                codeArea.fireEvent(event.copyFor(codeArea, codeArea));
                event.consume();
            }
        });



        suggestionList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) acceptSelected();
            else if (event.getCode() == KeyCode.ESCAPE) hideSuggestions();
        });
    }

    private boolean listenersAttached = false;

    private void attachListeners() {
        if (listenersAttached || codeArea == null) return;
        listenersAttached = true;

        codeArea.textProperty().addListener((_, _, _) -> Platform.runLater(this::showSuggestionsIfNeeded));
        codeArea.caretPositionProperty().addListener((_, _, _) -> Platform.runLater(this::showSuggestionsIfNeeded));



        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (suggestionList.isVisible()) {
                suggestionList.getSelectionModel().selectFirst();
                if (event.getCode() == KeyCode.UP) {
                    suggestionList.requestFocus();
                    suggestionList.getSelectionModel().selectFirst();
                    event.consume();
                } else if (event.getCode() == KeyCode.DOWN) {
                    suggestionList.requestFocus();
                    suggestionList.getSelectionModel().select(1);
                    event.consume();
                } else {
                    if (event.getCode() == KeyCode.ENTER) {
                        acceptSelected();
                        event.consume();
                    } else if (event.getCode() == KeyCode.ESCAPE) {
                        hideSuggestions();
                        event.consume();
                    }
                }
            }
            else codeArea.requestFocus();
        });

    }



    private void showSuggestionsIfNeeded() {
        String prefix = getCurrentWordPrefix();
        if (!prefix.isEmpty()) {
            List<String> matches = findMatches(prefix);
            if (!matches.isEmpty()) {
                suggestionList.getItems().setAll(matches);
                suggestionList.getSelectionModel().selectFirst();
                showSuggestions();
                positionSuggestionBox();
                return;
            }
        }
        hideSuggestions();
    }


    private String getCurrentWordPrefix() {
        int caretPos = codeArea.getCaretPosition();
        if (caretPos == 0) return "";
        String text = codeArea.getText(0, caretPos);
        int i = text.length() - 1;
        while (i >= 0 && Character.isLetterOrDigit(text.charAt(i))) i--;
        return text.substring(i + 1);
    }

    private List<String> findMatches(String prefix) {
        String text = codeArea.getText();
        Set<String> words = new HashSet<>(Arrays.asList(text.split("\\W+")));
        return words.stream()
                .filter(w -> !w.equals(prefix) && w.startsWith(prefix))
                .sorted()
                .collect(Collectors.toList());
    }

    private void positionSuggestionBox() {
        suggestionList.setPrefWidth(200);
        suggestionList.setPrefHeight(120);
        suggestionList.setMaxWidth(200);
        suggestionList.setMaxHeight(120);


        StackPane.setAlignment(suggestionList, Pos.TOP_LEFT);


        if (codeArea == null || suggestionList == null) return;


        try {

            var caretBoundsOpt = codeArea.getCaretBounds();
            if (caretBoundsOpt.isEmpty()) return;

            var caretBounds = caretBoundsOpt.get();

            // Convert local bounds of CodeArea caret to parent (StackPane) coordinates
            var localToScene = codeArea.localToScene(caretBounds);
            var sceneToParent = editorRoot.sceneToLocal(localToScene);

            double x = localToScene.getMinX()+100;
            double y = localToScene.getMaxY()+20;

            suggestionList.setLayoutX(x);
            suggestionList.setLayoutY(y);
        } catch (Exception e) {
            System.err.println("Failed to position suggestion box: " + e.getMessage());
        }
    }







    private void acceptSelected() {
        String selected = suggestionList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            selected+=" ";
            int caretPos = codeArea.getCaretPosition();
            int start = caretPos - 1;
            String text = codeArea.getText();
            while (start >= 0 && Character.isLetterOrDigit(text.charAt(start))) start--;
            start++;
            codeArea.replaceText(start, caretPos, selected);
            codeArea.moveTo(start + selected.length());
        }
        hideSuggestions();
        codeArea.requestFocus();
    }

    private void showSuggestions() {
        suggestionList.setVisible(true);
        suggestionList.setManaged(true);
        suggestionList.setOnMouseEntered(e ->{
            suggestionList.setCursor(Cursor.DEFAULT);
            suggestionList.requestFocus();
            editorRoot.setMouseTransparent(false);
        });
        suggestionList.setOnMouseExited(e ->{
            suggestionList.setCursor(Cursor.TEXT);
            codeArea.requestFocus();
            editorRoot.setMouseTransparent(true);
        });
        suggestionList.setCursor(Cursor.DEFAULT);
    }

    private void hideSuggestions() {
        editorRoot.setCursor(Cursor.TEXT);
        editorRoot.setMouseTransparent(true);
        suggestionList.setVisible(false);
        suggestionList.setManaged(false);
    }

}
