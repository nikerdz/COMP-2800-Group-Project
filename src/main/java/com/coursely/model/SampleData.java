package com.coursely.model;

public class SampleData {

    public static Schedule createSampleSchedule() {

        Course comp2800 = new Course("COMP 2800", "Software Development");
        Course comp2120 = new Course("COMP 2120", "Object Oriented Programming");

        TimeBlock monday = new TimeBlock("Monday", "9:00", "10:30");
        TimeBlock tuesday = new TimeBlock("Tuesday", "11:00", "12:30");

        Section s1 = new Section(comp2800, "001", monday);
        Section s2 = new Section(comp2120, "002", tuesday);

        Schedule schedule = new Schedule();
        schedule.addSection(s1);
        schedule.addSection(s2);

        return schedule;
    }
}