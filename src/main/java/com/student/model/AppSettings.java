package com.student.model;

public class AppSettings {
    private String theme = "dark";
    private String currency = "₹";
    private boolean notificationsEnabled = true;
    private int pomodoroWorkMinutes = 25;
    private int pomodoroBreakMinutes = 5;
    private boolean autoSave = true;

    public AppSettings() {}

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) { this.notificationsEnabled = notificationsEnabled; }

    public int getPomodoroWorkMinutes() { return pomodoroWorkMinutes; }
    public void setPomodoroWorkMinutes(int pomodoroWorkMinutes) { this.pomodoroWorkMinutes = pomodoroWorkMinutes; }

    public int getPomodoroBreakMinutes() { return pomodoroBreakMinutes; }
    public void setPomodoroBreakMinutes(int pomodoroBreakMinutes) { this.pomodoroBreakMinutes = pomodoroBreakMinutes; }

    public boolean isAutoSave() { return autoSave; }
    public void setAutoSave(boolean autoSave) { this.autoSave = autoSave; }
}
