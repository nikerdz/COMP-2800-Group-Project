package com.coursely.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Schedule {

    // DB fields
    private Integer scheduleId;     // null until saved/loaded
    private String scheduleName;    // maps to schedules.schedule_name
    private String term;            // maps to schedules.term
    private String createdAt;       // maps to schedules.created_at (optional)

    private final List<Section> sections = new ArrayList<>();

    public Schedule() {}

    public Schedule(String scheduleName, String term) {
        this.scheduleName = scheduleName;
        this.term = term;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public void addSection(Section section) {
        sections.add(section);
    }

    public boolean removeSectionByUiId(String uiId) {
        return sections.removeIf(s -> s.getUiId().equals(uiId));
    }

    public Optional<Section> findSectionByUiId(String uiId) {
        return sections.stream().filter(s -> s.getUiId().equals(uiId)).findFirst();
    }

    public boolean updateSectionByUiId(String uiId, Section updated) {
        for (Section existing : sections) {
            if (existing.getUiId().equals(uiId)) {
                // Keep ids, update fields
                existing.setCourseId(updated.getCourseId());
                existing.setSectionCode(updated.getSectionCode());
                existing.setSectionType(updated.getSectionType());
                existing.setInstructor(updated.getInstructor());
                existing.setLocation(updated.getLocation());
                existing.setColor(updated.getColor());

                existing.clearTimeBlocks();
                for (TimeBlock tb : updated.getTimeBlocks()) {
                    existing.addTimeBlock(tb);
                }
                return true;
            }
        }
        return false;
    }

    public List<Section> findConflicts(Section candidate) {
        List<Section> conflicts = new ArrayList<>();
        for (Section s : sections) {
            if (s.overlaps(candidate)) {
                conflicts.add(s);
            }
        }
        return conflicts;
    }
}