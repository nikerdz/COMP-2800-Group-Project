package com.coursely.model;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a single meeting block for a section on a specific day and time range.
 */
public class TimeBlock {

    // Database fields

    // Primary key from the database; null until persisted or loaded.
    private Integer timeBlockId;

    // Foreign key linking the time block to a section.
    private Integer sectionId;

    // Day of the week string.
    // Intended to match the database schema format exactly (Monday...Sunday).
    private String dayOfWeek;

    // Start time of the block.
    private LocalTime startTime;

    // End time of the block.
    private LocalTime endTime;

    /**
     * Creates a time block with only the required fields.
     *
     * @param dayOfWeek the day of the week
     * @param startTime the start time
     * @param endTime the end time
     */
    public TimeBlock(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this(null, null, dayOfWeek, startTime, endTime);
    }

    /**
     * Creates a fully initialized time block.
     *
     * @param timeBlockId the database id
     * @param sectionId the related section id
     * @param dayOfWeek the day of the week
     * @param startTime the start time
     * @param endTime the end time
     */
    public TimeBlock(Integer timeBlockId, Integer sectionId, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.timeBlockId = timeBlockId;
        this.sectionId = sectionId;
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
    }

    /**
     * Returns the database id of the time block.
     *
     * @return the time block id
     */
    public Integer getTimeBlockId() {
        return timeBlockId;
    }

    /**
     * Sets the database id of the time block.
     *
     * @param timeBlockId the time block id
     */
    public void setTimeBlockId(Integer timeBlockId) {
        this.timeBlockId = timeBlockId;
    }

    /**
     * Returns the related section id.
     *
     * @return the section id
     */
    public Integer getSectionId() {
        return sectionId;
    }

    /**
     * Sets the related section id.
     *
     * @param sectionId the section id
     */
    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    /**
     * Returns the day of the week.
     *
     * @return the day of the week
     */
    public String getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Sets the day of the week.
     * Null values are not allowed.
     *
     * @param dayOfWeek the day of the week
     */
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
    }

    /**
     * Returns the start time.
     *
     * @return the start time
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     * Null values are not allowed.
     *
     * @param startTime the start time
     */
    public void setStartTime(LocalTime startTime) {
        this.startTime = Objects.requireNonNull(startTime);
    }

    /**
     * Returns the end time.
     *
     * @return the end time
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     * Null values are not allowed.
     *
     * @param endTime the end time
     */
    public void setEndTime(LocalTime endTime) {
        this.endTime = Objects.requireNonNull(endTime);
    }

    /**
     * Checks whether this time block overlaps with another time block.
     * Overlap is only possible when both blocks occur on the same day.
     *
     * @param other the other time block to compare against
     * @return true if the time ranges overlap on the same day, otherwise false
     */
    public boolean overlaps(TimeBlock other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }

        // Two intervals overlap when each starts before the other ends.
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    /**
     * Returns a readable string representation of the time block.
     *
     * @return the day and time range
     */
    @Override
    public String toString() {
        return dayOfWeek + " " + startTime + " - " + endTime;
    }
}