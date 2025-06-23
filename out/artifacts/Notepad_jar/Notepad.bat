@echo off
set FX="C:\Users\Aditya\Downloads\Java\javafx-sdk-24.0.1\lib"
java --module-path %FX% --add-modules javafx.controls,javafx.fxml -jar Notepad.jar
pause