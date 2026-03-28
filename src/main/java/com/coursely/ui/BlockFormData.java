package com.coursely.ui;

import java.awt.Color;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.coursely.model.Section;
import com.coursely.model.TimeBlock;

/**
 * Immutable data holder for timetable block form input.
 * Stores the values needed to create or update a section block.
 */
public class BlockFormData {

    // Course-related fields.
    public final String courseCode;
    public final String courseName;
    public final String faculty;
    public final String term;

    // Section-related fields.
    public final String sectionCode;
    public final String sectionType;
    public final String instructor;
    public final String location;

    // Time and day selection fields.
    public final List<String> days;
    public final LocalTime start;
    public final LocalTime end;

    // Display color for the timetable block.
    public final Color color;

    /**
     * Creates a new form data object containing all values needed
     * for a timetable block.
     *
     * @param courseCode the course code
     * @param courseName the course name
     * @param faculty the faculty or department
     * @param term the academic term
     * @param sectionCode the section code
     * @param sectionType the section type
     * @param instructor the instructor name
     * @param location the location
     * @param days the selected meeting days
     * @param start the start time
     * @param end the end time
     * @param color the display color
     */
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

    /**
     * Returns a default empty form data object for creating a new block.
     *
     * @return a BlockFormData instance with default values
     */
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

    /**
     * Builds form data from an existing section so it can be used
     * to pre-fill an edit dialog.
     *
     * @param section the section to convert
     * @param fallbackCourseCode the course code to use if no course lookup is available
     * @return a BlockFormData instance populated from the section
     */
    public static BlockFormData fromSection(Section section, String fallbackCourseCode) {
        List<String> days = new ArrayList<>();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        // Use the first time block as the default time range if one exists.
        if (!section.getTimeBlocks().isEmpty()) {
            start = section.getTimeBlocks().get(0).getStartTime();
            end = section.getTimeBlocks().get(0).getEndTime();
        }

        // Collect all meeting days from the section's time blocks.
        for (TimeBlock tb : section.getTimeBlocks()) {
            days.add(tb.getDayOfWeek());
        }

        // Fall back to the default block color if none is assigned.
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