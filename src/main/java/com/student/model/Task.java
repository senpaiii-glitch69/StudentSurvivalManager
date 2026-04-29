package com.student.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Task {
    private String name;
    private String priority;
    private LocalDate date;
    private boolean done;
    private String subject;

    public Task() {}

    public Task(String name, LocalDate date, String priority) {
        this.name = name;
        this.date = date;
        this.priority = priority;
        this.done = false;
    }

    public Task(String name, LocalDate date, String priority, String subject) {
        this.name = name;
        this.date = date;
        this.priority = priority;
        this.subject = subject;
        this.done = false;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() {
        if (done) return "Done ✅";
        long d = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (d < 0)  return "Overdue ❌";
        if (d == 0) return "Due Today ⚡";
        return d + " day" + (d == 1 ? "" : "s") + " left";
    }

    public long getDaysLeft() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
