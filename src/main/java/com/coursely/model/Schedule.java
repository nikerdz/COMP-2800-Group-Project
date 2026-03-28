package com.coursely.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a saved schedule containing multiple sections.
 * A schedule may correspond to a specific term and can be persisted in the database.
 */
public class Schedule {

    // Database fields

    // Primary key from the database; null until persisted or loaded.
    private Integer scheduleId;

    // User-defined name of the schedule.
    private String scheduleName;

    // Academic term associated with the schedule.
    private String term;

    // Timestamp string from the database, if available.
    private String createdAt;

    // In-memory collection of sections that belong to this schedule.
    private final List<Section> sections = new ArrayList<>();

    /**
     * Creates an empty schedule.
     */
    public Schedule() {}

    /**
     * Creates a schedule with the main identifying fields.
     *
     * @param scheduleName the schedule name
     * @param term the academic term
     */
    public Schedule(String scheduleName, String term) {
        this.scheduleName = scheduleName;
        this.term = term;
    }

    /**
     * Returns the database id of the schedule.
     *
     * @return the schedule id
     */
    public Integer getScheduleId() {
        return scheduleId;
    }

    /**
     * Sets the database id of the schedule.
     *
     * @param scheduleId the schedule id
     */
    public void setScheduleId(Integer scheduleId) {
        this.scheduleId = scheduleId;
    }

    /**
     * Returns the name of the schedule.
     *
     * @return the schedule name
     */
    public String getScheduleName() {
        return scheduleName;
    }

    /**
     * Sets the name of the schedule.
     *
     * @param scheduleName the schedule name
     */
    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    /**
     * Returns the academic term associated with the schedule.
     *
     * @return the term
     */
    public String getTerm() {
        return term;
    }

    /**
     * Sets the academic term for the schedule.
     *
     * @param term the term
     */
    public void setTerm(String term) {
        this.term = term;
    }

    /**
     * Returns the creation timestamp string.
     *
     * @return the created-at value
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp string.
     *
     * @param createdAt the created-at value
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns an unmodifiable view of the sections in this schedule.
     * Prevents external code from directly mutating the internal list.
     *
     * @return a read-only list of sections
     */
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    /**
     * Adds a section to the schedule.
     *
     * @param section the section to add
     */
    public void addSection(Section section) {
        sections.add(section);
    }

    /**
     * Removes a section by its UI id.
     * The UI id is used for in-memory identification rather than the database id.
     *
     * @param uiId the UI id of the section
     * @return true if a matching section was removed, otherwise false
     */
    public boolean removeSectionByUiId(String uiId) {
        return sections.removeIf(s -> s.getUiId().equals(uiId));
    }

    /**
     * Finds a section by its UI id.
     *
     * @param uiId the UI id of the section
     * @return an Optional containing the matching section if found
     */
    public Optional<Section> findSectionByUiId(String uiId) {
        return sections.stream().filter(s -> s.getUiId().equals(uiId)).findFirst();
    }

    /**
     * Updates an existing section identified by UI id using values from another section object.
     * The existing section retains its own UI/database identity while its editable fields are replaced.
     *
     * @param uiId the UI id of the section to update
     * @param updated the source section containing updated values
     * @return true if a matching section was found and updated, otherwise false
     */
    public boolean updateSectionByUiId(String uiId, Section updated) {
        for (Section existing : sections) {
            if (existing.getUiId().equals(uiId)) {

                // Preserve object identity and ids while copying editable fields.
                existing.setCourseId(updated.getCourseId());
                existing.setSectionCode(updated.getSectionCode());
                existing.setSectionType(updated.getSectionType());
                existing.setInstructor(updated.getInstructor());
                existing.setLocation(updated.getLocation());
                existing.setColor(updated.getColor());

                // Replace all existing time blocks with the updated set.
                existing.clearTimeBlocks();
                for (TimeBlock tb : updated.getTimeBlocks()) {
                    existing.addTimeBlock(tb);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Finds all sections in the schedule that conflict with a candidate section.
     *
     * @param candidate the section to compare against
     * @return a list of conflicting sections
     */
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