package com.coursely.model;

public class Section {

    private Course course;
    private String sectionNumber;
    private TimeBlock timeBlock;

    public Section(Course course, String sectionNumber, TimeBlock timeBlock) {
        this.course = course;
        this.sectionNumber = sectionNumber;
        this.timeBlock = timeBlock;
    }

    public Course getCourse() {
        return course;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public TimeBlock getTimeBlock() {
        return timeBlock;
    }

    @Override
    public String toString() {
        return course + " | Section " + sectionNumber + " | " + timeBlock;
    }
}