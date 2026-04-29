package com.student.ui;

import com.student.data.DataManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ExportImportDialog {

    private Stage stage;

    public ExportImportDialog(Stage stage) {
        this.stage = stage;
    }

    public Dialog<ButtonType> create() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Export / Import Data");
        dialog.setHeaderText("Manage your data backups");

        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        // Export section
        VBox exportSection = new VBox(12);
        exportSection.setPadding(new Insets(15));
        exportSection.setStyle(
            "-fx-background-color: rgba(99,102,241,0.1);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(99,102,241,0.3);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );

        Label exportTitle = new Label("📤 Export Data");
        exportTitle.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox exportButtons = new HBox(10);
        exportButtons.setAlignment(Pos.CENTER_LEFT);

        Button exportExpensesBtn = new Button("Expenses CSV");
        exportExpensesBtn.setStyle(secondaryButtonStyle());
        exportExpensesBtn.setOnAction(e -> exportCSV("expenses"));

        Button exportTasksBtn = new Button("Tasks CSV");
        exportTasksBtn.setStyle(secondaryButtonStyle());
        exportTasksBtn.setOnAction(e -> exportCSV("tasks"));

        Button exportEventsBtn = new Button("Events CSV");
        exportEventsBtn.setStyle(secondaryButtonStyle());
        exportEventsBtn.setOnAction(e -> exportCSV("events"));

        Button exportAllBtn = new Button("Full Backup");
        exportAllBtn.setStyle(primaryButtonStyle());
        exportAllBtn.setOnAction(e -> createBackup());

        exportButtons.getChildren().addAll(exportExpensesBtn, exportTasksBtn, exportEventsBtn, exportAllBtn);

        exportSection.getChildren().addAll(exportTitle, exportButtons);

        // Import section
        VBox importSection = new VBox(12);
        importSection.setPadding(new Insets(15));
        importSection.setStyle(
            "-fx-background-color: rgba(16,185,129,0.1);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(16,185,129,0.3);" +
            "-fx-border-radius: 12;" +
            "-fx-border-width: 1;"
        );

        Label importTitle = new Label("📥 Import Data");
        importTitle.setStyle("-fx-text-fill: #6ee7b7; -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox importButtons = new HBox(10);
        importButtons.setAlignment(Pos.CENTER_LEFT);

        Button importBackupBtn = new Button("Restore Backup");
        importBackupBtn.setStyle(primaryButtonStyle());
        importBackupBtn.setOnAction(e -> restoreBackup());

        Button resetDataBtn = new Button("Reset All Data");
        resetDataBtn.setStyle(dangerButtonStyle());
        resetDataBtn.setOnAction(e -> resetData());

        importButtons.getChildren().addAll(importBackupBtn, resetDataBtn);

        Label importWarning = new Label("⚠️  Restoring a backup will replace all current data");
        importWarning.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");

        importSection.getChildren().addAll(importTitle, importButtons, importWarning);

        content.getChildren().addAll(exportSection, importSection);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

        return dialog;
    }

    private void exportCSV(String type) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export " + type + " to CSV");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName(type + "_export.csv");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            DataManager.exportToCSV(file.getAbsolutePath(), type);
            showAlert("Export Successful", type + " exported to " + file.getName());
        }
    }

    private void createBackup() {
        DataManager.createBackup();
        showAlert("Backup Created", "Full backup created successfully");
    }

    private void restoreBackup() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Backup Directory");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/.studyhub/backups"));

        File dir = dirChooser.showDialog(stage);
        if (dir != null) {
            File[] backups = dir.listFiles((d, name) -> name.startsWith("backup_") && name.endsWith(".json"));
            if (backups != null && backups.length > 0) {
                File latest = backups[backups.length - 1];
                DataManager.restoreBackup(latest.getAbsolutePath());
                showAlert("Restore Successful", "Data restored from " + latest.getName());
            } else {
                showAlert("No Backups Found", "No backup files found in the selected directory");
            }
        }
    }

    private void resetData() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset All Data");
        confirm.setHeaderText("Are you sure you want to reset all data?");
        confirm.setContentText("This action cannot be undone. All your expenses, tasks, events, and user accounts will be deleted.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            DataManager.resetData();
            showAlert("Data Reset", "All data has been reset to default");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String primaryButtonStyle() {
        return
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;";
    }

    private String secondaryButtonStyle() {
        return
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 13px;";
    }

    private String dangerButtonStyle() {
        return
            "-fx-background-color: rgba(239,68,68,0.2);" +
            "-fx-text-fill: #f87171;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 13px;";
    }
}
