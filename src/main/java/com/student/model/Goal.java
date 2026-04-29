package com.student.model;

import java.time.LocalDate;
import java.util.Objects;

public class Goal {
    private String id;
    private String title;
    private String description;
    private String category;
    private double targetValue;
    private double currentValue;
    private String unit;
    private LocalDate deadline;
    private boolean completed;
    private LocalDate createdAt;

    public Goal() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDate.now();
    }

    public Goal(String title, String category, double targetValue, String unit, LocalDate deadline) {
        this();
        this.title = title;
        this.category = category;
        this.targetValue = targetValue;
        this.unit = unit;
        this.deadline = deadline;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getTargetValue() { return targetValue; }
    public void setTargetValue(double targetValue) { this.targetValue = targetValue; }

    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public double getProgress() {
        if (targetValue > 0) {
            return (currentValue / targetValue) * 100;
        }
        return 0;
    }

    public String getProgressString() {
        return String.format("%.1f%s / %.1f%s", currentValue, unit, targetValue, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goal goal = (Goal) o;
        return Objects.equals(id, goal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
