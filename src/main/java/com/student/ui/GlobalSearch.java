package com.student.ui;

import com.student.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GlobalSearch {

    private List<Expense> expenses;
    private List<Task> tasks;
    private List<EventItem> events;
    private List<Note> notes;
    private List<Course> courses;
    private List<Goal> goals;

    public GlobalSearch(List<Expense> expenses, List<Task> tasks, List<EventItem> events,
                       List<Note> notes, List<Course> courses,
                       List<Goal> goals) {
        this.expenses = expenses;
        this.tasks = tasks;
        this.events = events;
        this.notes = notes;
        this.courses = courses;
        this.goals = goals;
    }

    public Pane create() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        Label title = new Label("🔍 Global Search");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Search across all data...");
        searchField.setPrefWidth(400);
        searchField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 16;" +
            "-fx-font-size: 14px;"
        );

        Button searchBtn = new Button("Search");
        searchBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 12 24;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;"
        );

        searchBox.getChildren().addAll(searchField, searchBtn);

        // Results area
        VBox resultsArea = new VBox(15);
        resultsArea.setPadding(new Insets(15));
        resultsArea.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 12;"
        );

        Label resultsLabel = new Label("Results");
        resultsLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 14px; -fx-font-weight: bold;");

        VBox resultsList = new VBox(10);

        Label hintLabel = new Label("Enter a search term above to find matching items across all your data.");
        hintLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 13px;");
        resultsList.getChildren().add(hintLabel);

        resultsArea.getChildren().addAll(resultsLabel, resultsList);

        // Search action
        Runnable doSearch = () -> {
            String query = searchField.getText().trim().toLowerCase();
            if (query.isEmpty()) {
                resultsList.getChildren().clear();
                resultsList.getChildren().add(hintLabel);
                return;
            }

            resultsList.getChildren().clear();

            List<SearchResult> results = new ArrayList<>();

            // Search expenses
            results.addAll(expenses.stream()
                .filter(e -> e.getName().toLowerCase().contains(query) ||
                           e.getCategory().toLowerCase().contains(query))
                .map(e -> new SearchResult(
                    "💸 Expense",
                    e.getName(),
                    e.getCategory() + " - ₹" + e.getValue(),
                    "expenses"
                ))
                .collect(Collectors.toList()));

            // Search tasks
            results.addAll(tasks.stream()
                .filter(t -> t.getName().toLowerCase().contains(query) ||
                           (t.getSubject() != null && t.getSubject().toLowerCase().contains(query)))
                .map(t -> new SearchResult(
                    "📋 Task",
                    t.getName(),
                    t.getStatus() + (t.getSubject() != null ? " - " + t.getSubject() : ""),
                    "tasks"
                ))
                .collect(Collectors.toList()));

            // Search events
            results.addAll(events.stream()
                .filter(e -> e.getTitle().toLowerCase().contains(query) ||
                           e.getLocation().toLowerCase().contains(query))
                .map(e -> new SearchResult(
                    "📅 Event",
                    e.getTitle(),
                    e.getStatus() + " - " + e.getLocation(),
                    "events"
                ))
                .collect(Collectors.toList()));

            // Search notes
            results.addAll(notes.stream()
                .filter(n -> n.getTitle().toLowerCase().contains(query) ||
                           (n.getContent() != null && n.getContent().toLowerCase().contains(query)) ||
                           (n.getSubject() != null && n.getSubject().toLowerCase().contains(query)))
                .map(n -> new SearchResult(
                    "📝 Note",
                    n.getTitle(),
                    n.getSubject() != null ? n.getSubject() : "No subject",
                    "notes"
                ))
                .collect(Collectors.toList()));

            // Search courses
            results.addAll(courses.stream()
                .filter(c -> c.getName().toLowerCase().contains(query) ||
                           c.getCode().toLowerCase().contains(query))
                .map(c -> new SearchResult(
                    "🎓 Course",
                    c.getName(),
                    c.getCode() + " - " + c.getLetterGrade(),
                    "courses"
                ))
                .collect(Collectors.toList()));

            // Search goals
            results.addAll(goals.stream()
                .filter(g -> g.getTitle().toLowerCase().contains(query) ||
                           (g.getDescription() != null && g.getDescription().toLowerCase().contains(query)))
                .map(g -> new SearchResult(
                    "🎯 Goal",
                    g.getTitle(),
                    g.getCategory() + " - " + (g.isCompleted() ? "Completed" : "In Progress"),
                    "goals"
                ))
                .collect(Collectors.toList()));

            if (results.isEmpty()) {
                Label noResults = new Label("No results found for \"" + query + "\"");
                noResults.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 13px;");
                resultsList.getChildren().add(noResults);
            } else {
                for (SearchResult result : results) {
                    HBox resultRow = new HBox(12);
                    resultRow.setAlignment(Pos.CENTER_LEFT);
                    resultRow.setPadding(new Insets(10, 15, 10, 15));
                    resultRow.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.05);" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
                    );

                    Label typeLabel = new Label(result.type);
                    typeLabel.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 12px; -fx-font-weight: bold;");
                    typeLabel.setPrefWidth(80);

                    Label titleLabel = new Label(result.title);
                    titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                    HBox.setHgrow(titleLabel, Priority.ALWAYS);

                    Label detailLabel = new Label(result.detail);
                    detailLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px;");

                    resultRow.getChildren().addAll(typeLabel, titleLabel, detailLabel);
                    resultsList.getChildren().add(resultRow);
                }

                Label countLabel = new Label("Found " + results.size() + " result" + (results.size() == 1 ? "" : "s"));
                countLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");
                resultsList.getChildren().add(0, countLabel);
            }
        };

        searchBtn.setOnAction(e -> doSearch.run());
        searchField.setOnAction(e -> doSearch.run());

        root.getChildren().addAll(title, searchBox, resultsArea);

        return root;
    }

    private static class SearchResult {
        String type;
        String title;
        String detail;
        @SuppressWarnings("unused")
        String category;

        SearchResult(String type, String title, String detail, String category) {
            this.type = type;
            this.title = title;
            this.detail = detail;
            this.category = category;
        }
    }
}
