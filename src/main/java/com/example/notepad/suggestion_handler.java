package com.example.notepad;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import org.fxmisc.richtext.CodeArea;

import java.util.*;
import java.util.stream.Collectors;

public class suggestion_handler {

    private final CodeArea codeArea;
    private final Popup suggestionPopup;
    private final ListView<String> suggestionList;

    private static final int MAX_VISIBLE_ROWS = 8;
    private static final double ROW_HEIGHT = 24;  // Approximate row height for ListView items
    private static final double MAX_HEIGHT = MAX_VISIBLE_ROWS * ROW_HEIGHT;

    public suggestion_handler(CodeArea codeArea) {
        this.codeArea = codeArea;

        suggestionList = new ListView<>();
        suggestionList.setMaxHeight(MAX_HEIGHT);
        suggestionList.setPrefHeight(MAX_HEIGHT);
        suggestionList.setFixedCellSize(ROW_HEIGHT);

        suggestionPopup = new Popup();
        suggestionPopup.setAutoHide(true);
        suggestionPopup.getContent().add(suggestionList);

        configureSuggestionList();
        attachListeners();
    }

    private void configureSuggestionList() {
        suggestionList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selected = suggestionList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    acceptSuggestion(selected);
                }
            }
        });

        suggestionList.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String selected = suggestionList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    acceptSuggestion(selected);
                }
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hideSuggestions();
                codeArea.requestFocus();
                event.consume();
            }
        });
    }

    private void attachListeners() {
        // Use richtext CodeArea's text property listener to trigger suggestions on every text change
        codeArea.textProperty().addListener((_, _, _) -> showSuggestionsIfNeeded());

        // Also listen to caret position changes (keyboard or mouse movement)
        codeArea.caretPositionProperty().addListener((_, _, _) -> showSuggestionsIfNeeded());

        // Handle key presses for navigating suggestions or hiding popup
        codeArea.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (suggestionPopup.isShowing()) {
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
        String prefix = getCurrentWordPrefix();
        if (!prefix.isEmpty()) {
            showSuggestions(prefix);
        } else {
            hideSuggestions();
        }
    }

    private String getCurrentWordPrefix() {
        int caretPos = codeArea.getCaretPosition();
        if (caretPos == 0) return "";

        String textUpToCaret = codeArea.getText(0, caretPos);
        int i = textUpToCaret.length() - 1;

        while (i >= 0 && Character.isLetterOrDigit(textUpToCaret.charAt(i))) {
            i--;
        }

        return textUpToCaret.substring(i + 1);
    }

    private void showSuggestions(String prefix) {
        int caretPosition = codeArea.getCaretPosition();
        String textUpToCaret = codeArea.getText(0, caretPosition);

        // Count previous word occurrences before caret
        String[] wordsBeforeCaret = textUpToCaret.split("\\W+");
        Map<String, Integer> freqBeforeCaret = new HashMap<>();
        for (String word : wordsBeforeCaret) {
            if (!word.isEmpty()) {
                freqBeforeCaret.put(word, freqBeforeCaret.getOrDefault(word, 0) + 1);
            }
        }

        List<String> matches = freqBeforeCaret.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .filter(entry -> entry.getValue() >= 1) // Word appeared at least once before
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            hideSuggestions();
            return;
        }

        suggestionList.getItems().setAll(matches);
        suggestionList.getSelectionModel().selectFirst();

        Optional<Bounds> caretBoundsOpt = codeArea.getCaretBounds();
        if (caretBoundsOpt.isPresent()) {
            Bounds caretBounds = caretBoundsOpt.get();
            Point2D screenPos = codeArea.localToScreen(caretBounds.getMinX(), caretBounds.getMaxY());

            if (screenPos != null) {
                double xOffset = 8;
                double yOffset = 18;
                suggestionPopup.show(codeArea, screenPos.getX() + xOffset, screenPos.getY() + yOffset);
            }
        } else {
            Point2D screenPos = codeArea.localToScreen(0, codeArea.getHeight());
            if (screenPos != null) {
                suggestionPopup.show(codeArea, screenPos.getX(), screenPos.getY());
            }
        }
    }


    private void acceptSuggestion(String suggestion) {
        int caretPos = codeArea.getCaretPosition();
        String text = codeArea.getText();

        int start = caretPos - 1;
        while (start >= 0 && Character.isLetterOrDigit(text.charAt(start))) {
            start--;
        }
        start++;

        codeArea.replaceText(start, caretPos, suggestion);
        codeArea.moveTo(start + suggestion.length());

        hideSuggestions();
        codeArea.requestFocus();
    }

    private void hideSuggestions() {
        if (suggestionPopup.isShowing()) {
            suggestionPopup.hide();
        }
    }
}
