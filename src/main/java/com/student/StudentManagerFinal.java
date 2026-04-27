package com.student;

import javafx.animation.*;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.collections.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class StudentManagerFinal extends Application {

    // ── Shared data ──────────────────────────────────────────────────────────
    final ObservableList<Expense> expenses = FXCollections.observableArrayList();
    final ObservableList<Task>    tasks    = FXCollections.observableArrayList();
    final ObservableList<EventItem> events = FXCollections.observableArrayList();

    // Credentials (change as needed)
    private static final String VALID_USER = "admin";
    private static final String VALID_PASS = "1234";

    // ═════════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        stage.setTitle("StudyHub Pro");
        stage.setMinWidth(860);
        stage.setMinHeight(560);
        showLogin(stage);
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  LOGIN SCREEN
    // ═════════════════════════════════════════════════════════════════════════
    void showLogin(Stage stage) {

        // ── Root with gradient background ────────────────────────────────────
        StackPane root = new StackPane();
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #090c14, #101526, #0d1f3c);"
        );

        // ── Floating orb decorations ─────────────────────────────────────────
        String[] orbColors = { "#3b82f6", "#8b5cf6", "#06b6d4", "#ec4899", "#10b981", "#f59e0b" };
        double[] orbSizes  = { 200, 150, 180, 120, 160, 100 };
        double[] orbX      = { -260, 260, -180, 220, -80,  150 };
        double[] orbY      = { -200, -180, 180, 200, -80, -120 };

        for (int i = 0; i < orbColors.length; i++) {
            Circle orb = new Circle(orbSizes[i]);
            orb.setFill(Color.web(orbColors[i], 0.06));
            orb.setTranslateX(orbX[i]);
            orb.setTranslateY(orbY[i]);
            root.getChildren().add(orb);

            // Gentle pulsating animation
            ScaleTransition st = new ScaleTransition(Duration.seconds(3 + i * 0.5), orb);
            st.setFromX(1.0); st.setToX(1.4);
            st.setFromY(1.0); st.setToY(1.4);
            st.setCycleCount(Animation.INDEFINITE);
            st.setAutoReverse(true);
            st.setInterpolator(Interpolator.EASE_BOTH);
            st.play();

            // Slow drift
            TranslateTransition drift = new TranslateTransition(Duration.seconds(6 + i), orb);
            drift.setByX(20 * (i % 2 == 0 ? 1 : -1));
            drift.setByY(15 * (i % 2 == 0 ? -1 : 1));
            drift.setCycleCount(Animation.INDEFINITE);
            drift.setAutoReverse(true);
            drift.play();
        }

        // ── Glass card ───────────────────────────────────────────────────────
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 55, 50, 55));
        card.setMaxWidth(430);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 28;" +
            "-fx-border-color: rgba(255,255,255,0.1);" +
            "-fx-border-radius: 28;" +
            "-fx-border-width: 1;"
        );
        DropShadow glow = new DropShadow(60, Color.web("#3b82f6", 0.25));
        glow.setOffsetY(8);
        card.setEffect(glow);

        // Logo + title
        Label logoEmoji = new Label("🎓");
        logoEmoji.setStyle("-fx-font-size: 56px;");

        Label appTitle = new Label("StudyHub Pro");
        appTitle.setStyle(
            "-fx-text-fill: white;" +
            "-fx-font-size: 28px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI', sans-serif;"
        );

        Label subTitle = new Label("Your personal academic dashboard");
        subTitle.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.38);" +
            "-fx-font-size: 13px;"
        );

        // Divider
        Separator divider = new Separator();
        divider.setOpacity(0.12);

        // Fields
        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setMaxWidth(Double.MAX_VALUE);
        userField.setStyle(loginFieldStyle());

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setMaxWidth(Double.MAX_VALUE);
        passField.setStyle(loginFieldStyle());

        // Error label (hidden by default)
        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorLbl.setVisible(false);

        // Login button
        Button loginBtn = new Button("SIGN IN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(loginBtnStyle(false));
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(loginBtnStyle(true)));
        loginBtn.setOnMouseExited(e  -> loginBtn.setStyle(loginBtnStyle(false)));

        // Hint
        Label hintLbl = new Label("Default credentials: admin / 1234");
        hintLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.18); -fx-font-size: 11px;");

        card.getChildren().addAll(
            logoEmoji, appTitle, subTitle, divider,
            userField, passField, errorLbl, loginBtn, hintLbl
        );
        root.getChildren().add(card);

        // ── Entrance animation ───────────────────────────────────────────────
        card.setOpacity(0);
        card.setTranslateY(50);
        card.setScaleX(0.92);
        card.setScaleY(0.92);

        ParallelTransition entrance = new ParallelTransition(
            fadeIn(card, 800),
            translate(card, 50, 0, 800),
            scale(card, 0.92, 1.0, 800)
        );
        entrance.play();

        // ── Login logic ──────────────────────────────────────────────────────
        Runnable doLogin = () -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            if (u.equals(VALID_USER) && p.equals(VALID_PASS)) {
                errorLbl.setVisible(false);
                FadeTransition out = new FadeTransition(Duration.millis(400), root);
                out.setFromValue(1); out.setToValue(0);
                out.setOnFinished(ev -> showMain(stage));
                out.play();
            } else {
                errorLbl.setText("❌  Invalid username or password");
                errorLbl.setVisible(true);
                shakeNode(card);
                // Brief red border flash on fields
                String errStyle = loginFieldStyle() +
                    "-fx-border-color: rgba(248,113,113,0.6); -fx-border-width: 1; -fx-border-radius: 11;";
                userField.setStyle(errStyle);
                passField.setStyle(errStyle);
                PauseTransition pause = new PauseTransition(Duration.millis(1500));
                pause.setOnFinished(e -> {
                    userField.setStyle(loginFieldStyle());
                    passField.setStyle(loginFieldStyle());
                });
                pause.play();
            }
        };

        loginBtn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());

        Scene scene = new Scene(root, 960, 640);
        loadCSS(scene);
        stage.setScene(scene);
        stage.show();
    }

    String loginFieldStyle() {
        return
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.32);" +
            "-fx-background-radius: 11;" +
            "-fx-padding: 13 16;" +
            "-fx-font-size: 14px;";
    }

    String loginBtnStyle(boolean hover) {
        String base =
            "-fx-text-fill: white;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 13 0;" +
            "-fx-cursor: hand;";
        if (hover) {
            return base +
                "-fx-background-color: linear-gradient(to right, #2563eb, #7c3aed);" +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.55), 20, 0, 0, 4);";
        }
        return base + "-fx-background-color: linear-gradient(to right, #3b82f6, #8b5cf6);";
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  MAIN WINDOW
    // ═════════════════════════════════════════════════════════════════════════
    void showMain(Stage stage) {

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

        // Default panel
        switchPane(content, dashboardPane());

        Scene scene = new Scene(root, 1080, 680);
        loadCSS(scene);

        root.setOpacity(0);
        stage.setScene(scene);
        stage.show();

        FadeTransition ft = new FadeTransition(Duration.millis(500), root);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────
    VBox buildSidebar() {
        VBox sb = new VBox(4);
        sb.setPadding(new Insets(24, 14, 24, 14));
        sb.setPrefWidth(220);
        sb.setStyle("-fx-background-color: #141626;");

        // Logo
        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        logoRow.setPadding(new Insets(0, 0, 22, 4));
        Label logoIc = new Label("🎓"); logoIc.setStyle("-fx-font-size: 24px;");
        VBox logoText = new VBox(1);
        Label appName = new Label("StudyHub"); appName.setStyle("-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");
        Label appTag  = new Label("Pro Edition"); appTag.setStyle("-fx-text-fill: rgba(99,102,241,0.8); -fx-font-size: 10px;");
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

        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);

        // Version badge
        HBox versionBox = new HBox(6);
        versionBox.setAlignment(Pos.CENTER_LEFT);
        versionBox.setPadding(new Insets(0, 0, 0, 6));
        Circle dot = new Circle(4, Color.web("#22c55e"));
        Label ver = new Label("v2.0 · Running");
        ver.setStyle("-fx-text-fill: rgba(255,255,255,0.18); -fx-font-size: 10px;");
        versionBox.getChildren().addAll(dot, ver);

        // Pulsing green dot
        ScaleTransition pulse = new ScaleTransition(Duration.millis(900), dot);
        pulse.setFromX(1); pulse.setToX(1.6);
        pulse.setFromY(1); pulse.setToY(1.6);
        pulse.setCycleCount(Animation.INDEFINITE); pulse.setAutoReverse(true); pulse.play();

        sb.getChildren().addAll(logoRow, sep, navLabel, dashBtn, expBtn, assBtn, evtBtn, spacer, versionBox);
        sb.setUserData(new Button[]{ dashBtn, expBtn, assBtn, evtBtn });
        return sb;
    }

    Button navBtn(String icon, String label) {
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

    // ── Top bar ───────────────────────────────────────────────────────────────
    HBox buildTopBar(Stage stage, BorderPane root) {
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

        // User chip
        Label userLbl = new Label("👤  " + VALID_USER);
        userLbl.setStyle(
            "-fx-text-fill: rgba(255,255,255,0.55);" +
            "-fx-font-size: 12px;" +
            "-fx-background-color: rgba(99,102,241,0.12);" +
            "-fx-background-radius: 7;" +
            "-fx-padding: 5 10;"
        );

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
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.22);" +
            "-fx-text-fill: #fca5a5;" +
            "-fx-background-radius: 9;" +
            "-fx-padding: 7 16;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;"
        ));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.1);" +
            "-fx-text-fill: #f87171;" +
            "-fx-background-radius: 9;" +
            "-fx-padding: 7 16;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;"
        ));
        logoutBtn.setOnAction(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(350), root);
            ft.setFromValue(1); ft.setToValue(0);
            ft.setOnFinished(ev -> showLogin(stage));
            ft.play();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.getChildren().addAll(spacer, dateLbl, userLbl, logoutBtn);
        return bar;
    }

    // ── Panel switcher ───────────────────────────────────────────────────────
    void switchPane(StackPane content, Pane pane) {
        pane.setOpacity(0);
        pane.setTranslateY(12);
        content.getChildren().setAll(pane);
        ParallelTransition pt = new ParallelTransition(
            fadeIn(pane, 350),
            translate(pane, 12, 0, 350)
        );
        pt.play();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DASHBOARD
    // ═════════════════════════════════════════════════════════════════════════
    Pane dashboardPane() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox pane = new VBox(26);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📊  Dashboard");

        // ── Stat cards ────────────────────────────────────────────────────────
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

        // ── Recent expenses ───────────────────────────────────────────────────
        Label recentTitle = subSectionTitle("💸  Recent Expenses");

        VBox recentBox = new VBox(8);
        List<Expense> recent = expenses.subList(Math.max(0, expenses.size() - 5), expenses.size());
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

        // ── Upcoming assignments ─────────────────────────────────────────────
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

    VBox statCard(String icon, String label, String value, String c1, String c2) {
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

    // ═════════════════════════════════════════════════════════════════════════
    //  EXPENSES
    // ═════════════════════════════════════════════════════════════════════════
    Pane expensePane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("💸  Expenses");

        // ── Add form ─────────────────────────────────────────────────────────
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

        Button addBtn = new Button("➕  Add Expense");
        addBtn.setStyle(primaryBtnStyle("#6366f1", "#4f46e5"));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(primaryBtnStyle("#4f46e5", "#4338ca")));
        addBtn.setOnMouseExited(e  -> addBtn.setStyle(primaryBtnStyle("#6366f1", "#4f46e5")));

        fields.getChildren().addAll(
            labeledCtrl("Expense Name", nameField),
            labeledCtrl("Amount (₹)",   amountField),
            labeledCtrl("Category",     catBox),
            addBtn
        );
        HBox.setMargin(addBtn, new Insets(18, 0, 0, 0));
        formCard.getChildren().addAll(formTitle, fields);

        // ── Table ────────────────────────────────────────────────────────────
        TableView<Expense> table = new TableView<>(expenses);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Expense, String> nc = new TableColumn<>("Expense Name");
        nc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

        TableColumn<Expense, String> cc = new TableColumn<>("Category");
        cc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        cc.setMaxWidth(160); cc.setMinWidth(160);

        TableColumn<Expense, Number> vc = new TableColumn<>("Amount (₹)");
        vc.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getValue()));
        vc.setMaxWidth(120); vc.setMinWidth(120);

        TableColumn<Expense, Void> dc = new TableColumn<>("");
        dc.setMaxWidth(70); dc.setMinWidth(70);
        dc.setCellFactory(col -> new TableCell<>() {
            final Button b = new Button("🗑");
            { b.setStyle(deleteBtnStyle()); b.setOnAction(e -> expenses.remove(getTableRow().getItem())); }
            protected void updateItem(Void x, boolean empty) { super.updateItem(x, empty); setGraphic(empty ? null : b); }
        });
        table.getColumns().addAll(nc, cc, vc, dc);

        // ── Total bar ────────────────────────────────────────────────────────
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
                    expenses.add(new Expense(n, v, catBox.getValue()));
                    nameField.clear(); amountField.clear();
                    flashTable(table);
                }
            } catch (NumberFormatException ignored) {}
        });

        pane.getChildren().addAll(title, formCard, table, totalBar);
        return pane;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ASSIGNMENTS
    // ═════════════════════════════════════════════════════════════════════════
    Pane assignmentPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(34));

        Label title = sectionTitle("📋  Assignments");

        // ── Form ─────────────────────────────────────────────────────────────
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

        Button addBtn = new Button("➕  Add Assignment");
        addBtn.setStyle(primaryBtnStyle("#0284c7", "#0369a1"));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(primaryBtnStyle("#0369a1", "#075985")));
        addBtn.setOnMouseExited(e  -> addBtn.setStyle(primaryBtnStyle("#0284c7", "#0369a1")));

        fields.getChildren().addAll(
            labeledCtrl("Assignment Title", nameField),
            labeledCtrl("Due Date",         datePicker),
            labeledCtrl("Priority",         priBox),
            addBtn
        );
        HBox.setMargin(addBtn, new Insets(18, 0, 0, 0));
        formCard.getChildren().addAll(formTitle, fields);

        // ── Table ────────────────────────────────────────────────────────────
        TableView<Task> table = new TableView<>(tasks);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
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

        table.getColumns().addAll(nc, pc, sc, doneCol, delCol);

        // Wire add button
        addBtn.setOnAction(e -> {
            String    n = nameField.getText().trim();
            LocalDate d = datePicker.getValue();
            if (!n.isEmpty() && d != null) {
                tasks.add(new Task(n, d, priBox.getValue()));
                nameField.clear();
                flashTable(table);
            }
        });

        pane.getChildren().addAll(title, formCard, table);
        return pane;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  EVENTS
    // ═════════════════════════════════════════════════════════════════════════
    Pane eventsPane() {
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

        Button addBtn = new Button("➕  Add Event");
        addBtn.setStyle(primaryBtnStyle("#0ea5e9", "#0284c7"));
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(primaryBtnStyle("#0284c7", "#0369a1")));
        addBtn.setOnMouseExited(e  -> addBtn.setStyle(primaryBtnStyle("#0ea5e9", "#0284c7")));

        fields.getChildren().addAll(
            labeledCtrl("Event Title", titleField),
            labeledCtrl("Event Date",  datePicker),
            labeledCtrl("Location",    placeField),
            addBtn
        );
        HBox.setMargin(addBtn, new Insets(18, 0, 0, 0));
        formCard.getChildren().addAll(formTitle, fields);

        TableView<EventItem> table = new TableView<>(events);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<EventItem, String> tc = new TableColumn<>("Event");
        tc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitle()));

        TableColumn<EventItem, String> dc = new TableColumn<>("Date");
        dc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDate().toString()));
        dc.setMaxWidth(140); dc.setMinWidth(140);

        TableColumn<EventItem, String> lc = new TableColumn<>("Location");
        lc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLocation()));
        lc.setMaxWidth(180); lc.setMinWidth(180);

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

        table.getColumns().addAll(tc, dc, lc, sc, doneCol, delCol);

        addBtn.setOnAction(e -> {
            String titleTxt = titleField.getText().trim();
            LocalDate date = datePicker.getValue();
            String location = placeField.getText().trim();
            if (!titleTxt.isEmpty() && date != null && !location.isEmpty()) {
                events.add(new EventItem(titleTxt, date, location));
                titleField.clear();
                placeField.clear();
                flashTable(table);
            }
        });

        pane.getChildren().addAll(title, formCard, table);
        return pane;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS – UI
    // ═════════════════════════════════════════════════════════════════════════

    Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        return l;
    }

    Label subSectionTitle(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 15px; -fx-font-weight: bold;");
        return l;
    }

    Label emptyHint(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.2); -fx-font-size: 13px; -fx-padding: 8 0 0 0;");
        return l;
    }

    TextField appField(String prompt, int width) {
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

    VBox labeledCtrl(String label, Control ctrl) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: rgba(255,255,255,0.38); -fx-font-size: 10px;");
        return new VBox(5, l, ctrl);
    }

    String primaryBtnStyle(String c1, String c2) {
        return "-fx-background-color: linear-gradient(to right, " + c1 + ", " + c2 + ");" +
               "-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;" +
               "-fx-background-radius: 10; -fx-padding: 10 20; -fx-cursor: hand;";
    }

    String deleteBtnStyle() {
        return "-fx-background-color: rgba(239,68,68,0.18); -fx-text-fill: #f87171; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;";
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS – ANIMATION
    // ═════════════════════════════════════════════════════════════════════════

    FadeTransition fadeIn(Node node, int ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), node);
        ft.setFromValue(0); ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        return ft;
    }

    TranslateTransition translate(Node node, double fromY, double toY, int ms) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), node);
        tt.setFromY(fromY); tt.setToY(toY);
        tt.setInterpolator(Interpolator.EASE_OUT);
        return tt;
    }

    ScaleTransition scale(Node node, double from, double to, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), node);
        st.setFromX(from); st.setToX(to);
        st.setFromY(from); st.setToY(to);
        st.setInterpolator(Interpolator.EASE_OUT);
        return st;
    }

    void shakeNode(Node node) {
        Timeline shake = new Timeline(
            new KeyFrame(Duration.millis(0),   new KeyValue(node.translateXProperty(),  0,  Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(55),  new KeyValue(node.translateXProperty(), -14, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(110), new KeyValue(node.translateXProperty(),  14, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(165), new KeyValue(node.translateXProperty(), -10, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(220), new KeyValue(node.translateXProperty(),  10, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(275), new KeyValue(node.translateXProperty(), -6,  Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.millis(330), new KeyValue(node.translateXProperty(),  0,  Interpolator.EASE_BOTH))
        );
        shake.play();
    }

    void flashTable(TableView<?> table) {
        FadeTransition ft = new FadeTransition(Duration.millis(250), table);
        ft.setFromValue(0.55); ft.setToValue(1.0);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    void loadCSS(Scene scene) {
        try {
            URL css = getClass().getResource("/style.css");
            if (css != null) scene.getStylesheets().add(css.toExternalForm());
        } catch (Exception ignored) {}
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  DATA MODELS
    // ═════════════════════════════════════════════════════════════════════════

    static class Expense {
        private final String name, category;
        private final int value;

        Expense(String n, int v, String cat) { name = n; value = v; category = cat; }
        String getName()     { return name; }
        int    getValue()    { return value; }
        String getCategory() { return category; }
    }

    static class Task {
        private final String   name, priority;
        private final LocalDate date;
        private boolean done = false;

        Task(String n, LocalDate d, String p) { name = n; date = d; priority = p; }
        String    getName()     { return name; }
        LocalDate getDate()     { return date; }
        String    getPriority() { return priority; }
        boolean   isDone()      { return done; }
        void      setDone(boolean b) { done = b; }

        String getStatus() {
            if (done) return "Done ✅";
            long d = ChronoUnit.DAYS.between(LocalDate.now(), date);
            if (d < 0)  return "Overdue ❌";
            if (d == 0) return "Due Today ⚡";
            return d + " day" + (d == 1 ? "" : "s") + " left";
        }
    }

    static class EventItem {
        private final String title;
        private final LocalDate date;
        private final String location;
        private boolean done = false;

        EventItem(String title, LocalDate date, String location) {
            this.title = title;
            this.date = date;
            this.location = location;
        }

        String getTitle() { return title; }
        LocalDate getDate() { return date; }
        String getLocation() { return location; }
        void setDone(boolean done) { this.done = done; }

        String getStatus() {
            if (done) return "Completed ✅";
            long days = ChronoUnit.DAYS.between(LocalDate.now(), date);
            if (days < 0) return "Overdue ❌";
            if (days == 0) return "Today ⚡";
            return "In " + days + " day" + (days == 1 ? "" : "s");
        }
    }
}
