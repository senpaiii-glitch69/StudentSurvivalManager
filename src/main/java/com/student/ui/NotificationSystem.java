package com.student.ui;

import com.student.model.EventItem;
import com.student.model.Task;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationSystem {

    private List<Task> tasks;
    private List<EventItem> events;
    private List<Notification> notifications;
    private Timeline checkTimeline;
    private Label unreadCountLabel;
    private VBox root;

    public NotificationSystem(List<Task> tasks, List<EventItem> events) {
        this.tasks = tasks;
        this.events = events;
        this.notifications = new ArrayList<>();
        startNotificationChecker();
    }

    public Pane create() {
        root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 20;");

        // Header
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🔔 Notifications");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setStyle("-fx-text-fill: white;");

        unreadCountLabel = new Label("0 unread");
        unreadCountLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 13px;");

        Button markAllReadBtn = new Button("Mark All Read");
        markAllReadBtn.setStyle(
            "-fx-background-color: rgba(99,102,241,0.2);" +
            "-fx-text-fill: #a5b4fc;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 6 12;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 12px;"
        );

        markAllReadBtn.setOnAction(e -> markAllAsRead());

        header.getChildren().addAll(title, unreadCountLabel, markAllReadBtn);

        // Notification settings
        VBox settingsBox = new VBox(12);
        settingsBox.setPadding(new Insets(15));
        settingsBox.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 12;"
        );

        Label settingsTitle = new Label("Notification Settings");
        settingsTitle.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 13px; -fx-font-weight: bold;");

        HBox settingsControls = new HBox(20);
        settingsControls.setAlignment(Pos.CENTER_LEFT);

        CheckBox taskReminders = new CheckBox("Task Reminders");
        taskReminders.setSelected(true);
        taskReminders.setStyle("-fx-text-fill: white;");

        CheckBox eventReminders = new CheckBox("Event Reminders");
        eventReminders.setSelected(true);
        eventReminders.setStyle("-fx-text-fill: white;");

        CheckBox deadlineAlerts = new CheckBox("Deadline Alerts");
        deadlineAlerts.setSelected(true);
        deadlineAlerts.setStyle("-fx-text-fill: white;");

        settingsControls.getChildren().addAll(taskReminders, eventReminders, deadlineAlerts);

        settingsBox.getChildren().addAll(settingsTitle, settingsControls);

        // Notifications list
        VBox notificationsList = new VBox(10);
        notificationsList.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(notificationsList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        refreshNotifications(notificationsList);

        root.getChildren().addAll(header, settingsBox, scrollPane);

        return root;
    }

    private void startNotificationChecker() {
        checkTimeline = new Timeline(new KeyFrame(Duration.minutes(5), e -> checkDeadlines()));
        checkTimeline.setCycleCount(Animation.INDEFINITE);
        checkTimeline.play();
    }

    private void checkDeadlines() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        // Check tasks due today
        List<Task> dueToday = tasks.stream()
            .filter(t -> !t.isDone() && t.getDate().equals(today))
            .collect(Collectors.toList());

        for (Task task : dueToday) {
            if (!hasNotificationForTask(task.getId(), "due_today")) {
                addNotification(new Notification(
                    "📋 Task Due Today",
                    task.getName() + " is due today!",
                    "task",
                    task.getId(),
                    LocalDateTime.now()
                ));
            }
        }

        // Check tasks due tomorrow
        List<Task> dueTomorrow = tasks.stream()
            .filter(t -> !t.isDone() && t.getDate().equals(tomorrow))
            .collect(Collectors.toList());

        for (Task task : dueTomorrow) {
            if (!hasNotificationForTask(task.getId(), "due_tomorrow")) {
                addNotification(new Notification(
                    "📋 Task Due Tomorrow",
                    task.getName() + " is due tomorrow",
                    "task",
                    task.getId(),
                    LocalDateTime.now()
                ));
            }
        }

        // Check overdue tasks
        List<Task> overdue = tasks.stream()
            .filter(t -> !t.isDone() && t.getDate().isBefore(today))
            .collect(Collectors.toList());

        for (Task task : overdue) {
            if (!hasNotificationForTask(task.getId(), "overdue")) {
                addNotification(new Notification(
                    "⚠️ Overdue Task",
                    task.getName() + " is overdue!",
                    "task",
                    task.getId(),
                    LocalDateTime.now()
                ));
            }
        }

        // Check events today
        List<EventItem> eventsToday = events.stream()
            .filter(e -> !e.isDone() && e.getDate().equals(today))
            .collect(Collectors.toList());

        for (EventItem event : eventsToday) {
            if (!hasNotificationForEvent(event.getId(), "today")) {
                addNotification(new Notification(
                    "📅 Event Today",
                    event.getTitle() + " is today at " + event.getLocation(),
                    "event",
                    event.getId(),
                    LocalDateTime.now()
                ));
            }
        }
    }

    private boolean hasNotificationForTask(String taskId, String type) {
        return notifications.stream()
            .anyMatch(n -> n.getRelatedId().equals(taskId) && n.getType().equals("task") && n.getMessage().contains(type));
    }

    private boolean hasNotificationForEvent(String eventId, String type) {
        return notifications.stream()
            .anyMatch(n -> n.getRelatedId().equals(eventId) && n.getType().equals("event"));
    }

    private void addNotification(Notification notification) {
        notifications.add(0, notification);
        // Could trigger system notification here
        System.out.println("New notification: " + notification.getTitle());
    }

    private void refreshNotifications(VBox list) {
        list.getChildren().clear();

        if (notifications.isEmpty()) {
            Label emptyLabel = new Label("No notifications");
            emptyLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.3); -fx-font-size: 13px;");
            list.getChildren().add(emptyLabel);
            updateUnreadCount();
            return;
        }

        for (Notification notification : notifications) {
            VBox notifCard = createNotificationCard(notification);
            list.getChildren().add(notifCard);
        }

        updateUnreadCount();
    }

    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle(
            "-fx-background-color: " + (notification.isRead() ? "rgba(255,255,255,0.03)" : "rgba(99,102,241,0.15)") + ";" +
            "-fx-background-radius: 10;" +
            "-fx-border-color: " + (notification.isRead() ? "transparent" : "rgba(99,102,241,0.3)") + ";" +
            "-fx-border-width: " + (notification.isRead() ? "0" : "1") + ";" +
            "-fx-border-radius: 10;"
        );

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        if (!notification.isRead()) {
            Circle unreadDot = new Circle(4, Color.web("#6366f1"));
            header.getChildren().add(unreadDot);
        }

        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label timeLabel = new Label(formatTime(notification.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 11px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, timeLabel);

        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 13px;");
        messageLabel.setWrapText(true);

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button markReadBtn = new Button(notification.isRead() ? "Mark Unread" : "Mark Read");
        markReadBtn.setStyle(
            "-fx-background-color: rgba(99,102,241,0.2);" +
            "-fx-text-fill: #a5b4fc;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 4 10;" +
            "-fx-cursor: hand;" +
            "-fx-font-size: 11px;"
        );

        markReadBtn.setOnAction(e -> {
            notification.setRead(!notification.isRead());
            refreshNotifications(list);
        });

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
            "-fx-background-color: rgba(239,68,68,0.2);" +
            "-fx-text-fill: #f87171;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 4 8;" +
            "-fx-cursor: hand;"
        );

        deleteBtn.setOnAction(e -> {
            notifications.remove(notification);
            refreshNotifications(list);
        });

        actions.getChildren().addAll(markReadBtn, deleteBtn);

        card.getChildren().addAll(header, messageLabel, actions);

        return card;
    }

    private String formatTime(LocalDateTime dateTime) {
        long minutes = ChronoUnit.MINUTES.between(dateTime, LocalDateTime.now());

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + "m ago";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + "h ago";
        } else {
            long days = minutes / 1440;
            return days + "d ago";
        }
    }

    private void updateUnreadCount() {
        long unread = notifications.stream().filter(n -> !n.isRead()).count();
        unreadCountLabel.setText(unread + " unread");
    }

    private void markAllAsRead() {
        notifications.forEach(n -> n.setRead(true));
        refreshNotifications((VBox) ((ScrollPane) ((VBox) root.getChildren().get(2)).getContent()));
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void stop() {
        if (checkTimeline != null) {
            checkTimeline.stop();
        }
    }

    public static class Notification {
        private String id;
        private String title;
        private String message;
        private String type;
        private String relatedId;
        private LocalDateTime createdAt;
        private boolean read;

        public Notification(String title, String message, String type, String relatedId, LocalDateTime createdAt) {
            this.id = java.util.UUID.randomUUID().toString();
            this.title = title;
            this.message = message;
            this.type = type;
            this.relatedId = relatedId;
            this.createdAt = createdAt;
            this.read = false;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public String getRelatedId() { return relatedId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public boolean isRead() { return read; }
        public void setRead(boolean read) { this.read = read; }
    }
}
