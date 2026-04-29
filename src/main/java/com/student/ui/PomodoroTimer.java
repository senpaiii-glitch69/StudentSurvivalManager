package com.student.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class PomodoroTimer {

    private int workMinutes = 25;
    private int breakMinutes = 5;
    private int currentSeconds;
    private boolean isWorkMode = true;
    private boolean isRunning = false;
    private Timeline timeline;
    private Label timeLabel;
    private Circle progressCircle;
    private Label modeLabel;
    private Runnable onSessionComplete;

    public Pane create() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Mode label
        modeLabel = new Label("FOCUS TIME");
        modeLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Timer display
        StackPane timerDisplay = new StackPane();
        timerDisplay.setAlignment(Pos.CENTER);

        // Progress circle
        progressCircle = new Circle(100);
        progressCircle.setFill(Color.TRANSPARENT);
        progressCircle.setStroke(Color.web("#6366f1"));
        progressCircle.setStrokeWidth(8);
        progressCircle.setStrokeType(StrokeType.INSIDE);

        // Time label
        timeLabel = new Label("25:00");
        timeLabel.setFont(Font.font("Segoe UI", 48));
        timeLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        timerDisplay.getChildren().addAll(progressCircle, timeLabel);

        // Controls
        HBox controls = new HBox(12);
        controls.setAlignment(Pos.CENTER);

        Button startBtn = createControlButton("▶ Start", "#22c55e");
        Button pauseBtn = createControlButton("⏸ Pause", "#f59e0b");
        Button resetBtn = createControlButton("↺ Reset", "#ef4444");

        startBtn.setOnAction(e -> startTimer());
        pauseBtn.setOnAction(e -> pauseTimer());
        resetBtn.setOnAction(e -> resetTimer());

        controls.getChildren().addAll(startBtn, pauseBtn, resetBtn);

        // Settings
        HBox settings = new HBox(20);
        settings.setAlignment(Pos.CENTER);

        VBox workSetting = createTimeSetting("Work (min)", workMinutes, this::setWorkMinutes);
        VBox breakSetting = createTimeSetting("Break (min)", breakMinutes, this::setBreakMinutes);

        settings.getChildren().addAll(workSetting, breakSetting);

        // Session counter
        Label sessionLabel = new Label("Sessions completed: 0");
        sessionLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px;");

        root.getChildren().addAll(modeLabel, timerDisplay, controls, settings, sessionLabel);

        resetTimer();

        return root;
    }

    private VBox createTimeSetting(String label, int value, java.util.function.Consumer<Integer> onChange) {
        VBox box = new VBox(8);
        box.setAlignment(Pos.CENTER);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");

        Spinner<Integer> spinner = new Spinner<>(1, 60, value);
        spinner.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");
        spinner.setPrefWidth(80);

        spinner.valueProperty().addListener((obs, old, newVal) -> {
            onChange.accept(newVal);
            if (!isRunning) {
                resetTimer();
            }
        });

        box.getChildren().addAll(lbl, spinner);
        return box;
    }

    private Button createControlButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;"
        );
        return btn;
    }

    private void startTimer() {
        if (isRunning) return;
        isRunning = true;

        if (timeline == null) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
            timeline.setCycleCount(Animation.INDEFINITE);
        }
        timeline.play();
    }

    private void pauseTimer() {
        isRunning = false;
        if (timeline != null) {
            timeline.pause();
        }
    }

    private void resetTimer() {
        pauseTimer();
        currentSeconds = isWorkMode ? workMinutes * 60 : breakMinutes * 60;
        updateDisplay();
        updateProgress();
    }

    private void tick() {
        currentSeconds--;
        updateDisplay();
        updateProgress();

        if (currentSeconds <= 0) {
            completeSession();
        }
    }

    private void completeSession() {
        pauseTimer();

        if (isWorkMode) {
            isWorkMode = false;
            modeLabel.setText("BREAK TIME");
            modeLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 14px; -fx-font-weight: bold;");
            progressCircle.setStroke(Color.web("#22c55e"));
        } else {
            isWorkMode = true;
            modeLabel.setText("FOCUS TIME");
            modeLabel.setStyle("-fx-text-fill: #818cf8; -fx-font-size: 14px; -fx-font-weight: bold;");
            progressCircle.setStroke(Color.web("#6366f1"));
        }

        resetTimer();

        if (onSessionComplete != null) {
            onSessionComplete.run();
        }
    }

    private void updateDisplay() {
        int minutes = currentSeconds / 60;
        int seconds = currentSeconds % 60;
        timeLabel.setText(String.format("%d:%02d", minutes, seconds));
    }

    private void updateProgress() {
        int totalSeconds = isWorkMode ? workMinutes * 60 : breakMinutes * 60;
        double progress = 1.0 - ((double) currentSeconds / totalSeconds);
        // Scale progress circle based on progress
        double scaleValue = 0.8 + (progress * 0.2); // Scale between 0.8 and 1.0
        progressCircle.setScaleX(scaleValue);
        progressCircle.setScaleY(scaleValue);
    }

    public void setWorkMinutes(int minutes) {
        this.workMinutes = minutes;
    }

    public void setBreakMinutes(int minutes) {
        this.breakMinutes = minutes;
    }

    public void setOnSessionComplete(Runnable callback) {
        this.onSessionComplete = callback;
    }
}
