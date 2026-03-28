package com.coursely.service;

import java.util.List;

import com.coursely.db.CourseDao;
import com.coursely.db.ScheduleDao;
import com.coursely.db.SectionDao;
import com.coursely.model.Course;
import com.coursely.model.Schedule;
import com.coursely.model.Section;
import com.coursely.model.TimeBlock;
import com.coursely.ui.BlockDialog;
import com.coursely.ui.BlockFormData;

/**
 * Coordinates timetable-related business logic between the UI layer
 * and the data access layer.
 */
public class TimetableService {

    // DAO used to create, retrieve, and update course records.
    private final CourseDao courseDao = new CourseDao();

    // DAO used to create, retrieve, update, and delete section records.
    private final SectionDao sectionDao = new SectionDao();

    // DAO used to manage schedules and schedule-section relationships.
    private final ScheduleDao scheduleDao = new ScheduleDao();

    /**
     * Creates and persists a new schedule.
     *
     * @param scheduleName the name of the schedule
     * @param term the academic term
     * @return the inserted schedule, including generated database fields
     */
    public Schedule createSchedule(String scheduleName, String term) {
        Schedule schedule = new Schedule(scheduleName, term);
        return scheduleDao.insert(schedule);
    }

    /**
     * Loads a schedule by its database id.
     *
     * @param scheduleId the schedule id
     * @return the loaded schedule, or null if not found
     */
    public Schedule loadSchedule(int scheduleId) {
        return scheduleDao.findById(scheduleId);
    }

    /**
     * Retrieves all saved schedules.
     *
     * @return a list of all schedules
     */
    public List<Schedule> getAllSchedules() {
        return scheduleDao.findAll();
    }

    /**
     * Saves schedule metadata.
     * Inserts the schedule if it is new, otherwise updates the existing record.
     *
     * @param schedule the schedule to save
     */
    public void saveScheduleDetails(Schedule schedule) {
        if (schedule.getScheduleId() == null) {
            scheduleDao.insert(schedule);
        } else {
            scheduleDao.update(schedule);
        }
    }

    /**
     * Creates a new section from block form data, persists it, links it to the schedule,
     * and adds it to the in-memory schedule object.
     *
     * @param schedule the target schedule
     * @param data the form data used to build the section
     * @return the inserted section
     */
    public Section addBlockToSchedule(Schedule schedule, BlockFormData data) {

        // Ensure the schedule exists in the database before linking sections to it.
        ensureScheduleExists(schedule);

        // Find an existing course matching the provided fields or create one if needed.
        // If the form term is blank, fall back to the schedule's term.
        Course course = courseDao.findOrCreate(
                data.courseCode,
                data.courseName,
                data.faculty,
                data.term == null || data.term.isBlank() ? schedule.getTerm() : data.term
        );

        // Build a new section using the resolved course id and form data.
        Section section = new Section(
                null,
                course.getCourseId(),
                data.sectionCode,
                BlockDialog.parseSectionTypeOrDefault(data.sectionType),
                emptyToNull(data.instructor),
                emptyToNull(data.location),
                data.color
        );

        // Create one time block per selected day using the same start and end time.
        for (String day : data.days) {
            section.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        // Persist the section, create the schedule-section association,
        // and keep the in-memory schedule synchronized.
        sectionDao.insert(section);
        scheduleDao.addSectionToSchedule(schedule.getScheduleId(), section.getSectionId());
        schedule.addSection(section);

        return section;
    }

    /**
     * Updates an existing section using form data and persists the changes.
     * The associated course record is also updated to reflect any changed course details.
     *
     * @param section the section to update
     * @param data the updated form data
     * @param schedule the parent schedule
     */
    public void updateBlock(Section section, BlockFormData data, Schedule schedule) {

        // Ensure the parent schedule exists before updating related data.
        ensureScheduleExists(schedule);

        // Resolve the course record associated with this block.
        Course course = courseDao.findOrCreate(
                data.courseCode,
                data.courseName,
                data.faculty,
                data.term == null || data.term.isBlank() ? schedule.getTerm() : data.term
        );

        // Update course fields in case descriptive values changed in the form.
        course.setCourseName(data.courseName);
        course.setFaculty(emptyToNull(data.faculty));
        course.setTerm(emptyToNull(data.term == null || data.term.isBlank() ? schedule.getTerm() : data.term));
        courseDao.update(course);

        // Update the section's editable fields.
        section.setCourseId(course.getCourseId());
        section.setSectionCode(data.sectionCode);
        section.setSectionType(BlockDialog.parseSectionTypeOrDefault(data.sectionType));
        section.setInstructor(emptyToNull(data.instructor));
        section.setLocation(emptyToNull(data.location));
        section.setColor(data.color);

        // Replace all existing time blocks with the new set from the form.
        section.clearTimeBlocks();
        for (String day : data.days) {
            section.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        // Persist the updated section and its time blocks.
        sectionDao.update(section);
    }

    /**
     * Removes a section from a schedule and deletes the section record.
     * The in-memory schedule is also updated to stay consistent with the database.
     *
     * @param schedule the parent schedule
     * @param section the section to delete
     */
    public void deleteBlock(Schedule schedule, Section section) {

        // Both entities must already exist in the database to remove the relationship
        // and delete the section record safely.
        if (schedule.getScheduleId() == null || section.getSectionId() == null) {
            throw new IllegalArgumentException("Schedule and section must both have DB ids");
        }

        // Remove the schedule-section association, delete the section record,
        // and remove it from the in-memory schedule.
        scheduleDao.removeSectionFromSchedule(schedule.getScheduleId(), section.getSectionId());
        sectionDao.delete(section.getSectionId());
        schedule.removeSectionByUiId(section.getUiId());
    }

    /**
     * Ensures that a schedule has been inserted into the database.
     * If the schedule is new, it is persisted before further operations continue.
     *
     * @param schedule the schedule to verify
     */
    private void ensureScheduleExists(Schedule schedule) {
        if (schedule.getScheduleId() == null) {
            scheduleDao.insert(schedule);
        }
    }

    /**
     * Converts null or blank strings to null.
     * This is useful for optional database fields.
     *
     * @param value the input string
     * @return null if the value is null or blank, otherwise the original string
     */
    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}