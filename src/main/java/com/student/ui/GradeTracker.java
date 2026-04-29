package com.student.ui;

import com.student.model.Course;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.stream.Collectors;

public class GradeTracker {

    private ObservableList<Course> courses;
    private Label gpaLabel;
    private Label creditsLabel;
    private TableView<Course> table;

    public GradeTracker(List<Course> courses) {
        this.courses = FXCollections.observableArrayList(courses);
    }

    public Pane create() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        Label title = new Label("📊 Grade Tracker");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Stats cards
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        VBox gpaCard = createStatCard("Current GPA", "0.00", "#6366f1");
        VBox creditsCard = createStatCard("Total Credits", "0", "#0891b2");
        VBox coursesCard = createStatCard("Courses", "0", "#10b981");

        gpaLabel = (Label) gpaCard.getChildren().get(2);
        creditsLabel = (Label) creditsCard.getChildren().get(2);
        ((Label) coursesCard.getChildren().get(2)).textProperty().bind(
            javafx.beans.binding.Bindings.size(courses).asString()
        );

        statsBox.getChildren().addAll(gpaCard, creditsCard, coursesCard);

        // Add course form
        VBox formCard = new VBox(12);
        formCard.setPadding(new Insets(18));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 14;" +
            "-fx-border-color: rgba(255,255,255,0.08);" +
            "-fx-border-radius: 14;" +
            "-fx-border-width: 1;"
        );

        Label formTitle = new Label("ADD NEW COURSE");
        formTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox formFields = new HBox(12);
        formFields.setAlignment(Pos.CENTER_LEFT);

        TextField nameField = createFormField("Course Name", 180);
        TextField codeField = createFormField("Code (e.g. CS101)", 100);
        Spinner<Double> creditsSpinner = new Spinner<>(0.5, 10.0, 3.0, 0.5);
        creditsSpinner.setPrefWidth(80);
        creditsSpinner.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");
        Spinner<Double> gradeSpinner = new Spinner<>(0.0, 100.0, 85.0, 0.5);
        gradeSpinner.setPrefWidth(80);
        gradeSpinner.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");
        ComboBox<String> semesterBox = new ComboBox<>();
        semesterBox.getItems().addAll("Fall 2024", "Spring 2025", "Summer 2025", "Fall 2025");
        semesterBox.setValue("Fall 2024");
        semesterBox.setPrefWidth(120);
        semesterBox.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

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
            String name = nameField.getText().trim();
            String code = codeField.getText().trim();
            double credits = creditsSpinner.getValue();
            double grade = gradeSpinner.getValue();
            String semester = semesterBox.getValue();

            if (!name.isEmpty() && !code.isEmpty()) {
                courses.add(new Course(name, code, credits, grade, semester));
                nameField.clear();
                codeField.clear();
                updateStats();
            }
        });

        formFields.getChildren().addAll(
            createFieldLabel("Name"), nameField,
            createFieldLabel("Code"), codeField,
            createFieldLabel("Credits"), creditsSpinner,
            createFieldLabel("Grade"), gradeSpinner,
            createFieldLabel("Semester"), semesterBox,
            addBtn
        );

        formCard.getChildren().addAll(formTitle, formFields);

        // Courses table
        table = new TableView<>(courses);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        table.setStyle("-fx-background-color: transparent;");

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCode()));
        codeCol.setMaxWidth(100);

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Course, Number> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getCredits()));
        creditsCol.setMaxWidth(80);

        TableColumn<Course, Number> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getGrade()));
        gradeCol.setMaxWidth(80);

        TableColumn<Course, String> letterCol = new TableColumn<>("Letter");
        letterCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLetterGrade()));
        letterCol.setMaxWidth(70);

        TableColumn<Course, String> semesterCol = new TableColumn<>("Semester");
        semesterCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSemester()));
        semesterCol.setMaxWidth(120);

        TableColumn<Course, Void> deleteCol = new TableColumn<>("");
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
                btn.setOnAction(e -> courses.remove(getTableRow().getItem()));
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        table.getColumns().addAll(codeCol, nameCol, creditsCol, gradeCol, letterCol, semesterCol, deleteCol);

        courses.addListener((javafx.collections.ListChangeListener<Course>) c -> updateStats());

        root.getChildren().addAll(title, statsBox, formCard, table);

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
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        val.setStyle("-fx-text-fill: white;");

        card.getChildren().addAll(lbl, val);
        return card;
    }

    private String darkenColor(String color) {
        return color.replace("6366f1", "4f46e5")
                   .replace("0891b2", "0e7490")
                   .replace("10b981", "059669");
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
        double totalCredits = courses.stream().mapToDouble(Course::getCredits).sum();
        double totalPoints = courses.stream()
            .mapToDouble(c -> c.getGradePoints() * c.getCredits())
            .sum();

        double gpa = totalCredits > 0 ? totalPoints / totalCredits : 0.0;

        gpaLabel.setText(String.format("%.2f", gpa));
        creditsLabel.setText(String.format("%.1f", totalCredits));
    }

    public List<Course> getCourses() {
        return courses;
    }
}
