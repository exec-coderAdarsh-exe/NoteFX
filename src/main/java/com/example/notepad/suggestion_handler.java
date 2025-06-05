package com.example.notepad;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.util.*;
import java.util.stream.Collectors;

public class suggestion_handler {

    @FXML private StackPane editorRoot;
    @FXML public ListView<String> suggestionList;

    private CodeArea codeArea;

    public suggestion_handler() {
        // Default constructor
    }
    @FXML
    public void initialize() {
        // This makes the suggestionBox itself transparent for mouse events
        editorRoot.setMouseTransparent(true);

        // But the ListView should still receive mouse events:
        suggestionList.setMouseTransparent(false);

        configureSuggestionList();
        // Do NOT call attachListeners here, codeArea is null now
    }

    public void setCodeArea(CodeArea area) {
        this.codeArea = area;
        attachListeners();  // safe to call now, codeArea is assigned
    }



    private void configureSuggestionList() {
        suggestionList.setFixedCellSize(24);
        suggestionList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) acceptSelected();
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

        codeArea.textProperty().addListener((obs, oldText, newText) -> Platform.runLater(this::showSuggestionsIfNeeded));
        codeArea.caretPositionProperty().addListener((obs, oldPos, newPos) -> Platform.runLater(this::showSuggestionsIfNeeded));

        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (suggestionList.isVisible()) {
                if (event.getCode() == KeyCode.DOWN) {
                    suggestionList.requestFocus();
                    suggestionList.getSelectionModel().selectFirst();
                    event.consume();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    hideSuggestions();
                    event.consume();
                }
            }
        });
    }


    private void showSuggestionsIfNeeded() {
        System.out.println("Checking suggestions...");
        String prefix = getCurrentWordPrefix();
        System.out.println("Current prefix: '" + prefix + "'");
        if (!prefix.isEmpty()) {
            List<String> matches = findMatches(prefix);
            System.out.println("Matches found: " + matches);
            if (!matches.isEmpty()) {
                suggestionList.getItems().setAll(matches);
                suggestionList.getSelectionModel().selectFirst();
                suggestionList.setVisible(true);
                suggestionList.setManaged(true);
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
        int paragraphIndex = codeArea.getCurrentParagraph();
        Optional<Bounds> paragraphBoundsOpt = codeArea.getParagraphBoundsOnScreen(paragraphIndex);

        paragraphBoundsOpt.ifPresent(bounds -> {
            // bounds are in screen coordinates (x, y, width, height)
            // Convert the screen coordinates to local coordinates relative to editorRoot
            Point2D localPoint = editorRoot.screenToLocal(bounds.getMinX(), bounds.getMaxY());

            // Add a small vertical padding to place the suggestion box below the line
            double verticalPadding = 40; // pixels, adjust as needed

            suggestionList.relocate(localPoint.getX(), localPoint.getY() + verticalPadding);
            suggestionList.toFront();
        });
    }





    private void acceptSelected() {
        String selected = suggestionList.getSelectionModel().getSelectedItem();
        if (selected != null) {
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
        editorRoot.setMouseTransparent(false); // allow suggestion box to receive events
    }

    private void hideSuggestions() {
        suggestionList.setVisible(false);
        suggestionList.setManaged(false);
        editorRoot.setMouseTransparent(true);  // let events pass through to CodeArea
    }

}
