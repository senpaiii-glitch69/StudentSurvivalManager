package com.student.ui;

import com.student.model.ScheduleItem;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class StudySchedule {

    private javafx.collections.ObservableList<ScheduleItem> schedule;
    private GridPane scheduleGrid;

    public StudySchedule(List<ScheduleItem> schedule) {
        this.schedule = FXCollections.observableArrayList(schedule);
    }

    public Pane create() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🗓 Study Schedule");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        Button addBtn = new Button("➕ Add Class");
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

        // Schedule grid
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        scheduleGrid = new GridPane();
        scheduleGrid.setHgap(8);
        scheduleGrid.setVgap(8);
        scheduleGrid.setPadding(new Insets(10));
        scheduleGrid.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 12;");

        // Time slots
        String[] timeSlots = {"8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        String[] days = {"Time", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        // Header row
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px; -fx-font-weight: bold;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(100);
            dayLabel.setPrefHeight(35);
            scheduleGrid.add(dayLabel, i, 0);
        }

        // Time slots and cells
        for (int row = 0; row < timeSlots.length; row++) {
            // Time label
            Label timeLabel = new Label(timeSlots[row]);
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");
            timeLabel.setAlignment(Pos.CENTER);
            timeLabel.setPrefWidth(100);
            timeLabel.setPrefHeight(50);
            scheduleGrid.add(timeLabel, 0, row + 1);

            // Day cells
            for (int col = 1; col < days.length; col++) {
                DayOfWeek day = DayOfWeek.of(col);
                LocalTime time = LocalTime.of(8 + row, 0);

                StackPane cell = new StackPane();
                cell.setPrefWidth(100);
                cell.setPrefHeight(50);
                cell.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 6;");

                // Find schedule item for this slot
                ScheduleItem item = findScheduleItem(day, time);
                if (item != null) {
                    cell.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #6366f1, #8b5cf6);" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
                    );

                    VBox itemBox = new VBox(3);
                    itemBox.setAlignment(Pos.CENTER);

                    Label subjectLabel = new Label(item.getSubject());
                    subjectLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");

                    Label timeLabel2 = new Label(item.getTimeRange());
                    timeLabel2.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 9px;");

                    itemBox.getChildren().addAll(subjectLabel, timeLabel2);
                    cell.getChildren().add(itemBox);

                    // Click to edit/delete
                    cell.setOnMouseClicked(e -> showItemDialog(item));
                }

                // Double-click to add
                cell.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && item == null) {
                        showAddDialog(day, time);
                    }
                });

                scheduleGrid.add(cell, col, row + 1);
            }
        }

        scrollPane.setContent(scheduleGrid);

        // Today's schedule
        VBox todayBox = new VBox(10);
        todayBox.setPadding(new Insets(15));
        todayBox.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 12;"
        );

        Label todayTitle = new Label("Today's Classes");
        todayTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox todayList = new VBox(8);

        DayOfWeek today = java.time.LocalDate.now().getDayOfWeek();
        List<ScheduleItem> todayItems = schedule.stream()
            .filter(s -> s.getDayOfWeek() == today)
            .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
            .collect(Collectors.toList());

        if (todayItems.isEmpty()) {
            Label noClasses = new Label("No classes scheduled for today");
            noClasses.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 12px;");
            todayList.getChildren().add(noClasses);
        } else {
            for (ScheduleItem item : todayItems) {
                HBox itemRow = new HBox(10);
                itemRow.setAlignment(Pos.CENTER_LEFT);
                itemRow.setPadding(new Insets(8, 12, 8, 12));
                itemRow.setStyle(
                    "-fx-background-color: rgba(99,102,241,0.15);" +
                    "-fx-background-radius: 8;"
                );

                Label timeLabel = new Label(item.getTimeRange());
                timeLabel.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 12px; -fx-font-weight: bold;");

                Label subjectLabel = new Label(item.getSubject());
                subjectLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");

                Label locLabel = new Label(item.getLocation());
                locLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button deleteBtn = new Button("🗑");
                deleteBtn.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.2);" +
                    "-fx-text-fill: #f87171;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-font-size: 11px;"
                );
                deleteBtn.setOnAction(e -> {
                    schedule.remove(item);
                    refreshSchedule();
                });

                itemRow.getChildren().addAll(timeLabel, subjectLabel, locLabel, spacer, deleteBtn);
                todayList.getChildren().add(itemRow);
            }
        }

        todayBox.getChildren().addAll(todayTitle, todayList);

        root.getChildren().addAll(header, scrollPane, todayBox);

        return root;
    }

    private ScheduleItem findScheduleItem(DayOfWeek day, LocalTime time) {
        return schedule.stream()
            .filter(s -> s.getDayOfWeek() == day)
            .filter(s -> {
                LocalTime start = s.getStartTime();
                LocalTime end = s.getEndTime();
                return !time.isBefore(start) && time.isBefore(end);
            })
            .findFirst()
            .orElse(null);
    }

    private void showAddDialog() {
        showAddDialog(null, null);
    }

    private void showAddDialog(DayOfWeek defaultDay, LocalTime defaultTime) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Class");
        dialog.setHeaderText("Add a new class to your schedule");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");
        subjectField.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");

        ComboBox<DayOfWeek> dayBox = new ComboBox<>();
        dayBox.getItems().addAll(DayOfWeek.values());
        dayBox.setValue(defaultDay != null ? defaultDay : DayOfWeek.MONDAY);
        dayBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8;");

        HBox timeBox = new HBox(10);
        timeBox.setAlignment(Pos.CENTER_LEFT);

        Spinner<Integer> startHour = new Spinner<>(8, 20, defaultTime != null ? defaultTime.getHour() : 9);
        startHour.setPrefWidth(70);
        startHour.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        Label toLabel = new Label("to");
        toLabel.setStyle("-fx-text-fill: white;");

        Spinner<Integer> endHour = new Spinner<>(9, 21, defaultTime != null ? defaultTime.getHour() + 1 : 10);
        endHour.setPrefWidth(70);
        endHour.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        timeBox.getChildren().addAll(startHour, toLabel, endHour);

        TextField locationField = new TextField();
        locationField.setPromptText("Location (optional)");
        locationField.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 12;");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Lecture", "Lab", "Tutorial", "Seminar");
        typeBox.setValue("Lecture");
        typeBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-background-radius: 8;");

        content.getChildren().addAll(
            new Label("Subject:"), subjectField,
            new Label("Day:"), dayBox,
            new Label("Time:"), timeBox,
            new Label("Location:"), locationField,
            new Label("Type:"), typeBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                String subject = subjectField.getText().trim();
                if (!subject.isEmpty()) {
                    ScheduleItem item = new ScheduleItem(
                        subject,
                        dayBox.getValue(),
                        LocalTime.of(startHour.getValue(), 0),
                        LocalTime.of(endHour.getValue(), 0),
                        locationField.getText().trim()
                    );
                    item.setType(typeBox.getValue());
                    schedule.add(item);
                    refreshSchedule();
                }
            }
        });
    }

    private void showItemDialog(ScheduleItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(item.getSubject());
        alert.setHeaderText(item.getSubject());
        alert.setContentText(
            "Day: " + item.getDayName() + "\n" +
            "Time: " + item.getTimeRange() + "\n" +
            "Location: " + (item.getLocation() != null ? item.getLocation() : "N/A") + "\n" +
            "Type: " + (item.getType() != null ? item.getType() : "N/A")
        );

        ButtonType editButton = new ButtonType("Edit");
        ButtonType deleteButton = new ButtonType("Delete");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(editButton, deleteButton, cancelButton);

        alert.showAndWait().ifPresent(result -> {
            if (result == deleteButton) {
                schedule.remove(item);
                refreshSchedule();
            } else if (result == editButton) {
                schedule.remove(item);
                showAddDialog(item.getDayOfWeek(), item.getStartTime());
            }
        });
    }

    private void refreshSchedule() {
        // Rebuild the schedule grid
        scheduleGrid.getChildren().clear();

        String[] timeSlots = {"8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"};
        String[] days = {"Time", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        // Header row
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px; -fx-font-weight: bold;");
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setPrefWidth(100);
            dayLabel.setPrefHeight(35);
            scheduleGrid.add(dayLabel, i, 0);
        }

        // Time slots and cells
        for (int row = 0; row < timeSlots.length; row++) {
            Label timeLabel = new Label(timeSlots[row]);
            timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");
            timeLabel.setAlignment(Pos.CENTER);
            timeLabel.setPrefWidth(100);
            timeLabel.setPrefHeight(50);
            scheduleGrid.add(timeLabel, 0, row + 1);

            for (int col = 1; col < days.length; col++) {
                DayOfWeek day = DayOfWeek.of(col);
                LocalTime time = LocalTime.of(8 + row, 0);

                StackPane cell = new StackPane();
                cell.setPrefWidth(100);
                cell.setPrefHeight(50);
                cell.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 6;");

                ScheduleItem item = findScheduleItem(day, time);
                if (item != null) {
                    cell.setStyle(
                        "-fx-background-color: linear-gradient(to bottom right, #6366f1, #8b5cf6);" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
                    );

                    VBox itemBox = new VBox(3);
                    itemBox.setAlignment(Pos.CENTER);

                    Label subjectLabel = new Label(item.getSubject());
                    subjectLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");

                    Label timeLabel2 = new Label(item.getTimeRange());
                    timeLabel2.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 9px;");

                    itemBox.getChildren().addAll(subjectLabel, timeLabel2);
                    cell.getChildren().add(itemBox);

                    cell.setOnMouseClicked(e -> showItemDialog(item));
                }

                cell.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 2 && item == null) {
                        showAddDialog(day, time);
                    }
                });

                scheduleGrid.add(cell, col, row + 1);
            }
        }
    }

    public List<ScheduleItem> getSchedule() {
        return schedule;
    }
}
