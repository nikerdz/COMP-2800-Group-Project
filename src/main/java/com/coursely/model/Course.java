package com.coursely.model;

import java.util.Objects;

/**
 * Represents a course record in the application.
 * Stores core identifying information about a course.
 */
public class Course {

    // Database fields

    // Primary key from the database; null until the course is persisted or loaded.
    private Integer courseId;

    // Course code identifier (for example, COMP-2800).
    private String courseCode;

    // Human-readable course name (for example, Software Development).
    private String courseName;

    // Optional faculty or department name.
    private String faculty;

    // Optional academic term label (for example, 2026W).
    private String term;

    /**
     * Creates a course with only the required fields.
     * Optional fields and database id default to null.
     *
     * @param courseCode the course code
     * @param courseName the course name
     */
    public Course(String courseCode, String courseName) {
        this(null, courseCode, courseName, null, null);
    }

    /**
     * Creates a fully initialized course object.
     *
     * @param courseId the database id
     * @param courseCode the course code
     * @param courseName the course name
     * @param faculty the faculty or department
     * @param term the academic term
     */
    public Course(Integer courseId, String courseCode, String courseName, String faculty, String term) {
        this.courseId = courseId;
        this.courseCode = Objects.requireNonNull(courseCode);
        this.courseName = Objects.requireNonNull(courseName);
        this.faculty = faculty;
        this.term = term;
    }

    /**
     * Returns the database id of the course.
     *
     * @return the course id
     */
    public Integer getCourseId() {
        return courseId;
    }

    /**
     * Sets the database id of the course.
     *
     * @param courseId the course id
     */
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    /**
     * Returns the course code.
     *
     * @return the course code
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * Sets the course code.
     * Null values are not allowed.
     *
     * @param courseCode the course code
     */
    public void setCourseCode(String courseCode) {
        this.courseCode = Objects.requireNonNull(courseCode);
    }

    /**
     * Returns the course name.
     *
     * @return the course name
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Sets the course name.
     * Null values are not allowed.
     *
     * @param courseName the course name
     */
    public void setCourseName(String courseName) {
        this.courseName = Objects.requireNonNull(courseName);
    }

    /**
     * Returns the faculty or department.
     *
     * @return the faculty
     */
    public String getFaculty() {
        return faculty;
    }

    /**
     * Sets the faculty or department.
     *
     * @param faculty the faculty
     */
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    /**
     * Returns the academic term.
     *
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * Sets the academic term.
     *
     * @param term the term
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * Returns a readable string representation of the course.
     *
     * @return the course code and course name
     */
    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}