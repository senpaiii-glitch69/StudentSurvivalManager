package com.student.model;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private List<Expense> expenses;
    private List<Task> tasks;
    private List<EventItem> events;
    private List<User> users;
    private List<Course> courses;
    private List<Attendance> attendance;
    private List<Exam> exams;
    private List<Note> notes;
    private List<ScheduleItem> schedule;
    private List<Goal> goals;
    private String currentUser;
    private AppSettings settings;

    public AppData() {
        this.expenses = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.events = new ArrayList<>();
        this.users = new ArrayList<>();
        this.courses = new ArrayList<>();
        this.attendance = new ArrayList<>();
        this.exams = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.schedule = new ArrayList<>();
        this.goals = new ArrayList<>();
        this.settings = new AppSettings();
    }

    public List<Expense> getExpenses() { return expenses; }
    public void setExpenses(List<Expense> expenses) { this.expenses = expenses; }

    public List<Task> getTasks() { return tasks; }
    public void setTasks(List<Task> tasks) { this.tasks = tasks; }

    public List<EventItem> getEvents() { return events; }
    public void setEvents(List<EventItem> events) { this.events = events; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }

    public List<Attendance> getAttendance() { return attendance; }
    public void setAttendance(List<Attendance> attendance) { this.attendance = attendance; }

    public List<Exam> getExams() { return exams; }
    public void setExams(List<Exam> exams) { this.exams = exams; }

    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }

    public List<ScheduleItem> getSchedule() { return schedule; }
    public void setSchedule(List<ScheduleItem> schedule) { this.schedule = schedule; }

    public List<Goal> getGoals() { return goals; }
    public void setGoals(List<Goal> goals) { this.goals = goals; }

    public String getCurrentUser() { return currentUser; }
    public void setCurrentUser(String currentUser) { this.currentUser = currentUser; }

    public AppSettings getSettings() { return settings; }
    public void setSettings(AppSettings settings) { this.settings = settings; }
}
