package com.coursely.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Section {

    // UI id for selection/editing (not stored in DB)
    private final String uiId;

    // DB fields
    private Integer sectionId;  // null until saved/loaded
    private Integer courseId;   // can be null if you aren’t using courses yet

    private String sectionCode;     // maps to sections.section_code (can store title/course code)
    private SectionType sectionType; // maps to sections.section_type
    private String instructor;      // optional
    private String location;        // optional

    // UI-only field (recommended to persist later as hex)
    private Color color;

    // A section can meet multiple times/week
    private final List<TimeBlock> timeBlocks = new ArrayList<>();

    public Section(String sectionCode, SectionType sectionType) {
        this(null, null, sectionCode, sectionType, null, null, null);
    }

    public Section(Integer sectionId, Integer courseId, String sectionCode, SectionType sectionType,
                   String instructor, String location, Color color) {
        this.uiId = UUID.randomUUID().toString();
        this.sectionId = sectionId;
        this.courseId = courseId;
        this.sectionCode = Objects.requireNonNull(sectionCode);
        this.sectionType = (sectionType == null) ? SectionType.LECTURE : sectionType;
        this.instructor = instructor;
        this.location = location;
        this.color = color;
    }

    public String getUiId() {
        return uiId;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = Objects.requireNonNull(sectionCode);
    }

    public SectionType getSectionType() {
        return sectionType;
    }

    public void setSectionType(SectionType sectionType) {
        this.sectionType = (sectionType == null) ? SectionType.LECTURE : sectionType;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<TimeBlock> getTimeBlocks() {
        return Collections.unmodifiableList(timeBlocks);
    }

    public void addTimeBlock(TimeBlock timeBlock) {
        timeBlocks.add(Objects.requireNonNull(timeBlock));
    }

    public void removeTimeBlock(int index) {
        timeBlocks.remove(index);
    }

    public void clearTimeBlocks() {
        timeBlocks.clear();
    }

    public boolean overlaps(Section other) {
        for (TimeBlock a : this.timeBlocks) {
            for (TimeBlock b : other.timeBlocks) {
                if (a.overlaps(b)) return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return sectionCode + " (" + sectionType + ") " + timeBlocks;
    }
}