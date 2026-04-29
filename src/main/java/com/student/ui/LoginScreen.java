package com.student.ui;

import com.student.data.DataManager;
import com.student.model.User;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.function.Consumer;

public class LoginScreen {

    private Stage stage;
    private Consumer<User> onLoginSuccess;

    public LoginScreen(Stage stage, Consumer<User> onLoginSuccess) {
        this.stage = stage;
        this.onLoginSuccess = onLoginSuccess;
    }

    public Pane create() {
        StackPane root = new StackPane();
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #090c14, #101526, #0d1f3c);"
        );

        // Floating orb decorations
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

            ScaleTransition st = new ScaleTransition(Duration.seconds(3 + i * 0.5), orb);
            st.setFromX(1.0); st.setToX(1.4);
            st.setFromY(1.0); st.setToY(1.4);
            st.setCycleCount(Animation.INDEFINITE);
            st.setAutoReverse(true);
            st.setInterpolator(Interpolator.EASE_BOTH);
            st.play();

            TranslateTransition drift = new TranslateTransition(Duration.seconds(6 + i), orb);
            drift.setByX(20 * (i % 2 == 0 ? 1 : -1));
            drift.setByY(15 * (i % 2 == 0 ? -1 : 1));
            drift.setCycleCount(Animation.INDEFINITE);
            drift.setAutoReverse(true);
            drift.play();
        }

        // Main card
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

        Label appTitle = new Label("Student Survival Management");
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

        // Tab buttons
        HBox tabBox = new HBox(8);
        tabBox.setAlignment(Pos.CENTER);

        Button loginTab = new Button("Login");
        Button registerTab = new Button("Register");

        String activeTabStyle = "-fx-background-color: rgba(99,102,241,0.3); -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px; -fx-font-weight: bold;";
        String inactiveTabStyle = "-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.4); -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 13px;";

        loginTab.setStyle(activeTabStyle);
        loginTab.setPrefWidth(100);
        registerTab.setStyle(inactiveTabStyle);
        registerTab.setPrefWidth(100);

        tabBox.getChildren().addAll(loginTab, registerTab);

        // Content area
        StackPane contentArea = new StackPane();
        contentArea.setPrefHeight(220);

        // Login form
        VBox loginForm = createLoginForm();
        // Register form
        VBox registerForm = createRegisterForm();

        contentArea.getChildren().addAll(loginForm, registerForm);
        registerForm.setVisible(false);

        // Tab switching
        loginTab.setOnAction(e -> {
            loginTab.setStyle(activeTabStyle);
            registerTab.setStyle(inactiveTabStyle);
            loginForm.setVisible(true);
            registerForm.setVisible(false);
        });

        registerTab.setOnAction(e -> {
            registerTab.setStyle(activeTabStyle);
            loginTab.setStyle(inactiveTabStyle);
            registerForm.setVisible(true);
            loginForm.setVisible(false);
        });

        card.getChildren().addAll(logoEmoji, appTitle, subTitle, tabBox, contentArea);
        root.getChildren().add(card);

        // Entrance animation
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

        return root;
    }

    private VBox createLoginForm() {
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setMaxWidth(Double.MAX_VALUE);
        userField.setStyle(fieldStyle());

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setMaxWidth(Double.MAX_VALUE);
        passField.setStyle(fieldStyle());

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorLbl.setVisible(false);

        Button loginBtn = new Button("SIGN IN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(buttonStyle(false));
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(buttonStyle(true)));
        loginBtn.setOnMouseExited(e  -> loginBtn.setStyle(buttonStyle(false)));

        loginBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String password = passField.getText();

            User user = DataManager.findUser(username);
            if (user != null && DataManager.verifyPassword(password, user.getPasswordHash())) {
                DataManager.getAppData().setCurrentUser(username);
                onLoginSuccess.accept(user);
            } else {
                errorLbl.setText("❌  Invalid username or password");
                errorLbl.setVisible(true);
                shakeNode(form);
            }
        });

        passField.setOnAction(e -> loginBtn.getOnAction().handle(null));

        form.getChildren().addAll(userField, passField, errorLbl, loginBtn);
        return form;
    }

    private VBox createRegisterForm() {
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER);

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setMaxWidth(Double.MAX_VALUE);
        userField.setStyle(fieldStyle());

        TextField emailField = new TextField();
        emailField.setPromptText("Email (optional)");
        emailField.setMaxWidth(Double.MAX_VALUE);
        emailField.setStyle(fieldStyle());

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name (optional)");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle(fieldStyle());

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setMaxWidth(Double.MAX_VALUE);
        passField.setStyle(fieldStyle());

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm Password");
        confirmField.setMaxWidth(Double.MAX_VALUE);
        confirmField.setStyle(fieldStyle());

        Label errorLbl = new Label();
        errorLbl.setStyle("-fx-text-fill: #f87171; -fx-font-size: 11px;");
        errorLbl.setVisible(false);
        errorLbl.setWrapText(true);

        Button registerBtn = new Button("CREATE ACCOUNT");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle(buttonStyle(false));
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(buttonStyle(true)));
        registerBtn.setOnMouseExited(e  -> registerBtn.setStyle(buttonStyle(false)));

        registerBtn.setOnAction(e -> {
            String username = userField.getText().trim();
            String email = emailField.getText().trim();
            String fullName = nameField.getText().trim();
            String password = passField.getText();
            String confirm = confirmField.getText();

            errorLbl.setVisible(false);

            if (username.isEmpty()) {
                errorLbl.setText("❌  Username is required");
                errorLbl.setVisible(true);
                shakeNode(form);
                return;
            }

            if (password.isEmpty()) {
                errorLbl.setText("❌  Password is required");
                errorLbl.setVisible(true);
                shakeNode(form);
                return;
            }

            if (!password.equals(confirm)) {
                errorLbl.setText("❌  Passwords do not match");
                errorLbl.setVisible(true);
                shakeNode(form);
                return;
            }

            if (password.length() < 4) {
                errorLbl.setText("❌  Password must be at least 4 characters");
                errorLbl.setVisible(true);
                shakeNode(form);
                return;
            }

            if (DataManager.addUser(username, password, email, fullName)) {
                DataManager.getAppData().setCurrentUser(username);
                onLoginSuccess.accept(DataManager.findUser(username));
            } else {
                errorLbl.setText("❌  Username already exists");
                errorLbl.setVisible(true);
                shakeNode(form);
            }
        });

        form.getChildren().addAll(userField, emailField, nameField, passField, confirmField, errorLbl, registerBtn);
        return form;
    }

    private String fieldStyle() {
        return
            "-fx-background-color: rgba(255,255,255,0.08);" +
            "-fx-text-fill: white;" +
            "-fx-prompt-text-fill: rgba(255,255,255,0.32);" +
            "-fx-background-radius: 11;" +
            "-fx-padding: 13 16;" +
            "-fx-font-size: 14px;";
    }

    private String buttonStyle(boolean hover) {
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

    private ScaleTransition scale(Node node, double from, double to, int ms) {
        ScaleTransition st = new ScaleTransition(Duration.millis(ms), node);
        st.setFromX(from); st.setToX(to);
        st.setFromY(from); st.setToY(to);
        st.setInterpolator(Interpolator.EASE_OUT);
        return st;
    }

    private void shakeNode(Node node) {
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
}
