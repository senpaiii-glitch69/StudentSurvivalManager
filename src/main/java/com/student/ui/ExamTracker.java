package com.student.ui;

import com.student.model.Exam;
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
import java.util.List;
import java.util.stream.Collectors;

public class ExamTracker {

    private javafx.collections.ObservableList<Exam> exams;
    private Label avgScoreLabel;
    private Label completedLabel;
    private TableView<Exam> table;

    public ExamTracker(List<Exam> exams) {
        this.exams = FXCollections.observableArrayList(exams);
    }

    public Pane create() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        Label title = new Label("📝 Exam Tracker");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Stats cards
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        VBox avgCard = createStatCard("Average Score", "0%", "#6366f1");
        VBox completedCard = createStatCard("Completed", "0", "#10b981");
        VBox upcomingCard = createStatCard("Upcoming", "0", "#f59e0b");

        avgScoreLabel = (Label) avgCard.getChildren().get(2);
        completedLabel = (Label) completedCard.getChildren().get(2);

        statsBox.getChildren().addAll(avgCard, completedCard, upcomingCard);

        // Add exam form
        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(18));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );

        Label formTitle = new Label("ADD NEW EXAM");
        formTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox formFields = new HBox(12);
        formFields.setAlignment(Pos.CENTER_LEFT);

        TextField subjectField = createFormField("Subject", 120);
        TextField titleField = createFormField("Exam Title", 180);
        DatePicker datePicker = new DatePicker(LocalDate.now().plusWeeks(1));
        datePicker.setPrefWidth(130);
        datePicker.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        Button addBtn = new Button("➕ Add");
        addBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 20;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;"
        );

        addBtn.setOnAction(e -> {
            String subject = subjectField.getText().trim();
            String examTitle = titleField.getText().trim();
            LocalDate date = datePicker.getValue();

            if (!subject.isEmpty() && !examTitle.isEmpty() && date != null) {
                exams.add(new Exam(subject, examTitle, date));
                subjectField.clear();
                titleField.clear();
                updateStats();
                table.refresh();
            }
        });

        formFields.getChildren().addAll(
            createFieldLabel("Subject"), subjectField,
            createFieldLabel("Title"), titleField,
            createFieldLabel("Date"), datePicker,
            addBtn
        );

        formCard.getChildren().addAll(formTitle, formFields);

        // Exams table
        table = new TableView<>(exams);
        @SuppressWarnings("deprecation")
        var policy = TableView.CONSTRAINED_RESIZE_POLICY;
        table.setColumnResizePolicy(policy);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-background-color: transparent;");

        TableColumn<Exam, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSubject()));
        subjectCol.setMaxWidth(120);

        TableColumn<Exam, String> titleCol = new TableColumn<>("Exam Title");
        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<Exam, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        ));
        dateCol.setMaxWidth(120);

        TableColumn<Exam, String> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getScore() > 0 ? String.format("%.1f/%.0f", c.getValue().getScore(), c.getValue().getMaxScore()) : "-"
        ));
        scoreCol.setMaxWidth(100);

        TableColumn<Exam, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getScore() > 0 ? c.getValue().getGrade() : "-"
        ));
        gradeCol.setMaxWidth(70);

        TableColumn<Exam, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        statusCol.setMaxWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                if (val.equals("Completed")) {
                    setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                } else if (val.equals("Upcoming")) {
                    setStyle("-fx-text-fill: #60a5fa;");
                } else {
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Exam, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setMaxWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button resultBtn = new Button("Result");
            final Button deleteBtn = new Button("🗑");
            {
                HBox actions = new HBox(5, resultBtn, deleteBtn);
                resultBtn.setStyle(
                    "-fx-background-color: rgba(99,102,241,0.2);" +
                    "-fx-text-fill: #a5b4fc;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 11px;"
                );
                deleteBtn.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.2);" +
                    "-fx-text-fill: #f87171;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
                );

                resultBtn.setOnAction(e -> showResultDialog(getTableRow().getItem()));
                deleteBtn.setOnAction(e -> exams.remove(getTableRow().getItem()));
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, resultBtn, deleteBtn));
            }
        });

        table.getColumns().addAll(subjectCol, titleCol, dateCol, scoreCol, gradeCol, statusCol, actionCol);

        exams.addListener((javafx.collections.ListChangeListener<Exam>) c -> updateStats());

        root.getChildren().addAll(title, statsBox, formCard, table);

        updateStats();

        return root;
    }

    private void showResultDialog(Exam exam) {
        if (exam == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Enter Exam Result");
        dialog.setHeaderText("Enter your score for: " + exam.getTitle());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);

        TextField scoreField = new TextField(String.valueOf(exam.getScore()));
        scoreField.setPrefWidth(80);
        scoreField.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");

        Label slashLabel = new Label("/");
        slashLabel.setStyle("-fx-text-fill: white;");

        TextField maxField = new TextField(String.valueOf(exam.getMaxScore() > 0 ? exam.getMaxScore() : 100));
        maxField.setPrefWidth(80);
        maxField.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");

        scoreBox.getChildren().addAll(scoreField, slashLabel, maxField);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("Completed", "Missed", "Cancelled");
        statusBox.setValue(exam.getStatus());
        statusBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        content.getChildren().addAll(new Label("Score:"), scoreBox, new Label("Status:"), statusBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    exam.setScore(Double.parseDouble(scoreField.getText()));
                    exam.setMaxScore(Double.parseDouble(maxField.getText()));
                    exam.setStatus(statusBox.getValue());
                    updateStats();
                    table.refresh();
                } catch (NumberFormatException ignored) {}
            }
        });
    }

    private VBox createStatCard(String label, String value, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setPrefWidth(140);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + color + ", " + darkenColor(color) + ");" +
            "-fx-background-radius: 14;"
        );

        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");

        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        val.setStyle("-fx-text-fill: white;");

        card.getChildren().addAll(lbl, val);
        return card;
    }

    private String darkenColor(String color) {
        return color.replace("6366f1", "4f46e5")
                   .replace("10b981", "059669")
                   .replace("f59e0b", "d97706");
    }

    private TextField createFormField(String prompt, int width) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(width);
        field.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12;" +
            "-fx-font-size: 13px;"
        );
        return field;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10px;");
        return label;
    }

    private void updateStats() {
        List<Exam> completed = exams.stream()
            .filter(e -> e.getStatus().equals("Completed") && e.getScore() > 0)
            .collect(Collectors.toList());

        if (!completed.isEmpty()) {
            double avg = completed.stream()
                .mapToDouble(Exam::getPercentage)
                .average()
                .orElse(0);
            avgScoreLabel.setText(String.format("%.1f%%", avg));
        } else {
            avgScoreLabel.setText("0%");
        }

        completedLabel.setText(String.valueOf(completed.size()));

        // Update upcoming count
        long upcoming = exams.stream()
            .filter(e -> e.getStatus().equals("Upcoming"))
            .count();
        // Note: Complex UI hierarchy update requires proper root reference
    }

    public List<Exam> getExams() {
        return exams;
    }
}
