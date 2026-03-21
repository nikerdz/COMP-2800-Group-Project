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

public class TimetableService {

    private final CourseDao courseDao = new CourseDao();
    private final SectionDao sectionDao = new SectionDao();
    private final ScheduleDao scheduleDao = new ScheduleDao();

    public Schedule createSchedule(String scheduleName, String term) {
        Schedule schedule = new Schedule(scheduleName, term);
        return scheduleDao.insert(schedule);
    }

    public Schedule loadSchedule(int scheduleId) {
        return scheduleDao.findById(scheduleId);
    }

    public List<Schedule> getAllSchedules() {
        return scheduleDao.findAll();
    }

    public void saveScheduleDetails(Schedule schedule) {
        if (schedule.getScheduleId() == null) {
            scheduleDao.insert(schedule);
        } else {
            scheduleDao.update(schedule);
        }
    }

    public Section addBlockToSchedule(Schedule schedule, BlockFormData data) {
        ensureScheduleExists(schedule);

        Course course = courseDao.findOrCreate(
                data.courseCode,
                data.courseName,
                data.faculty,
                data.term == null || data.term.isBlank() ? schedule.getTerm() : data.term
        );

        Section section = new Section(
                null,
                course.getCourseId(),
                data.sectionCode,
                BlockDialog.parseSectionTypeOrDefault(data.sectionType),
                emptyToNull(data.instructor),
                emptyToNull(data.location),
                data.color
        );

        for (String day : data.days) {
            section.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        sectionDao.insert(section);
        scheduleDao.addSectionToSchedule(schedule.getScheduleId(), section.getSectionId());
        schedule.addSection(section);

        return section;
    }

    public void updateBlock(Section section, BlockFormData data, Schedule schedule) {
        ensureScheduleExists(schedule);

        Course course = courseDao.findOrCreate(
                data.courseCode,
                data.courseName,
                data.faculty,
                data.term == null || data.term.isBlank() ? schedule.getTerm() : data.term
        );

        // Update course fields in case name/faculty/term changed
        course.setCourseName(data.courseName);
        course.setFaculty(emptyToNull(data.faculty));
        course.setTerm(emptyToNull(data.term == null || data.term.isBlank() ? schedule.getTerm() : data.term));
        courseDao.update(course);

        section.setCourseId(course.getCourseId());
        section.setSectionCode(data.sectionCode);
        section.setSectionType(BlockDialog.parseSectionTypeOrDefault(data.sectionType));
        section.setInstructor(emptyToNull(data.instructor));
        section.setLocation(emptyToNull(data.location));
        section.setColor(data.color);

        section.clearTimeBlocks();
        for (String day : data.days) {
            section.addTimeBlock(new TimeBlock(day, data.start, data.end));
        }

        sectionDao.update(section);
    }

    public void deleteBlock(Schedule schedule, Section section) {
        if (schedule.getScheduleId() == null || section.getSectionId() == null) {
            throw new IllegalArgumentException("Schedule and section must both have DB ids");
        }

        scheduleDao.removeSectionFromSchedule(schedule.getScheduleId(), section.getSectionId());
        sectionDao.delete(section.getSectionId());
        schedule.removeSectionByUiId(section.getUiId());
    }

    private void ensureScheduleExists(Schedule schedule) {
        if (schedule.getScheduleId() == null) {
            scheduleDao.insert(schedule);
        }
    }

    private String emptyToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}