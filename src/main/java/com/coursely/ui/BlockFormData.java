package com.coursely.ui;

import java.awt.Color;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

public class BlockFormData {
    public final String courseCode;
    public final String courseName;
    public final String faculty;
    public final String term;

    public final String sectionCode;
    public final String sectionType;
    public final String instructor;
    public final String location;

    public final List<String> days;
    public final LocalTime start;
    public final LocalTime end;

    public final Color color;

    public BlockFormData(
            String courseCode,
            String courseName,
            String faculty,
            String term,
            String sectionCode,
            String sectionType,
            String instructor,
            String location,
            List<String> days,
            LocalTime start,
            LocalTime end,
            Color color
    ) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.faculty = faculty;
        this.term = term;
        this.sectionCode = sectionCode;
        this.sectionType = sectionType;
        this.instructor = instructor;
        this.location = location;
        this.days = days;
        this.start = start;
        this.end = end;
        this.color = color;
    }

    public static BlockFormData empty() {
        return new BlockFormData(
                "",
                "",
                "",
                "",
                "",
                "LECTURE",
                "",
                "",
                new ArrayList<>(),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                Theme.BLOCK_BLUE
        );
    }

    public static BlockFormData fromSection(Section section, String fallbackCourseCode) {
        List<String> days = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        if (!section.getTimeBlocks().isEmpty()) {
            start = section.getTimeBlocks().get(0).getStartTime();
            end = section.getTimeBlocks().get(0).getEndTime();
        }

        for (TimeBlock tb : section.getTimeBlocks()) {
            days.add(tb.getDayOfWeek());
        }

        Color color = section.getColor() == null ? Theme.BLOCK_BLUE : section.getColor();

        return new BlockFormData(
                fallbackCourseCode == null ? "" : fallbackCourseCode,
                "",
                "",
                "",
                section.getSectionCode() == null ? "" : section.getSectionCode(),
                section.getSectionType() == null ? "LECTURE" : section.getSectionType().name(),
                section.getInstructor() == null ? "" : section.getInstructor(),
                section.getLocation() == null ? "" : section.getLocation(),
                days,
                start,
                end,
                color
        );
    }
}