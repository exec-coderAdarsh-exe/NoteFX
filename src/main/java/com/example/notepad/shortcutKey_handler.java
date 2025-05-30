package com.example.notepad;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Handles global shortcut keys for the notepad application.
 * <p>
 * Shortcuts supported:
 * Ctrl+N: New file
 * Ctrl+S: Save file
 * Ctrl+O: Open file
 * Ctrl+F: Find text
 * Ctrl+Z: Undo
 * Ctrl+Y: Redo
 */
public class shortcutKey_handler {

    public interface ShortcutListener {
        void onNewFile();
        void onSave();
        void onOpen();
        void onFind();
        void onUndo();
        void onRedo();
    }

    private final ShortcutListener listener;

    public shortcutKey_handler(ShortcutListener listener) {
        this.listener = listener;
    }

    /**
     * Register keyboard shortcuts on the given Scene.
     *
     * @param scene The JavaFX scene to listen for key events.
     */
    public void registerShortcuts(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                KeyCode code = event.getCode();

                switch (code) {
                    case N -> {
                        listener.onNewFile();
                        event.consume();
                    }
                    case S -> {
                        listener.onSave();
                        event.consume();
                    }
                    case O -> {
                        listener.onOpen();
                        event.consume();
                    }
                    case F -> {
                        listener.onFind();
                        event.consume();
                    }
                    case Z -> {
                        listener.onUndo();
                        event.consume();
                    }
                    case Y -> {
                        listener.onRedo();
                        event.consume();
                    }
                    default -> {
                        // Do nothing for other keys
                    }
                }
            }
        });
    }
}
