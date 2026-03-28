package com.coursely.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a course section such as a lecture, lab, or tutorial.
 * A section may contain multiple meeting times during the week.
 */
public class Section {

    // UI-only id used for selection and editing within the application.
    // This value is not currently stored in the database.
    private final String uiId;

    // Database fields

    // Primary key from the database; null until persisted or loaded.
    private Integer sectionId;

    // Foreign key linking the section to a course.
    // May be null if course records are not yet being used.
    private Integer courseId;

    // Section identifier or display label.
    private String sectionCode;

    // Classification of the section, such as lecture or lab.
    private SectionType sectionType;

    // Optional instructor name.
    private String instructor;

    // Optional location string.
    private String location;

    // UI-only display color for timetable rendering.
    // This may later be persisted as a hex string if needed.
    private Color color;

    // A section can meet multiple times per week.
    private final List<TimeBlock> timeBlocks = new ArrayList<>();

    /**
     * Creates a section with only the required fields.
     *
     * @param sectionCode the section code or label
     * @param sectionType the section type
     */
    public Section(String sectionCode, SectionType sectionType) {
        this(null, null, sectionCode, sectionType, null, null, null);
    }

    /**
     * Creates a fully initialized section.
     *
     * @param sectionId the database id
     * @param courseId the related course id
     * @param sectionCode the section code or label
     * @param sectionType the section type
     * @param instructor the instructor name
     * @param location the meeting location
     * @param color the display color
     */
    public Section(Integer sectionId, Integer courseId, String sectionCode, SectionType sectionType,
                   String instructor, String location, Color color) {
        this.uiId = UUID.randomUUID().toString();
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.sectionCode = Objects.requireNonNull(sectionCode);

        // Default to LECTURE if no section type is provided.
        this.sectionType = (sectionType == null) ? SectionType.LECTURE : sectionType;

        this.instructor = instructor;
        this.location = location;
        this.color = color;
    }

    /**
     * Returns the UI-only id for this section.
     *
     * @return the UI id
     */
    public String getUiId() {
        return uiId;
    }

    /**
     * Returns the database id of the section.
     *
     * @return the section id
     */
    public Integer getSectionId() {
        return sectionId;
    }

    /**
     * Sets the database id of the section.
     *
     * @param sectionId the section id
     */
    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    /**
     * Returns the related course id.
     *
     * @return the course id
     */
    public Integer getCourseId() {
        return courseId;
    }

    /**
     * Sets the related course id.
     *
     * @param courseId the course id
     */
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    /**
     * Returns the section code or display label.
     *
     * @return the section code
     */
    public String getSectionCode() {
        return sectionCode;
    }

    /**
     * Sets the section code.
     * Null values are not allowed.
     *
     * @param sectionCode the section code
     */
    public void setSectionCode(String sectionCode) {
        this.sectionCode = Objects.requireNonNull(sectionCode);
    }

    /**
     * Returns the section type.
     *
     * @return the section type
     */
    public SectionType getSectionType() {
        return sectionType;
    }

    /**
     * Sets the section type.
     * Defaults to LECTURE if a null value is provided.
     *
     * @param sectionType the section type
     */
    public void setSectionType(SectionType sectionType) {
        this.sectionType = (sectionType == null) ? SectionType.LECTURE : sectionType;
    }

    /**
     * Returns the instructor name.
     *
     * @return the instructor
     */
    public String getInstructor() {
        return instructor;
    }

    /**
     * Sets the instructor name.
     *
     * @param instructor the instructor
     */
    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    /**
     * Returns the location string.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location string.
     *
     * @param location the location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the UI display color.
     *
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the UI display color.
     *
     * @param color the color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Returns an unmodifiable view of the section's time blocks.
     *
     * @return a read-only list of time blocks
     */
    public List<TimeBlock> getTimeBlocks() {
        return Collections.unmodifiableList(timeBlocks);
    }

    /**
     * Adds a time block to the section.
     *
     * @param timeBlock the time block to add
     */
    public void addTimeBlock(TimeBlock timeBlock) {
        timeBlocks.add(Objects.requireNonNull(timeBlock));
    }

    /**
     * Removes a time block by index.
     *
     * @param index the index of the time block to remove
     */
    public void removeTimeBlock(int index) {
        timeBlocks.remove(index);
    }

    /**
     * Removes all time blocks from the section.
     */
    public void clearTimeBlocks() {
        timeBlocks.clear();
    }

    /**
     * Checks whether this section overlaps with another section.
     * Two sections overlap if any of their time blocks overlap.
     *
     * @param other the other section to compare against
     * @return true if any time block overlaps, otherwise false
     */
    public boolean overlaps(Section other) {
        for (TimeBlock a : this.timeBlocks) {
            for (TimeBlock b : other.timeBlocks) {
                if (a.overlaps(b)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a readable string representation of the section.
     *
     * @return the section code, section type, and time blocks
     */
    @Override
    public String toString() {
        return sectionCode + " (" + sectionType + ") " + timeBlocks;
    }
}