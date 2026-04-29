package com.student.ui;

import com.student.model.EventItem;
import com.student.model.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CalendarView {

    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private GridPane calendarGrid;
    private Label monthLabel;
    private List<Task> tasks;
    private List<EventItem> events;
    private java.util.function.Consumer<LocalDate> onDateSelected;

    public CalendarView(List<Task> tasks, List<EventItem> events) {
        this.currentMonth = YearMonth.now();
        this.selectedDate = LocalDate.now();
        this.tasks = tasks;
        this.events = events;
    }

    public Pane create() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header with navigation
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Button prevBtn = new Button("◀");
        prevBtn.setStyle(navButtonStyle());
        prevBtn.setOnAction(e -> changeMonth(-1));

        Button nextBtn = new Button("▶");
        nextBtn.setStyle(navButtonStyle());
        nextBtn.setOnAction(e -> changeMonth(1));

        monthLabel = new Label();
        monthLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        monthLabel.setStyle("-fx-text-fill: white;");

        Button todayBtn = new Button("Today");
        todayBtn.setStyle(navButtonStyle());
        todayBtn.setOnAction(e -> goToToday());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(prevBtn, monthLabel, nextBtn, spacer, todayBtn);

        // Day headers
        HBox dayHeaders = new HBox();
        dayHeaders.setAlignment(Pos.CENTER);
        dayHeaders.setSpacing(5);

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            Label dayLabel = new Label(day);
            dayLabel.setPrefWidth(45);
            dayLabel.setAlignment(Pos.CENTER);
            dayLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px; -fx-font-weight: bold;");
            dayHeaders.getChildren().add(dayLabel);
        }

        // Calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(5);
        calendarGrid.setVgap(5);
        calendarGrid.setAlignment(Pos.CENTER);

        buildCalendar();

        // Selected date info
        VBox dateInfo = new VBox(10);
        dateInfo.setPadding(new Insets(15));
        dateInfo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 12;");

        Label dateTitle = new Label("Selected Date");
        dateTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");

        Label selectedDateLabel = new Label(selectedDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        selectedDateLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox itemsList = new VBox(5);
        itemsList.getChildren().add(new Label("Tasks & Events:"));
        itemsList.getChildren().get(0).setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");

        updateDateInfo(itemsList);

        dateInfo.getChildren().addAll(dateTitle, selectedDateLabel, itemsList);

        root.getChildren().addAll(header, dayHeaders, calendarGrid, dateInfo);

        return root;
    }

    private void buildCalendar() {
        calendarGrid.getChildren().clear();

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int daysInMonth = currentMonth.lengthOfMonth();
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        // Empty cells before first day
        for (int i = 0; i < startDayOfWeek; i++) {
            StackPane empty = new StackPane();
            empty.setPrefSize(45, 45);
            calendarGrid.add(empty, i, 0);
        }

        // Day cells
        int day = 1;
        int row = 0;
        int col = startDayOfWeek;

        while (day <= daysInMonth) {
            LocalDate date = currentMonth.atDay(day);
            StackPane cell = createDayCell(date, day);

            cell.setOnMouseClicked(e -> {
                selectedDate = date;
                onDateSelected.accept(date);
                buildCalendar();
            });

            calendarGrid.add(cell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
            day++;
        }
    }

    private StackPane createDayCell(LocalDate date, int dayNum) {
        StackPane cell = new StackPane();
        cell.setPrefSize(45, 45);

        boolean isToday = date.equals(LocalDate.now());
        boolean isSelected = date.equals(selectedDate);
        boolean hasItems = hasItemsOnDate(date);

        String bgColor = "rgba(255,255,255,0.05)";
        String textColor = "white";

        if (isToday) {
            bgColor = "rgba(99,102,241,0.3)";
            textColor = "#a5b4fc";
        }
        if (isSelected) {
            bgColor = "rgba(99,102,241,0.5)";
            textColor = "white";
        }

        cell.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        Label dayLabel = new Label(String.valueOf(dayNum));
        dayLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px; -fx-font-weight: bold;");

        cell.getChildren().add(dayLabel);

        // Indicator for items
        if (hasItems) {
            Rectangle indicator = new Rectangle(6, 6);
            indicator.setFill(Color.web("#22c55e"));
            indicator.setArcHeight(3);
            indicator.setArcWidth(3);
            StackPane.setAlignment(indicator, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(indicator, new Insets(0, 4, 4, 0));
            cell.getChildren().add(indicator);
        }

        return cell;
    }

    private boolean hasItemsOnDate(LocalDate date) {
        return tasks.stream().anyMatch(t -> t.getDate().equals(date)) ||
               events.stream().anyMatch(e -> e.getDate().equals(date));
    }

    private void updateDateInfo(VBox itemsList) {
        itemsList.getChildren().clear();
        itemsList.getChildren().add(new Label("Tasks & Events:"));
        itemsList.getChildren().get(0).setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");

        List<String> items = new ArrayList<>();

        tasks.stream()
            .filter(t -> t.getDate().equals(selectedDate))
            .forEach(t -> items.add("📋 " + t.getName()));

        events.stream()
            .filter(e -> e.getDate().equals(selectedDate))
            .forEach(e -> items.add("📅 " + e.getTitle()));

        if (items.isEmpty()) {
            Label empty = new Label("No items scheduled");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 12px;");
            itemsList.getChildren().add(empty);
        } else {
            for (String item : items) {
                Label itemLabel = new Label(item);
                itemLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
                itemsList.getChildren().add(itemLabel);
            }
        }
    }

    private void changeMonth(int delta) {
        currentMonth = currentMonth.plusMonths(delta);
        buildCalendar();
    }

    private void goToToday() {
        currentMonth = YearMonth.now();
        selectedDate = LocalDate.now();
        buildCalendar();
    }

    private String navButtonStyle() {
        return
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 6 12;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;";
    }

    public void setOnDateSelected(java.util.function.Consumer<LocalDate> callback) {
        this.onDateSelected = callback;
    }

    public void refresh() {
        buildCalendar();
    }
}
