package com.student.model;

import java.time.LocalDate;

public class Expense {
    private String name;
    private int value;
    private String category;
    private LocalDate date;

    public Expense() {}

    public Expense(String name, int value, String category) {
        this.name = name;
        this.value = value;
        this.category = category;
        this.date = LocalDate.now();
    }

    public Expense(String name, int value, String category, LocalDate date) {
        this.name = name;
        this.value = value;
        this.category = category;
        this.date = date;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
