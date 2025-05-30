package com.example.notepad;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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
    private boolean darkMode = false;

    public OptHelp_handler() {
        helpStage = new Stage();
        helpStage.setTitle("Help - Notepad");
        helpStage.initModality(Modality.APPLICATION_MODAL);
        helpStage.setMinWidth(650);
        helpStage.setMinHeight(400);

        // Initialize help content
        helpContent = loadHelpContent();

        // Search input
        searchField = new TextField();
        searchField.setPromptText("Search help topics...");
        searchField.setMinWidth(400);

        Button darkModeToggle = new Button("Toggle Dark Mode");
        darkModeToggle.setOnAction(_ -> toggleDarkMode());

        HBox topBar = new HBox(10, searchField, darkModeToggle);
        topBar.setPadding(new Insets(10));

        // Tabs container
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Layout
        VBox root = new VBox(5, topBar, tabPane);
        root.setPadding(new Insets(10));

        // Set scene
        Scene scene = new Scene(root);
        helpStage.setScene(scene);

        // Populate all tabs initially (no highlight)
        loadAllTabs();

        // Add search listener
        searchField.textProperty().addListener((_, _, newVal) -> handleSmartSearch(newVal));

        // Allow pressing ESC to close help window
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) helpStage.close();
        });
    }

    public void show() {
        helpStage.showAndWait();
    }

    private void toggleDarkMode() {
        darkMode = !darkMode;
        // No CSS, so this is a stub.
        // You can expand to switch styles inline or via scene.getStylesheets()
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Dark mode toggled (stub). Implement styling here.");
        alert.initOwner(helpStage);
        alert.showAndWait();
    }

    private Map<String, String> loadHelpContent() {
        Map<String, String> content = new LinkedHashMap<>();

        content.put("About Notepad",
                """
                        Welcome to the Notepad application.
                        
                        This help section contains useful information about the features and how to use them."""
        );

        content.put("Using the Editor",
                """
                        The editor supports basic text editing features:
                        - Cut, Copy, Paste
                        - Select All
                        - Undo/Redo
                        - Find and Replace (supports multi-line replacements)
                        - Formatting like bold, italic, and underline (planned)"""
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
                        - Report bugs via the provided feedback form.
                        - For advanced help, visit our website."""
        );

        content.put("Dark Mode",
                "Toggle dark mode to reduce eye strain during night time usage.\n" +
                        "Currently, dark mode toggle is under development."
        );

        content.put("Advanced Features",
                """
                        Future updates will include:
                        - Syntax highlighting for code
                        - Auto-completion
                        - Plugin support
                        - Cloud sync"""
        );

        return content;
    }

    private void loadAllTabs() {
        tabPane.getTabs().clear();
        helpContent.forEach((title, content) -> {
            boolean highlight = false; // no highlight on full load
            tabPane.getTabs().add(createTab(title, content, highlight));
        });
    }

    private void handleSmartSearch(String query) {
        tabPane.getTabs().clear();

        if (query == null || query.isBlank()) {
            // Show all tabs no highlight
            loadAllTabs();
            return;
        }

        String[] tokens = query.toLowerCase().split("\\s+");

        // Exclude About section from search matching
        List<Map.Entry<String, String>> helpOnly = helpContent.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().equalsIgnoreCase("About Notepad"))
                .toList();

        // Find matched entries where all tokens appear in title or content
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
                // No more matches, add rest as normal text
                result.add(new Text(content.substring(index)));
                break;
            }

            // Add text before match
            if (nextMatchStart > index) {
                result.add(new Text(content.substring(index, nextMatchStart)));
            }

            // Add matched text with highlight
            Text highlighted = new Text(content.substring(nextMatchStart, nextMatchStart + matchLength));
            highlighted.setStyle("-fx-fill: red; -fx-font-weight: bold;");
            result.add(highlighted);

            index = nextMatchStart + matchLength;
        }

        return result;
    }
}
