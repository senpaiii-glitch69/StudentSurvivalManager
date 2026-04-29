package com.student.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class ThemeSwitcher {

    private static final String DARK_THEME = "dark";
    private static final String LIGHT_THEME = "light";

    private String currentTheme = DARK_THEME;
    private Runnable onThemeChange;

    public Pane create() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        Label title = new Label("🎨 Theme Settings");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Theme options
        VBox themeOptions = new VBox(15);
        themeOptions.setPadding(new Insets(20));
        themeOptions.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 14;"
        );

        Label themeLabel = new Label("Select Theme");
        themeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px; -fx-font-weight: bold;");

        // Dark theme card
        VBox darkCard = createThemeCard(
            "🌙 Dark Mode",
            "Easy on the eyes, perfect for late-night study sessions",
            "#1e1e2e",
            "#6366f1",
            true
        );

        // Light theme card
        VBox lightCard = createThemeCard(
            "☀️ Light Mode",
            "Clean and bright, great for daytime use",
            "#ffffff",
            "#6366f1",
            false
        );

        themeOptions.getChildren().addAll(themeLabel, darkCard, lightCard);

        // Accent color selection
        VBox accentSection = new VBox(15);
        accentSection.setPadding(new Insets(20));
        accentSection.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 14;"
        );

        Label accentLabel = new Label("Accent Color");
        accentLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox colorPicker = new HBox(12);
        colorPicker.setAlignment(Pos.CENTER_LEFT);

        String[] colors = {"#6366f1", "#8b5cf6", "#ec4899", "#f59e0b", "#10b981", "#06b6d4"};
        for (String color : colors) {
            Button colorBtn = new Button();
            colorBtn.setPrefSize(40, 40);
            colorBtn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 20;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: white;" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 20;"
            );
            colorBtn.setOnAction(e -> {
                // Apply accent color
                applyAccentColor(color);
            });
            colorPicker.getChildren().add(colorBtn);
        }

        accentSection.getChildren().addAll(accentLabel, colorPicker);

        // Font size
        VBox fontSection = new VBox(15);
        fontSection.setPadding(new Insets(20));
        fontSection.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 14;"
        );

        Label fontLabel = new Label("Font Size");
        fontLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px; -fx-font-weight: bold;");

        HBox fontControls = new HBox(15);
        fontControls.setAlignment(Pos.CENTER_LEFT);

        Slider fontSlider = new Slider(10, 18, 14);
        fontSlider.setPrefWidth(200);
        fontSlider.setStyle("-fx-accent: #6366f1;");

        Label fontValue = new Label("14px");
        fontValue.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

        fontSlider.valueProperty().addListener((obs, old, newVal) -> {
            fontValue.setText(String.format("%.0fpx", newVal));
            applyFontSize(newVal.doubleValue());
        });

        fontControls.getChildren().addAll(fontSlider, fontValue);

        fontSection.getChildren().addAll(fontLabel, fontControls);

        root.getChildren().addAll(themeOptions, accentSection, fontSection);

        return root;
    }

    private VBox createThemeCard(String title, String description, String bgColor, String accentColor, boolean isSelected) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: " + (isSelected ? accentColor : "rgba(255,255,255,0.1)") + ";" +
            "-fx-border-width: " + (isSelected ? "3" : "1") + ";" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + (bgColor.equals("#ffffff") ? "#1e1e2e" : "white") + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-text-fill: " + (bgColor.equals("#ffffff") ? "rgba(30,30,46,0.6)" : "rgba(255,255,255,0.6)") + "; -fx-font-size: 12px;");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(300);

        card.getChildren().addAll(titleLabel, descLabel);

        card.setOnMouseClicked(e -> {
            if (title.contains("Dark")) {
                currentTheme = DARK_THEME;
            } else {
                currentTheme = LIGHT_THEME;
            }
            applyTheme(currentTheme);
            if (onThemeChange != null) {
                onThemeChange.run();
            }
        });

        return card;
    }

    private void applyTheme(String theme) {
        // Theme application would be handled by the main application
        // This is a placeholder for the actual theme switching logic
        System.out.println("Applying theme: " + theme);
    }

    private void applyAccentColor(String color) {
        // Accent color application
        System.out.println("Applying accent color: " + color);
    }

    private void applyFontSize(double size) {
        // Font size application
        System.out.println("Applying font size: " + size);
    }

    public void setOnThemeChange(Runnable callback) {
        this.onThemeChange = callback;
    }

    public String getCurrentTheme() {
        return currentTheme;
    }
}
