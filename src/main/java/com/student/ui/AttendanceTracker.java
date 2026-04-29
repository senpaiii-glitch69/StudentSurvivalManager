package com.student.ui;

import com.student.model.Attendance;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceTracker {

    private javafx.collections.ObservableList<Attendance> attendance;
    private Label overallPercentage;
    private TableView<Attendance> table;

    public AttendanceTracker(List<Attendance> attendance) {
        this.attendance = FXCollections.observableArrayList(attendance);
    }

    public Pane create() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        Label title = new Label("📊 Attendance Tracker");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Stats cards
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        VBox overallCard = createStatCard("Overall Attendance", "0%", "#6366f1");
        VBox thisWeekCard = createStatCard("This Week", "0%", "#10b981");
        VBox thisMonthCard = createStatCard("This Month", "0%", "#f59e0b");

        overallPercentage = (Label) overallCard.getChildren().get(2);

        statsBox.getChildren().addAll(overallCard, thisWeekCard, thisMonthCard);

        // Subject filter
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Subject:");
        filterLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 13px;");

        ComboBox<String> subjectFilter = new ComboBox<>();
        subjectFilter.getItems().addAll("All Subjects");
        subjectFilter.getItems().addAll(getUniqueSubjects());
        subjectFilter.setValue("All Subjects");
        subjectFilter.setPrefWidth(150);
        subjectFilter.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        filterBox.getChildren().addAll(filterLabel, subjectFilter);

        // Add attendance form
        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(18));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );

        Label formTitle = new Label("MARK ATTENDANCE");
        formTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox formFields = new HBox(12);
        formFields.setAlignment(Pos.CENTER_LEFT);

        TextField subjectField = createFormField("Subject", 150);
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(140);
        datePicker.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        ToggleGroup statusGroup = new ToggleGroup();
        RadioButton presentBtn = new RadioButton("Present");
        RadioButton absentBtn = new RadioButton("Absent");
        presentBtn.setSelected(true);
        presentBtn.setToggleGroup(statusGroup);
        absentBtn.setToggleGroup(statusGroup);
        presentBtn.setStyle("-fx-text-fill: white;");
        absentBtn.setStyle("-fx-text-fill: white;");

        HBox statusBox = new HBox(15, presentBtn, absentBtn);
        statusBox.setAlignment(Pos.CENTER_LEFT);

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
            LocalDate date = datePicker.getValue();
            boolean present = presentBtn.isSelected();

            if (!subject.isEmpty() && date != null) {
                attendance.add(new Attendance(subject, date, present));
                subjectField.clear();
                updateStats();
                table.refresh();
            }
        });

        formFields.getChildren().addAll(
            createFieldLabel("Subject"), subjectField,
            createFieldLabel("Date"), datePicker,
            createFieldLabel("Status"), statusBox,
            addBtn
        );

        formCard.getChildren().addAll(formTitle, formFields);

        // Attendance table
        table = new TableView<>(attendance);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-background-color: transparent;");

        TableColumn<Attendance, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSubject()));
        subjectCol.setMaxWidth(150);

        TableColumn<Attendance, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        ));
        dateCol.setMaxWidth(120);

        TableColumn<Attendance, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().isPresent() ? "Present ✅" : "Absent ❌"
        ));
        statusCol.setMaxWidth(100);
        statusCol.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                if (val.contains("Present")) {
                    setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                }
            }
        });

        TableColumn<Attendance, Void> deleteCol = new TableColumn<>("");
        deleteCol.setMaxWidth(60);
        deleteCol.setCellFactory(col -> new TableCell<>() {
            final Button btn = new Button("🗑");
            {
                btn.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.2);" +
                    "-fx-text-fill: #f87171;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;"
                );
                btn.setOnAction(e -> attendance.remove(getTableRow().getItem()));
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(subjectCol, dateCol, statusCol, deleteCol);

        attendance.addListener((javafx.collections.ListChangeListener<Attendance>) c -> updateStats());

        root.getChildren().addAll(title, statsBox, filterBox, formCard, table);

        updateStats();

        return root;
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

    private List<String> getUniqueSubjects() {
        return attendance.stream()
            .map(Attendance::getSubject)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private void updateStats() {
        if (attendance.isEmpty()) {
            overallPercentage.setText("0%");
            return;
        }

        long present = attendance.stream().filter(Attendance::isPresent).count();
        double percentage = (present * 100.0) / attendance.size();
        overallPercentage.setText(String.format("%.1f%%", percentage));
    }

    public List<Attendance> getAttendance() {
        return attendance;
    }
}
