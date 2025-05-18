package com.example.notepad;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

import java.util.HashMap;
import java.util.Map;

public class HelloController {

    @FXML
    private VBox editorContainer;

    private CodeArea codeArea;
    private final int fntSize=24;

    // Map to store line number colors
    private final Map<Integer, String> lineColorMap = new HashMap<>();

    @FXML
    public void initialize() {
        codeArea = new CodeArea();
        codeArea.setWrapText(false);
        codeArea.setStyle("-fx-font-family:'Fira Code'; -fx-font-size: "+fntSize+"px;");
        codeArea.setPrefSize(HelloApplication.width, HelloApplication.height);

        codeArea.setParagraphGraphicFactory(this::createLineLabel);

        editorContainer.getChildren().add(codeArea);
    }

    private Label createLineLabel(int lineIndex) {
        Label lineLabel = new Label(String.valueOf(lineIndex + 1));

        // Default style
        String backgroundColor = lineColorMap.getOrDefault(lineIndex, "transparent");
        String textColor = backgroundColor.equals("transparent") ? "gray" : "black";

        lineLabel.setStyle(
                "-fx-padding: 2 8 2 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-size: "+(fntSize-2)+";" +
                        "-fx-background-radius: 12;" +
                        "-fx-background-insets: 0;" +
                        "-fx-background-color: " + backgroundColor + ";" +
                        "-fx-text-fill: " + textColor + ";"
        );

        // Click toggles color cycling (yellow → green → red → none)
        lineLabel.setOnMouseClicked(e -> {
            cycleLineColor(lineIndex);
            codeArea.setParagraphGraphicFactory(this::createLineLabel); // refresh
        });

        return lineLabel;
    }

    // Cycles through different highlight colors
    private void cycleLineColor(int lineIndex) {
        String current = lineColorMap.getOrDefault(lineIndex, "transparent");
        String next;
        switch (current) {
            case "transparent" -> next = "yellow";
            case "yellow"     -> next = "lightgreen";
            case "lightgreen" -> next = "lightcoral";
            default           -> next = "transparent";
        }

        if (next.equals("transparent")) {
            lineColorMap.remove(lineIndex);
        } else {
            lineColorMap.put(lineIndex, next);
        }
    }

    // You can call this to highlight a line with a specific color externally
    public void setLineColor(int lineIndex, String color) {
        lineColorMap.put(lineIndex, color);
        codeArea.setParagraphGraphicFactory(this::createLineLabel);
    }

    @FXML
    protected void onHelloButtonClick() {}

    public void handleNew(ActionEvent actionEvent) {}

    public void handleOpen(ActionEvent actionEvent) {}

    public void handleSave(ActionEvent actionEvent) {}

    public void handleSaveAS(ActionEvent actionEvent) {}

    public void handlePrint(ActionEvent actionEvent) {}

    public void handleExit(ActionEvent actionEvent) {}
}
