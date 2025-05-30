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

    // Inserts current date and time at caret position
    public void EditMenu_date_time() {
        String dateTime = LocalDateTime.now().toString();
        CodeArea area = controller.getCodeArea();
        area.insertText(area.getCaretPosition(), dateTime);
    }

    // Selects all text in the editor
    public void EditMenu_selectAll() {
        controller.getCodeArea().selectAll();
    }

    // Navigates to a specific line
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

    // Replaces selected text with user input
    public void EditMenu_replace() {
        CodeArea area = controller.getCodeArea();
        String selected = area.getSelectedText();
        if (!selected.isEmpty()) {
            Optional<String> replacement = callReplaceDialogBox(selected);
            replacement.ifPresent(area::replaceSelection);
        } else {
            showAlert("No selection", "Please select text to replace.");
        }
    }

    // Deletes selected text
    public void EditMenu_delete() {
        controller.getCodeArea().replaceSelection("");
    }

    // Cuts selected text
    public void EditMenu_cut() {
        controller.getCodeArea().cut();
    }

    // Copies selected text
    public void EditMenu_copy() {
        controller.getCodeArea().copy();
    }

    // Pastes text from clipboard
    public void EditMenu_paste() {
        controller.getCodeArea().paste();
    }

    // Copies and immediately pastes at caret (duplicates selected text)
    public void EditMenu_copy_paste() {
        CodeArea area = controller.getCodeArea();
        String selected = area.getSelectedText();
        if (!selected.isEmpty()) {
            area.insertText(area.getCaretPosition(), selected);
        } else {
            showAlert("No selection", "Please select text to duplicate.");
        }
    }
/*
    // Multiline find and replace in entire text
    public void EditMenu_find_replace_all() {
        TextInputDialog findDialog = new TextInputDialog();
        findDialog.setTitle("Find and Replace All");
        findDialog.setHeaderText("Find text:");
        Optional<String> findTextOpt = findDialog.showAndWait();

        if (findTextOpt.isEmpty()) return;

        TextInputDialog replaceDialog = new TextInputDialog();
        replaceDialog.setTitle("Find and Replace All");
        replaceDialog.setHeaderText("Replace with:");
        Optional<String> replaceTextOpt = replaceDialog.showAndWait();

        if (replaceTextOpt.isEmpty()) return;

        String findText = findTextOpt.get();
        String replaceText = replaceTextOpt.get();

        if (findText.isEmpty()) {
            showAlert("Empty Find", "The text to find cannot be empty.");
            return;
        }

        CodeArea area = controller.getCodeArea();
        String content = area.getText();

        // Replace all occurrences (simple approach, can use regex if needed)
        String newContent = content.replace(findText, replaceText);
        area.replaceText(newContent);

        showAlert("Replace Complete", "All occurrences of \"" + findText + "\" have been replaced.");
    }

 */

    // Internal dialog for single replace
    private Optional<String> callReplaceDialogBox(String selectedText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Replace Text");
        dialog.setHeaderText("Replace selected text:\n\"" + selectedText + "\"");
        dialog.setContentText("Replace with:");
        return dialog.showAndWait();
    }

    // Utility to show alerts
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
