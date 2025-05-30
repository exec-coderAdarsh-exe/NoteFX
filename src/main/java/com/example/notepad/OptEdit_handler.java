package com.example.notepad;

/*
    Handling edit option in main stage.
 */

import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import org.fxmisc.richtext.CodeArea;

import java.util.Optional;

public class OptEdit_handler {

    private final core controller;

    public OptEdit_handler(core controller) {
        this.controller = controller;
    }

    public void EditMenu_date_time() {
    }

    public void EditMenu_selectAll() {
        controller.getCodeArea().selectAll();
    }

    public void EditMenu_goTo() {

    }

    public void EditMenu_replace() {
        if (controller.getCodeArea().selectedTextProperty()!=null){
            controller.getCodeArea().replaceSelection(callReplaceDialogBox());

        }
    }

    public void EditMenu_copy_paste() {
    }

    public void EditMenu_delete() {
    }

    public void EditMenu_cut() {
    }

    public void EditMenu_copy() {
    }

    public void EditMenu_paste() {
    }

    private String callReplaceDialogBox(){
        TextInputDialog dialog=new TextInputDialog();
        dialog.setHeaderText("Replace selected text");
        dialog.setContentText("Replace with: ");
        dialog.showAndWait();

        return dialog.getResult();
    }

}
