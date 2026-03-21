package com.coursely.model;

import java.util.Objects;

public class Course {
    // DB fields
    private Integer courseId;   // null until saved/loaded
    private String courseCode;  // e.g., COMP-2800
    private String courseName;  // e.g., Software Development
    private String faculty;     // optional
    private String term;        // optional (e.g., 2026W)

    public Course(String courseCode, String courseName) {
        this(null, courseCode, courseName, null, null);
    }

    public Course(Integer courseId, String courseCode, String courseName, String faculty, String term) {
        this.courseId = courseId;
        this.courseCode = Objects.requireNonNull(courseCode);
        this.courseName = Objects.requireNonNull(courseName);
        this.faculty = faculty;
        this.term = term;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = Objects.requireNonNull(courseCode);
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = Objects.requireNonNull(courseName);
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}