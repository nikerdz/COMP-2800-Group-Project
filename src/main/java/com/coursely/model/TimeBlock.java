package com.coursely.model;

import java.time.LocalTime;
import java.util.Objects;

public class TimeBlock {

    private String day;          // "Monday", "Tuesday", etc.
    private LocalTime startTime; // 08:00
    private LocalTime endTime;   // 09:30

    public TimeBlock(String day, LocalTime startTime, LocalTime endTime) {
        this.day = Objects.requireNonNull(day);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
    }

    public String getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setDay(String day) {
        this.day = Objects.requireNonNull(day);
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = Objects.requireNonNull(startTime);
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = Objects.requireNonNull(endTime);
    }

    public boolean overlaps(TimeBlock other) {
        if (!this.day.equals(other.day)) return false;
        // overlap if start < other.end AND other.start < end
        return this.startTime.isBefore(other.endTime) && other.startTime.isBefore(this.endTime);
    }

    @Override
    public String toString() {
        return day + " " + startTime + " - " + endTime;
    }
}