package com.example.notepad;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class suggestion_handler {

    private final Popup suggestionPopup = new Popup();
    public VBox codeContainer;
    @FXML public ListView<String> suggestionList;
    @FXML private StackPane editorRoot;
    private CodeArea codeArea;
    private boolean listenersAttached = false;

    @FXML
    public void initialize() {

        editorRoot.setMouseTransparent(false);
        suggestionList.setCursor(Cursor.DEFAULT);

        configureSuggestionList();
        editorRoot.setCursor(Cursor.TEXT);

        if (!suggestionPopup.getContent().contains(suggestionList)) {
            suggestionPopup.getContent().add(suggestionList);
        }

    }

    public void setCodeArea(CodeArea area) {
        this.codeArea = area;
        attachListeners();
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

    private void attachListeners() {

        Scene scene = codeArea.getScene();
        if (scene != null) {
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode() == KeyCode.ESCAPE && suggestionList.isVisible()) {
                    hideSuggestions();
                    codeArea.requestFocus();
                    suggestionList.getSelectionModel().clearSelection();
                    event.consume();
                }
            });
        } else {
            Platform.runLater(this::attachListeners);
        }


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
                }
                else if (event.getCode() == KeyCode.DOWN) {
                    suggestionList.requestFocus();
                    suggestionList.getSelectionModel().select(1);
                    event.consume();
                }
                else if (event.getCode() == KeyCode.ENTER) {
                    acceptSelected();
                    event.consume();
                }
            }
            else codeArea.requestFocus();

            Platform.runLater(() -> {
                Stage stage = (Stage) codeArea.getScene().getWindow();
                stage.setOnCloseRequest(e -> {
                    e.consume();
                    hideSuggestions();
                    OptFile_handler.exitApplication();
                });
            });
        });
    }

    private void showSuggestionsIfNeeded() {
        String prefix = getCurrentWordPrefix();
        if (!codeArea.getSelectedText().isEmpty()) {
            hideSuggestions();
            return;
        }
        if (prefix.isEmpty()) {
            hideSuggestions();
            return;
        }

        int caretPos = codeArea.getCaretPosition();
        String fullText = codeArea.getText();

        boolean leftWhitespace = caretPos - prefix.length() - 1 < 0 ||
                Character.isWhitespace(fullText.charAt(caretPos - prefix.length() - 1));

        boolean rightWhitespace = caretPos >= fullText.length() ||
                Character.isWhitespace(fullText.charAt(caretPos));

        if (leftWhitespace && rightWhitespace) {
            List<String> matches = findMatches(prefix);
            if (!matches.isEmpty()) {
                suggestionList.getSelectionModel().clearSelection();
                suggestionList.getItems().setAll(matches);
                suggestionList.getSelectionModel().selectFirst();
                showSuggestions();
                positionSuggestionBox(true);
                return;
            }
        }
        hideSuggestions();
        suggestionList.getSelectionModel().clearSelection();
        positionSuggestionBox(false);
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

    private void showSuggestions(){
        editorRoot.setMouseTransparent(false);
        suggestionList.setVisible(true);
        suggestionList.setManaged(true);
    }

    private void positionSuggestionBox(boolean flag) {
        double cellSize = core.fontSize * 1.5;
        suggestionList.setStyle(core.defaultFontSize);
        suggestionList.setFixedCellSize(cellSize);
        suggestionList.setPrefHeight(3 * cellSize);

        if (flag) {
            codeArea.getCaretBounds().ifPresent(bounds -> {
                suggestionPopup.show(codeArea, bounds.getMaxX(), bounds.getMaxY());
            });
        } else {
            suggestionPopup.hide();
            codeArea.requestFocus();
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

    private void hideSuggestions() {
        suggestionList.setVisible(false);
        suggestionList.setManaged(false);
        positionSuggestionBox(false);
        editorRoot.setMouseTransparent(true);
        codeArea.requestFocus();
    }

}
