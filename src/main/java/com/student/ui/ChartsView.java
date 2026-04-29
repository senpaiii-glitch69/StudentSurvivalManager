package com.student.ui;

import com.student.model.Expense;
import com.student.model.Task;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class ChartsView {

    private List<Expense> expenses;
    private List<Task> tasks;

    public ChartsView(List<Expense> expenses, List<Task> tasks) {
        this.expenses = expenses;
        this.tasks = tasks;
    }

    public Pane create() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        Label title = new Label("📈 Analytics");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Chart tabs
        HBox tabs = new HBox(10);
        tabs.setAlignment(Pos.CENTER_LEFT);

        Button expenseTab = new Button("Expenses");
        Button taskTab = new Button("Tasks");
        Button overviewTab = new Button("Overview");

        String activeStyle = "-fx-background-color: rgba(99,102,241,0.3); -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.5); -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;";

        expenseTab.setStyle(activeStyle);
        taskTab.setStyle(inactiveStyle);
        overviewTab.setStyle(inactiveStyle);

        StackPane chartArea = new StackPane();
        chartArea.setPrefHeight(400);

        Pane expenseChart = createExpenseChart();
        Pane taskChart = createTaskChart();
        Pane overviewChart = createOverviewChart();

        chartArea.getChildren().addAll(expenseChart, taskChart, overviewChart);
        taskChart.setVisible(false);
        overviewChart.setVisible(false);

        expenseTab.setOnAction(e -> {
            expenseTab.setStyle(activeStyle);
            taskTab.setStyle(inactiveStyle);
            overviewTab.setStyle(inactiveStyle);
            expenseChart.setVisible(true);
            taskChart.setVisible(false);
            overviewChart.setVisible(false);
        });

        taskTab.setOnAction(e -> {
            taskTab.setStyle(activeStyle);
            expenseTab.setStyle(inactiveStyle);
            overviewTab.setStyle(inactiveStyle);
            taskChart.setVisible(true);
            expenseChart.setVisible(false);
            overviewChart.setVisible(false);
        });

        overviewTab.setOnAction(e -> {
            overviewTab.setStyle(activeStyle);
            expenseTab.setStyle(inactiveStyle);
            taskTab.setStyle(inactiveStyle);
            overviewChart.setVisible(true);
            expenseChart.setVisible(false);
            taskChart.setVisible(false);
        });

        tabs.getChildren().addAll(expenseTab, taskTab, overviewTab);

        root.getChildren().addAll(title, tabs, chartArea);

        return root;
    }

    private Pane createExpenseChart() {
        VBox pane = new VBox(15);

        // Expense breakdown by category
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Expenses by Category");
        pieChart.setTitleSide(javafx.geometry.Side.TOP);
        pieChart.setLegendVisible(true);
        pieChart.setStyle("-fx-text-fill: white;");

        Map<String, Integer> categoryTotals = expenses.stream()
            .collect(Collectors.groupingBy(Expense::getCategory, Collectors.summingInt(Expense::getValue)));

        for (Map.Entry<String, Integer> entry : categoryTotals.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            pieChart.getData().add(slice);
        }

        if (pieChart.getData().isEmpty()) {
            pieChart.getData().add(new PieChart.Data("No Data", 1));
        }

        // Monthly expense trend
        BarChart<String, Number> barChart = new BarChart<>();
        barChart.setTitle("Monthly Expenses");
        barChart.getXAxis().setLabel("Month");
        barChart.getYAxis().setLabel("Amount");
        barChart.setLegendVisible(false);
        barChart.setStyle("-fx-text-fill: white;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        Map<String, Integer> monthlyTotals = expenses.stream()
            .collect(Collectors.groupingBy(
                e -> e.getDate().format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy")),
                Collectors.summingInt(Expense::getValue)
            ));

        for (Map.Entry<String, Integer> entry : monthlyTotals.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        if (series.getData().isEmpty()) {
            series.getData().add(new XYChart.Data<>("No Data", 0));
        }

        barChart.getData().add(series);

        pane.getChildren().addAll(pieChart, barChart);
        return pane;
    }

    private Pane createTaskChart() {
        VBox pane = new VBox(15);

        // Task completion status
        PieChart statusChart = new PieChart();
        statusChart.setTitle("Task Status");
        statusChart.setStyle("-fx-text-fill: white;");

        long completed = tasks.stream().filter(Task::isDone).count();
        long pending = tasks.stream().filter(t -> !t.isDone() && ChronoUnit.DAYS.between(LocalDate.now(), t.getDate()) >= 0).count();
        long overdue = tasks.stream().filter(t -> !t.isDone() && ChronoUnit.DAYS.between(LocalDate.now(), t.getDate()) < 0).count();

        statusChart.getData().add(new PieChart.Data("Completed", completed));
        statusChart.getData().add(new PieChart.Data("Pending", pending));
        statusChart.getData().add(new PieChart.Data("Overdue", overdue));

        if (completed + pending + overdue == 0) {
            statusChart.getData().clear();
            statusChart.getData().add(new PieChart.Data("No Tasks", 1));
        }

        // Priority distribution
        BarChart<String, Number> priorityChart = new BarChart<>();
        priorityChart.setTitle("Tasks by Priority");
        priorityChart.getXAxis().setLabel("Priority");
        priorityChart.getYAxis().setLabel("Count");
        priorityChart.setLegendVisible(false);
        priorityChart.setStyle("-fx-text-fill: white;");

        XYChart.Series<String, Number> prioritySeries = new XYChart.Series<>();

        Map<String, Long> priorityCounts = tasks.stream()
            .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));

        for (Map.Entry<String, Long> entry : priorityCounts.entrySet()) {
            prioritySeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        if (prioritySeries.getData().isEmpty()) {
            prioritySeries.getData().add(new XYChart.Data<>("No Data", 0));
        }

        priorityChart.getData().add(prioritySeries);

        pane.getChildren().addAll(statusChart, priorityChart);
        return pane;
    }

    private Pane createOverviewChart() {
        VBox pane = new VBox(20);
        pane.setAlignment(Pos.CENTER);

        // Summary stats
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        VBox totalExpenses = createSummaryCard("💸", "Total Expenses",
            "₹" + expenses.stream().mapToInt(Expense::getValue).sum(), "#6366f1");

        long completedTasks = tasks.stream().filter(Task::isDone).count();
        long totalTasks = tasks.size();
        double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;

        VBox taskRate = createSummaryCard("✅", "Task Completion",
            String.format("%.1f%%", completionRate), "#10b981");

        VBox upcomingTasks = createSummaryCard("📋", "Upcoming Tasks",
            String.valueOf(tasks.stream()
                .filter(t -> !t.isDone() && ChronoUnit.DAYS.between(LocalDate.now(), t.getDate()) >= 0)
                .count()), "#f59e0b");

        statsBox.getChildren().addAll(totalExpenses, taskRate, upcomingTasks);

        // Activity summary
        VBox activityBox = new VBox(10);
        activityBox.setPadding(new Insets(20));
        activityBox.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 14;"
        );

        Label activityTitle = new Label("Recent Activity");
        activityTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox activityList = new VBox(8);

        List<String> activities = new ArrayList<>();

        expenses.stream()
            .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
            .limit(3)
            .forEach(e -> activities.add("💸 Added expense: " + e.getName()));

        tasks.stream()
            .filter(Task::isDone)
            .sorted((t1, t2) -> t2.getDate().compareTo(t1.getDate()))
            .limit(3)
            .forEach(t -> activities.add("✅ Completed: " + t.getName()));

        if (activities.isEmpty()) {
            Label noActivity = new Label("No recent activity");
            noActivity.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 12px;");
            activityList.getChildren().add(noActivity);
        } else {
            for (String activity : activities) {
                Label activityLabel = new Label(activity);
                activityLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px;");
                activityList.getChildren().add(activityLabel);
            }
        }

        activityBox.getChildren().addAll(activityTitle, activityList);

        pane.getChildren().addAll(statsBox, activityBox);

        return pane;
    }

    private VBox createSummaryCard(String icon, String label, String value, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(160);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + color + ", " + darkenColor(color) + ");" +
            "-fx-background-radius: 16;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 32px;");

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: white;");

        Label labelLabel = new Label(label);
        labelLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");

        card.getChildren().addAll(iconLabel, valueLabel, labelLabel);
        return card;
    }

    private String darkenColor(String color) {
        return color.replace("6366f1", "4f46e5")
                   .replace("10b981", "059669")
                   .replace("f59e0b", "d97706");
    }
}
