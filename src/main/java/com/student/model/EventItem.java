package com.student.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class EventItem {
    private String title;
    private LocalDate date;
    private String location;
    private boolean done;
    private String type;

    public EventItem() {}

    public EventItem(String title, LocalDate date, String location) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.done = false;
    }

    public EventItem(String title, LocalDate date, String location, String type) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.type = type;
        this.done = false;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() {
        if (done) return "Completed ✅";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), date);
        if (days < 0) return "Overdue ❌";
        if (days == 0) return "Today ⚡";
        return "In " + days + " day" + (days == 1 ? "" : "s");
    }

    public long getDaysLeft() {
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
