package com.student.ui;

import com.student.model.Goal;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class GoalSetting {

    private javafx.collections.ObservableList<Goal> goals;
    private VBox root;

    public GoalSetting(List<Goal> goals) {
        this.goals = FXCollections.observableArrayList(goals);
    }

    public Pane create() {
        root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🎯 Academic Goals");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        Button addBtn = new Button("➕ New Goal");
        addBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;"
        );

        addBtn.setOnAction(e -> showAddDialog());

        header.getChildren().addAll(title, addBtn);

        // Goals grid
        VBox goalsContainer = new VBox(15);

        goals.addListener((javafx.collections.ListChangeListener<Goal>) c -> {
            refreshGoals(goalsContainer);
        });

        refreshGoals(goalsContainer);

        ScrollPane scrollPane = new ScrollPane(goalsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(header, scrollPane);

        return root;
    }

    private void refreshGoals(VBox container) {
        container.getChildren().clear();

        if (goals.isEmpty()) {
            Label emptyLabel = new Label("No goals set yet. Click 'New Goal' to get started!");
            emptyLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 14px;");
            container.getChildren().add(emptyLabel);
            return;
        }

        // Group by category
        java.util.Map<String, List<Goal>> grouped = goals.stream()
            .collect(Collectors.groupingBy(g -> g.getCategory() != null ? g.getCategory() : "Other"));

        for (java.util.Map.Entry<String, List<Goal>> entry : grouped.entrySet()) {
            VBox categoryBox = new VBox(10);
            categoryBox.setPadding(new Insets(15));
            categoryBox.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                "-fx-background-radius: 12;"
            );

            Label categoryLabel = new Label(entry.getKey());
            categoryLabel.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 14px; -fx-font-weight: bold;");

            VBox goalsList = new VBox(10);

            for (Goal goal : entry.getValue()) {
                VBox goalCard = createGoalCard(goal);
                goalsList.getChildren().add(goalCard);
            }

            categoryBox.getChildren().addAll(categoryLabel, goalsList);
            container.getChildren().add(categoryBox);
        }
    }

    private VBox createGoalCard(Goal goal) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-radius: 10;" +
            "-fx-border-width: 1;"
        );

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(goal.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Status badge
        String statusColor = goal.isCompleted() ? "#22c55e" : "#f59e0b";
        Label statusLabel = new Label(goal.isCompleted() ? "✅ Completed" : "🎯 In Progress");
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        header.getChildren().addAll(titleLabel, statusLabel);

        // Progress bar
        double progress = goal.getProgress();
        ProgressBar progressBar = new ProgressBar(progress / 100);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setStyle(
            "-fx-accent: " + (goal.isCompleted() ? "#22c55e" : "#6366f1") + ";" +
            "-fx-control-inner-background: rgba(255,255,255,0.1);"
        );

        // Progress text
        Label progressLabel = new Label(goal.getProgressString());
        progressLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px;");

        // Deadline
        if (goal.getDeadline() != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), goal.getDeadline());
            Label deadlineLabel = new Label();
            if (daysLeft < 0) {
                deadlineLabel.setText("⚠️ Overdue by " + Math.abs(daysLeft) + " days");
                deadlineLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px;");
            } else if (daysLeft == 0) {
                deadlineLabel.setText("⚡ Due today!");
                deadlineLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 11px;");
            } else {
                deadlineLabel.setText("📅 Due in " + daysLeft + " days");
                deadlineLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
            }
            card.getChildren().add(deadlineLabel);
        }

        // Actions
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button updateBtn = new Button("Update Progress");
        updateBtn.setStyle(
            "-fx-background-color: rgba(99,102,241,0.2);" +
            "-fx-text-fill: #a5b4fc;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 12;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 11px;"
        );

        Button completeBtn = new Button(goal.isCompleted() ? "Mark Incomplete" : "Mark Complete");
        completeBtn.setStyle(
            "-fx-background-color: " + (goal.isCompleted() ? "rgba(245,158,11,0.2)" : "rgba(34,197,94,0.2)") + ";" +
            "-fx-text-fill: " + (goal.isCompleted() ? "#fbbf24" : "#6ee7b7") + ";" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 12;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 11px;"
        );

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.2);" +
            "-fx-text-fill: #f87171;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 10;" +
            "-fx-cursor: hand;"
        );

        updateBtn.setOnAction(e -> showUpdateDialog(goal));
        completeBtn.setOnAction(e -> {
            goal.setCompleted(!goal.isCompleted());
            refreshGoals((VBox) ((ScrollPane) ((VBox) root.getChildren().get(1)).getContent()));
        });
        deleteBtn.setOnAction(e -> {
            goals.remove(goal);
            refreshGoals((VBox) ((ScrollPane) ((VBox) root.getChildren().get(1)).getContent()));
        });

        actions.getChildren().addAll(updateBtn, completeBtn, deleteBtn);

        card.getChildren().addAll(header, progressBar, progressLabel, actions);

        return card;
    }

    private void showAddDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Goal");
        dialog.setHeaderText("Set a new academic goal");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField titleField = new TextField();
        titleField.setPromptText("Goal title (e.g., Complete 10 assignments)");
        titleField.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");

        TextArea descField = new TextArea();
        descField.setPromptText("Description (optional)");
        descField.setPrefRowCount(3);
        descField.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Assignments", "Study Hours", "Grades", "Attendance", "Exams", "Other");
        categoryBox.setValue("Other");
        categoryBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8;");

        HBox targetBox = new HBox(10);
        targetBox.setAlignment(Pos.CENTER_LEFT);

        Spinner<Double> targetSpinner = new Spinner<>(0.1, 1000, 10, 0.5);
        targetSpinner.setPrefWidth(100);
        targetSpinner.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        TextField unitField = new TextField();
        unitField.setPromptText("Unit (e.g., assignments, hours, %)");
        unitField.setPrefWidth(120);
        unitField.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");

        targetBox.getChildren().addAll(targetSpinner, unitField);

        DatePicker deadlinePicker = new DatePicker(LocalDate.now().plusWeeks(4));
        deadlinePicker.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        content.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Description:"), descField,
            new Label("Category:"), categoryBox,
            new Label("Target:"), targetBox,
            new Label("Deadline:"), deadlinePicker
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String title = titleField.getText().trim();
                if (!title.isEmpty()) {
                    Goal goal = new Goal(
                        title,
                        categoryBox.getValue(),
                        targetSpinner.getValue(),
                        unitField.getText().trim(),
                        deadlinePicker.getValue()
                    );
                    goal.setDescription(descField.getText().trim());
                    goals.add(goal);
                }
            }
        });
    }

    private void showUpdateDialog(Goal goal) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Progress");
        dialog.setHeaderText("Update progress for: " + goal.getTitle());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label currentLabel = new Label("Current: " + goal.getProgressString());
        currentLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 13px;");

        HBox updateBox = new HBox(10);
        updateBox.setAlignment(Pos.CENTER_LEFT);

        Spinner<Double> valueSpinner = new Spinner<>(0, goal.getTargetValue() * 1.5, goal.getCurrentValue(), 0.5);
        valueSpinner.setPrefWidth(100);
        valueSpinner.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        Label unitLabel = new Label(goal.getUnit());
        unitLabel.setStyle("-fx-text-fill: white;");

        updateBox.getChildren().addAll(valueSpinner, unitLabel);

        content.getChildren().addAll(currentLabel, new Label("New Value:"), updateBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                goal.setCurrentValue(valueSpinner.getValue());
                if (goal.getCurrentValue() >= goal.getTargetValue()) {
                    goal.setCompleted(true);
                }
                refreshGoals((VBox) ((ScrollPane) ((VBox) root.getChildren().get(1)).getContent()));
            }
        });
    }

    public List<Goal> getGoals() {
        return goals;
    }
}
