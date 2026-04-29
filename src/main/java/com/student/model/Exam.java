package com.student.model;

import java.time.LocalDate;
import java.util.Objects;

public class Exam {
    private String id;
    private String subject;
    private String title;
    private LocalDate date;
    private double score;
    private double maxScore;
    private String status;
    private String notes;

    public Exam() {
        this.id = java.util.UUID.randomUUID().toString();
        this.status = "Upcoming";
    }

    public Exam(String subject, String title, LocalDate date) {
        this();
        this.subject = subject;
        this.title = title;
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public double getPercentage() {
        if (maxScore > 0) {
            return (score / maxScore) * 100;
        }
        return 0;
    }

    public String getGrade() {
        double pct = getPercentage();
        if (pct >= 90) return "A";
        if (pct >= 85) return "A-";
        if (pct >= 80) return "B+";
        if (pct >= 75) return "B";
        if (pct >= 70) return "B-";
        if (pct >= 65) return "C+";
        if (pct >= 60) return "C";
        if (pct >= 55) return "C-";
        if (pct >= 50) return "D";
        return "F";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Exam exam = (Exam) o;
        return Objects.equals(id, exam.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

