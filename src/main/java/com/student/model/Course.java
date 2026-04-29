package com.student.model;

import java.util.Objects;

public class Course {
    private String name;
    private String code;
    private double credits;
    private double grade;
    private String semester;

    public Course() {}

    public Course(String name, String code, double credits, double grade, String semester) {
        this.name = name;
        this.code = code;
        this.credits = credits;
        this.grade = grade;
        this.semester = semester;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getCredits() { return credits; }
    public void setCredits(double credits) { this.credits = credits; }

    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getLetterGrade() {
        if (grade >= 90) return "A";
        if (grade >= 85) return "A-";
        if (grade >= 80) return "B+";
        if (grade >= 75) return "B";
        if (grade >= 70) return "B-";
        if (grade >= 65) return "C+";
        if (grade >= 60) return "C";
        if (grade >= 55) return "C-";
        if (grade >= 50) return "D";
        return "F";
    }

    public double getGradePoints() {
        double g = grade;
        if (g >= 90) return 4.0;
        if (g >= 85) return 3.7;
        if (g >= 80) return 3.3;
        if (g >= 75) return 3.0;
        if (g >= 70) return 2.7;
        if (g >= 65) return 2.3;
        if (g >= 60) return 2.0;
        if (g >= 55) return 1.7;
        if (g >= 50) return 1.0;
        return 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(code, course.code) && Objects.equals(semester, course.semester);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, semester);
    }
}
