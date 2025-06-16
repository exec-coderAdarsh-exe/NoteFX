package com.example.notepad;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.fxmisc.richtext.CodeArea;

import java.time.LocalDateTime;
import java.util.Optional;

public class OptEdit_handler {

    private final core controller;

    public OptEdit_handler(core controller) {
        this.controller = controller;
    }

    public void EditMenu_date_time() {
        String dateTime = LocalDateTime.now().toString();
        CodeArea area = controller.getCodeArea();
        area.insertText(area.getCaretPosition(), dateTime);
    }

    public void EditMenu_selectAll() {
        controller.getCodeArea().selectAll();
    }

    public void EditMenu_goTo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Go To Line");
        dialog.setHeaderText("Enter line number:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(input -> {
            try {
                int line = Integer.parseInt(input.trim());
                if (line < 1) throw new NumberFormatException();
                CodeArea area = controller.getCodeArea();
                int pos = area.position(line - 1, 0).toOffset();
                area.moveTo(pos);
            } catch (Exception e) {
                showAlert("Invalid input", "Please enter a valid positive line number.");
            }
        });
    }

    public void EditMenu_delete() {
        controller.getCodeArea().replaceSelection("");
    }

    public void EditMenu_cut() {
        controller.getCodeArea().cut();
    }

    public void EditMenu_copy() {
        controller.getCodeArea().copy();
    }

    public void EditMenu_paste() {
        controller.getCodeArea().paste();
    }

    public void EditMenu_copy_paste() {
        CodeArea area = controller.getCodeArea();
        String selected = area.getSelectedText();
        if (!selected.isEmpty()) {
            area.insertText(area.getCaretPosition(), selected);
        } else {
            showAlert("No selection", "Please select text to duplicate.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
