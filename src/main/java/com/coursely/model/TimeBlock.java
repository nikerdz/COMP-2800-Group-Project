package com.coursely.model;

import java.time.LocalTime;
import java.util.Objects;

public class TimeBlock {

    // DB fields
    private Integer timeBlockId; // null until saved/loaded
    private Integer sectionId;   // null until saved/loaded

    private String dayOfWeek;    // Must match schema: Monday...Sunday
    private LocalTime startTime;
    private LocalTime endTime;

    public TimeBlock(String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this(null, null, dayOfWeek, startTime, endTime);
    }

    public TimeBlock(Integer timeBlockId, Integer sectionId, String dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.timeBlockId = timeBlockId;
        this.sectionId = sectionId;
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
    }

    public Integer getTimeBlockId() {
        return timeBlockId;
    }

    public void setTimeBlockId(Integer timeBlockId) {
        this.timeBlockId = timeBlockId;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = Objects.requireNonNull(startTime);
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = Objects.requireNonNull(endTime);
    }

    public boolean overlaps(TimeBlock other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) return false;
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime + " - " + endTime;
    }
}