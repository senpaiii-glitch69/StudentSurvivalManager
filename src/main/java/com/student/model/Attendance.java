package com.student.model;

import java.time.LocalDate;
import java.util.Objects;

public class Attendance {
    private String id;
    private String subject;
    private LocalDate date;
    private boolean present;
    private String notes;

    public Attendance() {
        this.id = java.util.UUID.randomUUID().toString();
    }

    public Attendance(String subject, LocalDate date, boolean present) {
        this();
        this.subject = subject;
        this.date = date;
        this.present = present;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendance that = (Attendance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

