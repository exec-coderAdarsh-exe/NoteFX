package com.example.notepad;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class OptHelp_handler {

    private final Stage helpStage;
    private final TextField searchField;
    private final TabPane tabPane;
    private final Map<String, String> helpContent;

    public OptHelp_handler() {
        helpStage = new Stage();
        helpStage.setTitle("Help - Notepad");
        helpStage.initModality(Modality.APPLICATION_MODAL);
        helpStage.setMinWidth(750);
        helpStage.setMinHeight(400);
        helpContent = loadHelpContent();
        searchField = new TextField();
        searchField.setPromptText("Search help topics...");
        searchField.setMinWidth(400);
        HBox topBar = new HBox(10, searchField);
        topBar.setPadding(new Insets(10));
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox root = new VBox(5, topBar, tabPane);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root);
        helpStage.setScene(scene);
        loadAllTabs();
        searchField.textProperty().addListener((_, _, newVal) -> handleSmartSearch(newVal));

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) helpStage.close();
        });
    }

    public void show() {
        helpStage.showAndWait();
    }

    private Map<String, String> loadHelpContent() {
        Map<String, String> content = new LinkedHashMap<>();

        content.put("About Notepad",
                """
                        NoteFX
                        Advanced Notepad built using JavaFx
                       \s
                        This Notepad Application is a sophisticated text editor built using JavaFX and RichTextFX.
                        It combines essential editing functionalities with advanced features designed to enhance productivity and ease of use. This project showcases a robust, user-friendly interface tailored for efficient text manipulation.
                       \s
                        Features :\s
                        Rich Text Editing
                        Customizable fonts and font sizes to suit user preferences.
                        Text Suggestions (current file based)
                        File Operations
                        Line Numbering & Highlighting - Click numbers for highlighting it so that you make keep it for some reminder.
                        Find and Replace
                        Draft System - Auto-save drafts every 3 minutes
                       \s
                        Installation Ensure you have Java 11 or higher installed
                       \s
                        The application auto-saves drafts periodically to prevent data loss.
                       \s
                       \s
                        About the Developer : Aditya — A committed student and aspiring software developer focused on creating practical and efficient applications.This Notepad project represents a step toward mastering JavaFX and advanced UI development.
                       \s"""
        );
        content.put("Using the Editor",
                """
                        The editor supports basic text editing features:
                        - Cut, Copy, Paste
                        - Select All
                        - Undo/Redo
                        - Find and Replace (supports multi-line replacements)"""
        );
        content.put("Keyboard Shortcuts",
                """
                        Common shortcuts include:
                        Ctrl+N: New file
                        Ctrl+S: Save file
                        Ctrl+O: Open file
                        Ctrl+F: Find text
                        Ctrl+Z: Undo
                        Ctrl+Y: Redo
                        """
        );
        content.put("Troubleshooting",
                """
                        If you encounter issues:
                        - Ensure you have proper write permissions to the save location.
                        - If the app crashes, restart it and try again.
                        - Report bugs via the provided feedback form."""
        );

        return content;
    }
    private void loadAllTabs() {
        tabPane.getTabs().clear();
        helpContent.forEach((title, content) -> {
            boolean highlight = false;
            tabPane.getTabs().add(createTab(title, content, highlight));
        });
    }
    private void handleSmartSearch(String query) {
        tabPane.getTabs().clear();
        if (query == null || query.isBlank()) {
            loadAllTabs();
            return;
        }
        String[] tokens = query.toLowerCase().split("\\s+");
        List<Map.Entry<String, String>> helpOnly = helpContent.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase("About Notepad"))
                .toList();
        List<Map.Entry<String, String>> matched = helpOnly.stream()
                .filter(entry -> {
                    String title = entry.getKey().toLowerCase();
                    String body = entry.getValue().toLowerCase();
                    return Arrays.stream(tokens).allMatch(token ->
                            title.contains(token) || body.contains(token)
                    );
                })
                .toList();

        if (matched.isEmpty()) {
            tabPane.getTabs().add(createTab("Not Found", "No help topics matched your search.", false));

            Tab separator = new Tab("—");
            separator.setDisable(true);
            tabPane.getTabs().add(separator);

            helpContent.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase("About Notepad"))
                    .findFirst()
                    .ifPresent(about -> tabPane.getTabs().add(createTab(about.getKey(), about.getValue(), false)));
        } else {
            matched.forEach(entry -> tabPane.getTabs().add(createTab(entry.getKey(), entry.getValue(), true)));
        }
    }

    private Tab createTab(String title, String content, boolean highlight) {
        Tab tab = new Tab(title);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        TextFlow textFlow = new TextFlow();
        textFlow.setLineSpacing(5);
        textFlow.setPrefWidth(600);

        if (!highlight) {
            textFlow.getChildren().add(new Text(content));
        } else {
            String query = searchField.getText();
            if (query == null || query.isBlank()) {
                textFlow.getChildren().add(new Text(content));
            } else {
                List<String> tokens = Arrays.stream(query.toLowerCase().split("\\s+"))
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toList());
                textFlow.getChildren().addAll(generateHighlightedText(content, tokens));
            }
        }
        scrollPane.setContent(textFlow);
        tab.setContent(scrollPane);
        return tab;
    }

    private List<Text> generateHighlightedText(String content, List<String> tokens) {
        List<Text> result = new ArrayList<>();
        int index = 0;
        String contentLower = content.toLowerCase();

        while (index < content.length()) {
            int nextMatchStart = -1;
            int matchLength = 0;
            for (String token : tokens) {
                if (token.isBlank()) continue;
                int match = contentLower.indexOf(token, index);
                if (match != -1 && (nextMatchStart == -1 || match < nextMatchStart)) {
                    nextMatchStart = match;
                    matchLength = token.length();
                }
            }
            if (nextMatchStart == -1) {
                result.add(new Text(content.substring(index)));
                break;
            }
            if (nextMatchStart > index) {
                result.add(new Text(content.substring(index, nextMatchStart)));
            }
            Text highlighted = new Text(content.substring(nextMatchStart, nextMatchStart + matchLength));
            highlighted.setStyle("-fx-fill: red; -fx-font-weight: bold;");
            result.add(highlighted);
            index = nextMatchStart + matchLength;
        }
        return result;
    }
}
