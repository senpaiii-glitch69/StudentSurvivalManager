package com.student;

import com.student.data.DataManager;
import com.student.model.*;
import com.student.ui.*;
import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;

import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StudyHubPro extends Application {

    private AppData appData;
    private User currentUser;

    // Observable lists for UI
    final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    final ObservableList<Task>    tasks    = FXCollections.observableArrayList();
    final ObservableList<EventItem> events = FXCollections.observableArrayList();
    final ObservableList<Course> courses = FXCollections.observableArrayList();
    final ObservableList<Attendance> attendance = FXCollections.observableArrayList();
    final ObservableList<Exam> exams = FXCollections.observableArrayList();
    final ObservableList<Note> notes = FXCollections.observableArrayList();
    final ObservableList<ScheduleItem> schedule = FXCollections.observableArrayList();
    final ObservableList<Goal> goals = FXCollections.observableArrayList();

    @Override
    public void init() {
        appData = DataManager.loadData();
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Student Survival Management");
        stage.setMinWidth(1000);
        stage.setMinHeight(700);

        showLogin(stage);
    }

    private void showLogin(Stage stage) {
        LoginScreen loginScreen = new LoginScreen(stage, user -> {
            currentUser = user;
            loadUserData();
            showMain(stage);
        });

        Scene scene = new Scene(loginScreen.create(), 960, 640);
        loadCSS(scene);
        stage.setScene(scene);
        stage.show();
    }

    private void loadUserData() {
        expenses.clear();
        tasks.clear();
        events.clear();
        courses.clear();
        attendance.clear();
        exams.clear();
        notes.clear();
        schedule.clear();
        goals.clear();

        expenses.addAll(appData.getExpenses());
        tasks.addAll(appData.getTasks());
        events.addAll(appData.getEvents());
        courses.addAll(appData.getCourses());
        attendance.addAll(appData.getAttendance() != null ? appData.getAttendance() : new ArrayList<>());
        exams.addAll(appData.getExams() != null ? appData.getExams() : new ArrayList<>());
        notes.addAll(appData.getNotes() != null ? appData.getNotes() : new ArrayList<>());
        schedule.addAll(appData.getSchedule() != null ? appData.getSchedule() : new ArrayList<>());
        goals.addAll(appData.getGoals() != null ? appData.getGoals() : new ArrayList<>());

        // Sync changes back to app data
        expenses.addListener((ListChangeListener<Expense>) c -> {
            appData.getExpenses().clear();
            appData.getExpenses().addAll(expenses);
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        tasks.addListener((ListChangeListener<Task>) c -> {
            appData.getTasks().clear();
            appData.getTasks().addAll(tasks);
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        events.addListener((ListChangeListener<EventItem>) c -> {
            appData.getEvents().clear();
            appData.getEvents().addAll(events);
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        courses.addListener((ListChangeListener<Course>) c -> {
            appData.getCourses().clear();
            appData.getCourses().addAll(courses);
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        attendance.addListener((ListChangeListener<Attendance>) c -> {
            appData.setAttendance(new ArrayList<>(attendance));
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        exams.addListener((ListChangeListener<Exam>) c -> {
            appData.setExams(new ArrayList<>(exams));
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        notes.addListener((ListChangeListener<Note>) c -> {
            appData.setNotes(new ArrayList<>(notes));
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        schedule.addListener((ListChangeListener<ScheduleItem>) c -> {
            appData.setSchedule(new ArrayList<>(schedule));
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });

        goals.addListener((ListChangeListener<Goal>) c -> {
            appData.setGoals(new ArrayList<>(goals));
            if (appData.getSettings().isAutoSave()) DataManager.saveData();
        });
    }

    private void showMain(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #0c0e1a;");

        // Sidebar
        VBox sidebar = buildSidebar();

        // Content area
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: #0f1120;");

        // Top bar
        HBox topBar = buildTopBar(stage, root);

        BorderPane mainArea = new BorderPane();
        mainArea.setTop(topBar);
        mainArea.setCenter(content);

        root.setLeft(sidebar);
        root.setCenter(mainArea);

        // Wire nav buttons
        Button[] navBtns = (Button[]) sidebar.getUserData();
        navBtns[0].setOnAction(e -> switchPane(content, dashboardPane()));
        navBtns[1].setOnAction(e -> switchPane(content, expensePane()));
        navBtns[2].setOnAction(e -> switchPane(content, assignmentPane()));
        navBtns[3].setOnAction(e -> switchPane(content, eventsPane()));
        navBtns[4].setOnAction(e -> switchPane(content, pomodoroPane()));
        navBtns[5].setOnAction(e -> switchPane(content, calendarPane()));
        navBtns[6].setOnAction(e -> switchPane(content, gradesPane()));
        navBtns[7].setOnAction(e -> switchPane(content, chartsPane()));
        navBtns[8].setOnAction(e -> switchPane(content, attendancePane()));
        navBtns[9].setOnAction(e -> switchPane(content, examsPane()));
        navBtns[10].setOnAction(e -> switchPane(content, notesPane()));
        navBtns[11].setOnAction(e -> switchPane(content, schedulePane()));
        navBtns[12].setOnAction(e -> switchPane(content, goalsPane()));
        navBtns[13].setOnAction(e -> switchPane(content, searchPane()));
        navBtns[14].setOnAction(e -> switchPane(content, themePane()));
        navBtns[15].setOnAction(e -> switchPane(content, notificationsPane()));

        // Default panel
        switchPane(content, dashboardPane());

        Scene scene = new Scene(root, 1200, 750);
        loadCSS(scene);

        root.setOpacity(0);
        stage.setScene(scene);
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private VBox buildSidebar() {
        VBox sb = new VBox(4);
        sb.setPadding(new Insets(24, 14, 24, 14));
        sb.setPrefWidth(240);
        sb.setStyle("-fx-background-color: #141626;");

        // Logo
        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        logoRow.setPadding(new Insets(0, 0, 22, 4));
        Label logoIc = new Label("🎓"); logoIc.setStyle("-fx-font-size: 24px;");
        VBox logoText = new VBox(1);
        Label appName = new Label("Student Survival"); appName.setStyle("-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");
        Label appTag  = new Label("Management"); appTag.setStyle("-fx-text-fill: rgba(99,102,241,0.8); -fx-font-size: 10px;");
        logoText.getChildren().addAll(appName, appTag);
        logoRow.getChildren().addAll(logoIc, logoText);

        // Separator
        Separator sep = new Separator(); sep.setOpacity(0.08);

        Label navLabel = new Label("NAVIGATION");
        navLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.2); -fx-font-size: 9px; -fx-font-weight: bold;");
        navLabel.setPadding(new Insets(12, 0, 4, 8));

        Button dashBtn = navBtn("📊", "Dashboard");
        Button expBtn  = navBtn("💸", "Expenses");
        Button assBtn  = navBtn("📋", "Assignments");
        Button evtBtn  = navBtn("📅", "Events");
        Button pomBtn  = navBtn("⏱", "Pomodoro");
        Button calBtn  = navBtn("🗓", "Calendar");
        Button grdBtn  = navBtn("🎓", "Grades");
        Button chrtBtn = navBtn("📈", "Analytics");
        Button attBtn  = navBtn("📊", "Attendance");
        Button exmBtn  = navBtn("📝", "Exams");
        Button notBtn  = navBtn("📝", "Notes");
        Button schBtn  = navBtn("🗓", "Schedule");
        Button glsBtn  = navBtn("🎯", "Goals");
        Button srcBtn  = navBtn("🔍", "Search");
        Button thmBtn  = navBtn("🎨", "Theme");
        Button ntfBtn  = navBtn("🔔", "Notifications");

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        // User info
        HBox userBox = new HBox(8);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(0, 0, 0, 6));
        Circle avatar = new Circle(16, Color.web("#6366f1"));
        Label userName = new Label(currentUser != null ? currentUser.getUsername() : "Guest");
        userName.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
        userBox.getChildren().addAll(avatar, userName);

        sb.getChildren().addAll(logoRow, sep, navLabel, dashBtn, expBtn, assBtn, evtBtn, pomBtn, calBtn, grdBtn, chrtBtn, attBtn, exmBtn, notBtn, schBtn, glsBtn, srcBtn, thmBtn, ntfBtn, spacer, userBox);
        sb.setUserData(new Button[]{ dashBtn, expBtn, assBtn, evtBtn, pomBtn, calBtn, grdBtn, chrtBtn, attBtn, exmBtn, notBtn, schBtn, glsBtn, srcBtn, thmBtn, ntfBtn });
        return sb;
    }

    private Button navBtn(String icon, String label) {
        Button b = new Button(icon + "   " + label);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setPadding(new Insets(12, 14, 12, 14));
        String off = "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.55); -fx-font-size: 13px; -fx-background-radius: 11; -fx-cursor: hand;";
        String on  = "-fx-background-color: rgba(99,102,241,0.2); -fx-text-fill: white; -fx-font-size: 13px; -fx-background-radius: 11; -fx-cursor: hand; -fx-border-color: rgba(99,102,241,0.3); -fx-border-radius: 11; -fx-border-width: 1;";
        b.setStyle(off);
        b.setOnMouseEntered(e -> b.setStyle(on));
        b.setOnMouseExited(e  -> b.setStyle(off));
        return b;
    }

    private HBox buildTopBar(Stage stage, BorderPane root) {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(13, 24, 13, 24));
        bar.setStyle(
            "-fx-background-color: #141626;" +
            "-fx-border-color: rgba(255,255,255,0.05);" +
            "-fx-border-width: 0 0 1 0;"
        );

        // Date chip
        Label dateLbl = new Label("📅  " + LocalDate.now());
        dateLbl.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.35);" +
            "-fx-font-size: 12px;" +
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 7;" +
            "-fx-padding: 5 10;"
        );

        // Export/Import button
        Button exportBtn = new Button("📤 Export/Import");
        exportBtn.setStyle(
            "-fx-background-color: rgba(99,102,241,0.15);" +
            "-fx-text-fill: #a5b4fc;" +
            "-fx-background-radius: 9;" +
            "-fx-padding: 7 16;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;"
        );
        exportBtn.setOnAction(e -> {
            ExportImportDialog dialog = new ExportImportDialog(stage);
            dialog.create().showAndWait();
        });

        // Logout button
        Button logoutBtn = new Button("⏻  Logout");
        logoutBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.1);" +
            "-fx-text-fill: #f87171;" +
            "-fx-background-radius: 9;" +
            "-fx-padding: 7 16;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;"
        );
        logoutBtn.setOnAction(e -> {
            DataManager.saveData();
            FadeTransition ft = new FadeTransition(Duration.millis(350), root);
            ft.setFromValue(1); ft.setToValue(0);
            ft.setOnFinished(ev -> showLogin(stage));
            ft.play();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(spacer, dateLbl, exportBtn, logoutBtn);
        return bar;
    }

    private void switchPane(StackPane content, Pane pane) {
        pane.setOpacity(0);
        pane.setTranslateY(12);
        content.getChildren().setAll(pane);
        ParallelTransition pt = new ParallelTransition(
            fadeIn(pane, 350),
            translate(pane, 12, 0, 350)
        );
        pt.play();
    }

    private Pane dashboardPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox pane = new VBox(26);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📊  Dashboard");

        // Stat cards
        int   totalExp = expenses.stream().mapToInt(Expense::getValue).sum();
        long  pending  = tasks.stream()
            .filter(t -> !t.isDone() && ChronoUnit.DAYS.between(LocalDate.now(), t.getDate()) >= 0)
            .count();
        long  overdue  = tasks.stream().filter(t -> !t.isDone() && ChronoUnit.DAYS.between(LocalDate.now(), t.getDate()) < 0).count();
        long  done     = tasks.stream().filter(Task::isDone).count();

        HBox cards = new HBox(18);
        cards.getChildren().addAll(
            statCard("💸", "Total Spent",   "₹" + totalExp,        "#6366f1", "#4f46e5"),
            statCard("📚", "Pending Tasks", String.valueOf(pending), "#0891b2", "#0e7490"),
            statCard("⚠️",  "Overdue",       String.valueOf(overdue), "#dc2626", "#b91c1c"),
            statCard("✅", "Completed",     String.valueOf(done),    "#16a34a", "#15803d")
        );

        // Recent expenses
        Label recentTitle = subSectionTitle("💸  Recent Expenses");
        VBox recentBox = new VBox(8);
        List<Expense> recent = expenses.stream()
            .sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
            .limit(5)
            .collect(Collectors.toList());
        if (recent.isEmpty()) {
            recentBox.getChildren().add(emptyHint("No expenses yet — head to the Expenses tab to add some."));
        } else {
            for (Expense ex : recent) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(11, 18, 11, 18));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 11;");
                Label nm  = new Label(ex.getName());  nm.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                Label cat = new Label(ex.getCategory()); cat.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 11px;");
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label amt = new Label("₹" + ex.getValue()); amt.setStyle("-fx-text-fill: #818cf8; -fx-font-weight: bold; -fx-font-size: 14px;");
                row.getChildren().addAll(nm, cat, sp, amt);
                recentBox.getChildren().add(row);
            }
        }

        // Upcoming assignments
        Label upTitle = subSectionTitle("📋  Upcoming Assignments");
        VBox upBox = new VBox(8);
        List<Task> upcoming = tasks.stream()
            .filter(t -> !t.isDone() && ChronoUnit.DAYS.between(LocalDate.now(), t.getDate()) >= 0)
            .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
            .limit(5)
            .collect(Collectors.toList());

        if (upcoming.isEmpty()) {
            upBox.getChildren().add(emptyHint("No pending assignments — you're all caught up! 🎉"));
        } else {
            for (Task t : upcoming) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(11, 18, 11, 18));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 11;");
                Label nm = new Label(t.getName()); nm.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), t.getDate());
                String col = daysLeft <= 1 ? "#f59e0b" : daysLeft <= 3 ? "#fb923c" : "#60a5fa";
                Label sl = new Label(t.getStatus()); sl.setStyle("-fx-text-fill: " + col + "; -fx-font-size: 12px;");
                row.getChildren().addAll(nm, sp, sl);
                upBox.getChildren().add(row);
            }
        }

        pane.getChildren().addAll(title, cards, recentTitle, recentBox, upTitle, upBox);
        scroll.setContent(pane);
        return new StackPane(scroll);
    }

    private Pane expensePane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("💸  Expenses");

        // Add form
        VBox formCard = new VBox(14);
        formCard.setPadding(new Insets(22));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;"
        );
        Label formTitle = new Label("ADD NEW EXPENSE");
        formTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox fields = new HBox(14);
        fields.setAlignment(Pos.BOTTOM_LEFT);

        TextField nameField   = appField("e.g. Lunch, Textbook...", 210);
        TextField amountField = appField("0", 100);

        ComboBox<String> catBox = new ComboBox<>();
        catBox.getItems().addAll("🍔 Food","🚌 Transport","📚 Books","🎮 Entertainment","💊 Health","📦 Other");
        catBox.setValue("🍔 Food");
        catBox.setPrefWidth(160);
        catBox.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 9;");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setPrefWidth(140);
        datePicker.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 9;");

        Button addBtn = new Button("➕  Add Expense");
        addBtn.setStyle(primaryBtnStyle("#6366f1", "#4f46e5"));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(primaryBtnStyle("#4f46e5", "#4338ca")));
        addBtn.setOnMouseExited(e  -> addBtn.setStyle(primaryBtnStyle("#6366f1", "#4f46e5")));

        fields.getChildren().addAll(
            labeledCtrl("Expense Name", nameField),
            labeledCtrl("Amount (₹)",   amountField),
            labeledCtrl("Category",     catBox),
            labeledCtrl("Date",         datePicker),
            addBtn
        );
        HBox.setMargin(addBtn, new Insets(18, 0, 0, 0));
        formCard.getChildren().addAll(formTitle, fields);

        // Table
        TableView<Expense> table = new TableView<>(expenses);
        @SuppressWarnings("deprecation")
        var policy1 = TableView.CONSTRAINED_RESIZE_POLICY;
        table.setColumnResizePolicy(policy1);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Expense, String> nc = new TableColumn<>("Expense Name");
        nc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Expense, String> cc = new TableColumn<>("Category");
        cc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        cc.setMaxWidth(160); cc.setMinWidth(160);

        TableColumn<Expense, String> dc = new TableColumn<>("Date");
        dc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        dc.setMaxWidth(120); dc.setMinWidth(120);

        TableColumn<Expense, Number> vc = new TableColumn<>("Amount (₹)");
        vc.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getValue()));
        vc.setMaxWidth(120); vc.setMinWidth(120);

        TableColumn<Expense, Void> delCol = new TableColumn<>("");
        delCol.setMaxWidth(70); delCol.setMinWidth(70);
        delCol.setCellFactory(col -> new TableCell<>() {
            final Button b = new Button("🗑");
            { b.setStyle(deleteBtnStyle()); b.setOnAction(e -> expenses.remove(getTableRow().getItem())); }
            protected void updateItem(Void x, boolean empty) { super.updateItem(x, empty); setGraphic(empty ? null : b); }
        });
        table.getColumns().addAll(List.of(nc, cc, dc, vc, delCol));

        // Total bar
        HBox totalBar = new HBox();
        totalBar.setAlignment(Pos.CENTER_RIGHT);
        totalBar.setPadding(new Insets(13, 20, 13, 20));
        totalBar.setStyle("-fx-background-color: rgba(99,102,241,0.12); -fx-background-radius: 12;");
        Label totalLbl = new Label("Total: ₹0");
        totalLbl.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 16px; -fx-font-weight: bold;");
        totalBar.getChildren().add(totalLbl);

        Runnable refreshTotal = () -> totalLbl.setText(
            "Total Spent:   ₹" + expenses.stream().mapToInt(Expense::getValue).sum()
        );
        expenses.addListener((ListChangeListener<Expense>) c -> refreshTotal.run());
        refreshTotal.run();

        // Wire add button
        addBtn.setOnAction(e -> {
            try {
                String n = nameField.getText().trim();
                int    v = Integer.parseInt(amountField.getText().trim());
                if (!n.isEmpty()) {
                    expenses.add(new Expense(n, v, catBox.getValue(), datePicker.getValue()));
                    nameField.clear(); amountField.clear();
                    flashTable(table);
                }
            } catch (NumberFormatException ignored) {}
        });

        pane.getChildren().addAll(title, formCard, table, totalBar);
        return pane;
    }

    private Pane assignmentPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📋  Assignments");

        // Form
        VBox formCard = new VBox(14);
        formCard.setPadding(new Insets(22));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;"
        );
        Label formTitle = new Label("ADD NEW ASSIGNMENT");
        formTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox fields = new HBox(14);
        fields.setAlignment(Pos.BOTTOM_LEFT);

        TextField nameField = appField("Assignment title...", 230);

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(7));
        datePicker.setPrefWidth(160);
        datePicker.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 9;");

        ComboBox<String> priBox = new ComboBox<>();
        priBox.getItems().addAll("🔴 High", "🟡 Medium", "🟢 Low");
        priBox.setValue("🟡 Medium");
        priBox.setPrefWidth(150);
        priBox.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 9;");

        TextField subjectField = appField("Subject (optional)", 140);

        Button addBtn = new Button("➕  Add Assignment");
        addBtn.setStyle(primaryBtnStyle("#0284c7", "#0369a1"));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(primaryBtnStyle("#0369a1", "#075985")));
        addBtn.setOnMouseExited(e  -> addBtn.setStyle(primaryBtnStyle("#0284c7", "#0369a1")));

        fields.getChildren().addAll(
            labeledCtrl("Assignment Title", nameField),
            labeledCtrl("Due Date",         datePicker),
            labeledCtrl("Priority",         priBox),
            labeledCtrl("Subject",          subjectField),
            addBtn
        );
        HBox.setMargin(addBtn, new Insets(18, 0, 0, 0));
        formCard.getChildren().addAll(formTitle, fields);

        // Table
        TableView<Task> table = new TableView<>(tasks);
        @SuppressWarnings("deprecation")
        var policy2 = TableView.CONSTRAINED_RESIZE_POLICY;
        table.setColumnResizePolicy(policy2);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Task, String> nc = new TableColumn<>("Assignment");
        nc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Task, String> pc = new TableColumn<>("Priority");
        pc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPriority()));
        pc.setMaxWidth(120); pc.setMinWidth(120);

        TableColumn<Task, String> sc = new TableColumn<>("Status");
        sc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        sc.setMaxWidth(140); sc.setMinWidth(140);
        sc.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                if      (val.contains("Done"))    setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                else if (val.contains("Overdue")) setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
                else if (val.contains("Today"))   setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                else                              setStyle("-fx-text-fill: #60a5fa;");
            }
        });

        TableColumn<Task, Void> doneCol = new TableColumn<>("✔ Done");
        doneCol.setMaxWidth(90); doneCol.setMinWidth(90);
        doneCol.setCellFactory(col -> new TableCell<>() {
            final Button b = new Button("✔");
            {
                b.setStyle("-fx-background-color: rgba(34,197,94,0.18); -fx-text-fill: #4ade80; -fx-background-radius: 7; -fx-cursor: hand;");
                b.setOnAction(e -> {
                    Task t = getTableRow().getItem();
                    if (t != null) { t.setDone(true); table.refresh(); }
                });
            }
            protected void updateItem(Void x, boolean empty) { super.updateItem(x, empty); setGraphic(empty ? null : b); }
        });

        TableColumn<Task, Void> delCol = new TableColumn<>("");
        delCol.setMaxWidth(65); delCol.setMinWidth(65);
        delCol.setCellFactory(col -> new TableCell<>() {
            final Button b = new Button("🗑");
            { b.setStyle(deleteBtnStyle()); b.setOnAction(e -> tasks.remove(getTableRow().getItem())); }
            protected void updateItem(Void x, boolean empty) { super.updateItem(x, empty); setGraphic(empty ? null : b); }
        });

        table.getColumns().addAll(List.of(nc, pc, sc, doneCol, delCol));

        // Wire add button
        addBtn.setOnAction(e -> {
            String    n = nameField.getText().trim();
            LocalDate d = datePicker.getValue();
            String    s = subjectField.getText().trim();
            if (!n.isEmpty() && d != null) {
                tasks.add(new Task(n, d, priBox.getValue(), s));
                nameField.clear(); subjectField.clear();
                flashTable(table);
            }
        });

        pane.getChildren().addAll(title, formCard, table);
        return pane;
    }

    private Pane eventsPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📅  Events");

        VBox formCard = new VBox(14);
        formCard.setPadding(new Insets(22));
        formCard.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-background-radius: 18;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 18;" +
            "-fx-border-width: 1;"
        );
        Label formTitle = new Label("ADD NEW EVENT");
        formTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-size: 10px; -fx-font-weight: bold;");

        HBox fields = new HBox(14);
        fields.setAlignment(Pos.BOTTOM_LEFT);

        TextField titleField = appField("Event name...", 220);

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setPrefWidth(160);
        datePicker.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 9;");

        TextField placeField = appField("Location", 180);

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("📚 Study", "🎉 Social", "🏃 Sports", "💼 Work", "📝 Exam", "🎯 Other");
        typeBox.setValue("📚 Study");
        typeBox.setPrefWidth(140);
        typeBox.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 9;");

        Button addBtn = new Button("➕  Add Event");
        addBtn.setStyle(primaryBtnStyle("#0ea5e9", "#0284c7"));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(primaryBtnStyle("#0284c7", "#0369a1")));
        addBtn.setOnMouseExited(e  -> addBtn.setStyle(primaryBtnStyle("#0ea5e9", "#0284c7")));

        fields.getChildren().addAll(
            labeledCtrl("Event Title", titleField),
            labeledCtrl("Event Date",  datePicker),
            labeledCtrl("Location",    placeField),
            labeledCtrl("Type",        typeBox),
            addBtn
        );
        HBox.setMargin(addBtn, new Insets(18, 0, 0, 0));
        formCard.getChildren().addAll(formTitle, fields);

        TableView<EventItem> table = new TableView<>(events);
        @SuppressWarnings("deprecation")
        var policy3 = TableView.CONSTRAINED_RESIZE_POLICY;
        table.setColumnResizePolicy(policy3);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<EventItem, String> tc = new TableColumn<>("Event");
        tc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<EventItem, String> dc = new TableColumn<>("Date");
        dc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        dc.setMaxWidth(140); dc.setMinWidth(140);

        TableColumn<EventItem, String> lc = new TableColumn<>("Location");
        lc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        lc.setMaxWidth(180); lc.setMinWidth(180);

        TableColumn<EventItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));
        typeCol.setMaxWidth(120); typeCol.setMinWidth(120);

        TableColumn<EventItem, String> sc = new TableColumn<>("Status");
        sc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        sc.setMaxWidth(140); sc.setMinWidth(140);
        sc.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                if      (val.contains("Completed")) setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                else if (val.contains("Today"))     setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                else if (val.contains("Overdue"))   setStyle("-fx-text-fill: #f87171; -fx-font-weight: bold;");
                else                                 setStyle("-fx-text-fill: #60a5fa;");
            }
        });

        TableColumn<EventItem, Void> doneCol = new TableColumn<>("✔ Done");
        doneCol.setMaxWidth(90); doneCol.setMinWidth(90);
        doneCol.setCellFactory(col -> new TableCell<>() {
            final Button b = new Button("✔");
            {
                b.setStyle("-fx-background-color: rgba(34,197,94,0.18); -fx-text-fill: #4ade80; -fx-background-radius: 7; -fx-cursor: hand;");
                b.setOnAction(e -> {
                    EventItem item = getTableRow().getItem();
                    if (item != null) { item.setDone(true); table.refresh(); }
                });
            }
            protected void updateItem(Void x, boolean empty) { super.updateItem(x, empty); setGraphic(empty ? null : b); }
        });

        TableColumn<EventItem, Void> delCol = new TableColumn<>("");
        delCol.setMaxWidth(65); delCol.setMinWidth(65);
        delCol.setCellFactory(col -> new TableCell<>() {
            final Button b = new Button("🗑");
            { b.setStyle(deleteBtnStyle()); b.setOnAction(e -> events.remove(getTableRow().getItem())); }
            protected void updateItem(Void x, boolean empty) { super.updateItem(x, empty); setGraphic(empty ? null : b); }
        });

        table.getColumns().addAll(tc, dc, lc, typeCol, sc, doneCol, delCol);

        addBtn.setOnAction(e -> {
            String titleTxt = titleField.getText().trim();
            LocalDate date = datePicker.getValue();
            String location = placeField.getText().trim();
            String type = typeBox.getValue();
            if (!titleTxt.isEmpty() && date != null) {
                events.add(new EventItem(titleTxt, date, location, type));
                titleField.clear();
                placeField.clear();
                flashTable(table);
            }
        });

        pane.getChildren().addAll(title, formCard, table);
        return pane;
    }

    private Pane pomodoroPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("⏱  Pomodoro Timer");

        PomodoroTimer timer = new PomodoroTimer();
        timer.setOnSessionComplete(() -> {
            // Could add notification here
        });

        pane.getChildren().addAll(title, timer.create());
        return pane;
    }

    private Pane calendarPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("🗓  Calendar");

        CalendarView calendar = new CalendarView(tasks, events);

        pane.getChildren().addAll(title, calendar.create());
        return pane;
    }

    private Pane gradesPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("🎓  Grade Tracker");

        GradeTracker gradeTracker = new GradeTracker(courses);

        pane.getChildren().addAll(title, gradeTracker.create());
        return pane;
    }

    private Pane chartsPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📈  Analytics");

        ChartsView charts = new ChartsView(expenses, tasks);

        pane.getChildren().addAll(title, charts.create());
        return pane;
    }

    private Pane attendancePane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📊  Attendance");

        AttendanceTracker attendanceTracker = new AttendanceTracker(attendance);

        pane.getChildren().addAll(title, attendanceTracker.create());
        return pane;
    }

    private Pane examsPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📝  Exams");

        ExamTracker examTracker = new ExamTracker(exams);

        pane.getChildren().addAll(title, examTracker.create());
        return pane;
    }

    private Pane notesPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📝  Notes");

        NotesModule notesModule = new NotesModule(notes);

        pane.getChildren().addAll(title, notesModule.create());
        return pane;
    }

    private Pane schedulePane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("🗓  Study Schedule");

        StudySchedule studySchedule = new StudySchedule(schedule);

        pane.getChildren().addAll(title, studySchedule.create());
        return pane;
    }

    private Pane goalsPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("🎯  Goals");

        GoalSetting goalSetting = new GoalSetting(goals);

        pane.getChildren().addAll(title, goalSetting.create());
        return pane;
    }

    private Pane searchPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("🔍  Global Search");

        GlobalSearch globalSearch = new GlobalSearch(expenses, tasks, events, notes, courses, attendance, exams, goals);

        pane.getChildren().addAll(title, globalSearch.create());
        return pane;
    }

    private Pane themePane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("🎨  Theme Settings");

        ThemeSwitcher themeSwitcher = new ThemeSwitcher();

        pane.getChildren().addAll(title, themeSwitcher.create());
        return pane;
    }

    private Pane notificationsPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("🔔  Notifications");

        NotificationSystem notificationSystem = new NotificationSystem(tasks, events);

        pane.getChildren().addAll(title, notificationSystem.create());
        return pane;
    }

    // UI Helpers
    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        return l;
    }

    private Label subSectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 15px; -fx-font-weight: bold;");
        return l;
    }

    private Label emptyHint(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.2); -fx-font-size: 13px; -fx-padding: 8 0 0 0;");
        return l;
    }

    private TextField appField(String prompt, int width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        tf.setStyle(
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.28);" +
            "-fx-background-radius: 9;" +
            "-fx-padding: 9 13;" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: rgba(255,255,255,0.06);" +
            "-fx-border-radius: 9;"
        );
        return tf;
    }

    private VBox labeledCtrl(String label, Control ctrl) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.38); -fx-font-size: 10px;");
        return new VBox(5, l, ctrl);
    }

    private String primaryBtnStyle(String c1, String c2) {
        return "-fx-background-color: linear-gradient(to right, " + c1 + ", " + c2 + ");" +
               "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
               "-fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;";
    }

    private String deleteBtnStyle() {
        return "-fx-background-color: rgba(239,68,68,0.18); -fx-text-fill: #f87171; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;";
    }

    private VBox statCard(String icon, String label, String value, String c1, String c2) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(22, 24, 22, 24));
        card.setPrefWidth(190);
        card.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, " + c1 + ", " + c2 + ");" +
            "-fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 14, 0, 0, 6);"
        );
        Label ic  = new Label(icon);  ic.setStyle("-fx-font-size: 28px;");
        Label lbl = new Label(label); lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.65); -fx-font-size: 12px;");
        Label val = new Label(value); val.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold;");
        card.getChildren().addAll(ic, lbl, val);

        ScaleTransition sc = new ScaleTransition(Duration.millis(160), card);
        card.setOnMouseEntered(e -> { sc.stop(); sc.setToX(1.05); sc.setToY(1.05); sc.play(); });
        card.setOnMouseExited(e  -> { sc.stop(); sc.setToX(1.00); sc.setToY(1.00); sc.play(); });
        return card;
    }

    private FadeTransition fadeIn(Node node, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        return ft;
    }

    private TranslateTransition translate(Node node, double fromY, double toY, int ms) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), node);
        tt.setFromY(fromY); tt.setToY(toY);
        tt.setInterpolator(Interpolator.EASE_OUT);
        return tt;
    }

    private void flashTable(TableView<?> table) {
        FadeTransition ft = new FadeTransition(Duration.millis(250), table);
        ft.setFromValue(0.55); ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    private void loadCSS(Scene scene) {
        try {
            java.net.URL css = getClass().getResource("/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}
