package com.student.ui;

import com.student.model.Note;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NotesModule {

    private javafx.collections.ObservableList<Note> notes;
    private Note currentNote;
    private TextArea contentArea;
    private TextField titleField;
    private TextField subjectField;
    private TextField tagsField;
    private Label lastSavedLabel;
    private TableView<Note> notesList;

    public NotesModule(List<Note> notes) {
        this.notes = FXCollections.observableArrayList(notes);
    }

    public Pane create() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📝 Notes");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("Search notes...");
        searchField.setPrefWidth(200);
        searchField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12;"
        );

        // Subject filter
        ComboBox<String> subjectFilter = new ComboBox<>();
        subjectFilter.getItems().addAll("All Subjects");
        subjectFilter.getItems().addAll(getUniqueSubjects());
        subjectFilter.setValue("All Subjects");
        subjectFilter.setPrefWidth(150);
        subjectFilter.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8;");

        // New note button
        Button newBtn = new Button("➕ New Note");
        newBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;"
        );

        newBtn.setOnAction(e -> createNewNote());

        header.getChildren().addAll(title, searchField, subjectFilter, newBtn);

        // Main content area
        HBox mainContent = new HBox(15);
        HBox.setHgrow(mainContent, Priority.ALWAYS);

        // Notes list
        VBox listPane = new VBox(10);
        listPane.setPrefWidth(300);

        notesList = new TableView<>(notes);
        @SuppressWarnings("deprecation")
        var policy = TableView.CONSTRAINED_RESIZE_POLICY;
        notesList.setColumnResizePolicy(policy);
        notesList.setPrefHeight(400);
        notesList.setStyle("-fx-background-color: transparent;");

        TableColumn<Note, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<Note, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSubject()));
        subjectCol.setMaxWidth(100);

        notesList.getColumns().addAll(List.of(titleCol, subjectCol));

        notesList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                loadNote(newVal);
            }
        });

        listPane.getChildren().addAll(notesList);

        // Note editor
        VBox editorPane = new VBox(10);
        editorPane.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 15;"
        );
        VBox.setVgrow(editorPane, Priority.ALWAYS);

        // Note header
        HBox noteHeader = new HBox(10);
        noteHeader.setAlignment(Pos.CENTER_LEFT);

        titleField = new TextField();
        titleField.setPromptText("Note title");
        titleField.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 18px;" +
            "-fx-font-weight: bold;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.3);"
        );
        HBox.setHgrow(titleField, Priority.ALWAYS);

        Button saveBtn = new Button("💾 Save");
        saveBtn.setStyle(
            "-fx-background-color: rgba(16,185,129,0.2);" +
            "-fx-text-fill: #6ee7b7;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 16;" +
            "-fx-cursor: hand;"
        );

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.2);" +
            "-fx-text-fill: #f87171;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12;" +
            "-fx-cursor: hand;"
        );

        noteHeader.getChildren().addAll(titleField, saveBtn, deleteBtn);

        // Note metadata
        HBox metadataBox = new HBox(15);
        metadataBox.setAlignment(Pos.CENTER_LEFT);

        subjectField = new TextField();
        subjectField.setPromptText("Subject");
        subjectField.setPrefWidth(150);
        subjectField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 10;"
        );

        tagsField = new TextField();
        tagsField.setPromptText("Tags (comma separated)");
        tagsField.setPrefWidth(200);
        tagsField.setStyle(
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 10;"
        );

        lastSavedLabel = new Label("");
        lastSavedLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");

        metadataBox.getChildren().addAll(subjectField, tagsField, lastSavedLabel);

        // Content area
        contentArea = new TextArea();
        contentArea.setPromptText("Start writing your note...");
        contentArea.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.3);" +
            "-fx-background-radius: 8;" +
            "-fx-font-size: 14px;" +
            "-fx-font-family: 'Segoe UI', sans-serif;"
        );
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        // Formatting toolbar
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 8 12;");

        Button boldBtn = new Button("B");
        boldBtn.setStyle(formatButtonStyle());
        Button italicBtn = new Button("I");
        italicBtn.setStyle(formatButtonStyle());
        Button underlineBtn = new Button("U");
        underlineBtn.setStyle(formatButtonStyle());

        toolbar.getChildren().addAll(boldBtn, italicBtn, underlineBtn);

        editorPane.getChildren().addAll(noteHeader, metadataBox, toolbar, contentArea);

        mainContent.getChildren().addAll(listPane, editorPane);

        // Event handlers
        saveBtn.setOnAction(e -> saveCurrentNote());
        deleteBtn.setOnAction(e -> deleteCurrentNote());

        titleField.textProperty().addListener((obs, old, newVal) -> {
            if (currentNote != null) {
                currentNote.setTitle(newVal);
                notesList.refresh();
            }
        });

        subjectField.textProperty().addListener((obs, old, newVal) -> {
            if (currentNote != null) {
                currentNote.setSubject(newVal);
                notesList.refresh();
            }
        });

        tagsField.textProperty().addListener((obs, old, newVal) -> {
            if (currentNote != null) {
                currentNote.setTags(newVal);
            }
        });

        contentArea.textProperty().addListener((obs, old, newVal) -> {
            if (currentNote != null) {
                currentNote.setContent(newVal);
                currentNote.touch();
            }
        });

        // Search
        searchField.textProperty().addListener((obs, old, newVal) -> {
            filterNotes(newVal, subjectFilter.getValue());
        });

        subjectFilter.valueProperty().addListener((obs, old, newVal) -> {
            filterNotes(searchField.getText(), newVal);
        });

        root.getChildren().addAll(header, mainContent);

        // Select first note if available
        if (!notes.isEmpty()) {
            notesList.getSelectionModel().select(0);
            loadNote(notes.get(0));
        }

        return root;
    }

    private void createNewNote() {
        Note note = new Note("Untitled Note", "", "");
        notes.add(note);
        notesList.getSelectionModel().select(note);
        loadNote(note);
        titleField.requestFocus();
    }

    private void loadNote(Note note) {
        currentNote = note;
        titleField.setText(note.getTitle());
        subjectField.setText(note.getSubject() != null ? note.getSubject() : "");
        tagsField.setText(note.getTags() != null ? note.getTags() : "");
        contentArea.setText(note.getContent() != null ? note.getContent() : "");

        if (note.getUpdatedAt() != null) {
            lastSavedLabel.setText("Last saved: " + note.getUpdatedAt().format(
                DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            ));
        }
    }

    private void saveCurrentNote() {
        if (currentNote != null) {
            currentNote.setTitle(titleField.getText());
            currentNote.setSubject(subjectField.getText());
            currentNote.setTags(tagsField.getText());
            currentNote.setContent(contentArea.getText());
            currentNote.touch();

            lastSavedLabel.setText("Saved: " + currentNote.getUpdatedAt().format(
                DateTimeFormatter.ofPattern("MMM dd, HH:mm")
            ));

            notesList.refresh();
        }
    }

    private void deleteCurrentNote() {
        if (currentNote != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Note");
            confirm.setHeaderText("Are you sure you want to delete this note?");
            confirm.setContentText("This action cannot be undone.");

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                notes.remove(currentNote);
                currentNote = null;
                titleField.clear();
                subjectField.clear();
                tagsField.clear();
                contentArea.clear();
                lastSavedLabel.setText("");
            }
        }
    }

    private void filterNotes(String searchText, String subject) {
        notesList.setItems(FXCollections.observableArrayList(
            notes.stream()
                .filter(n -> {
                    boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                        n.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                        (n.getContent() != null && n.getContent().toLowerCase().contains(searchText.toLowerCase()));
                    boolean matchesSubject = subject == null || subject.equals("All Subjects") ||
                        subject.equals(n.getSubject());
                    return matchesSearch && matchesSubject;
                })
                .collect(Collectors.toList())
        ));
    }

    private List<String> getUniqueSubjects() {
        return notes.stream()
            .map(Note::getSubject)
            .filter(s -> s != null && !s.isEmpty())
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private String formatButtonStyle() {
        return
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 12;" +
            "-fx-cursor: hand;" +
            "-fx-font-weight: bold;";
    }

    public List<Note> getNotes() {
        return notes;
    }
}
