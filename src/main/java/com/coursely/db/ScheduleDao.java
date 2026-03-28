package com.coursely.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.coursely.model.Schedule;
import com.coursely.model.Section;

// Data Access Object for CRUD operations on the schedules table
// Also manages the schedule_sections join table for section associations
public class ScheduleDao {

    // Used to load full Section objects when populating a schedule
    private final SectionDao sectionDao = new SectionDao();

    // Inserts a new schedule row and assigns the generated primary key back to the object
    public Schedule insert(Schedule schedule) {
        String sql = """
                INSERT INTO schedules (schedule_name, term)
                VALUES (?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, schedule.getScheduleName());
            ps.setString(2, schedule.getTerm());
            ps.executeUpdate();

            // Write the auto-generated ID back to the object so the caller has it
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    schedule.setScheduleId(keys.getInt(1));
                }
            }

            return schedule;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert schedule: " + schedule.getScheduleName(), e);
        }
    }

    // Updates the name and term of an existing schedule row
    // Requires scheduleId to be set, as it is used to target the correct row
    public void update(Schedule schedule) {
        if (schedule.getScheduleId() == null) {
            throw new IllegalArgumentException("Cannot update schedule without scheduleId");
        }

        String sql = """
                UPDATE schedules
                SET schedule_name = ?, term = ?
                WHERE schedule_id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, schedule.getScheduleName());
            ps.setString(2, schedule.getTerm());
            ps.setInt(3, schedule.getScheduleId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update schedule id: " + schedule.getScheduleId(), e);
        }
    }

    // Retrieves a schedule by its primary key, with all associated sections populated
    // Returns null if no schedule with the given ID exists
    public Schedule findById(int scheduleId) {
        String sql = """
                SELECT schedule_id, schedule_name, term, created_at
                FROM schedules
                WHERE schedule_id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Schedule schedule = mapScheduleRow(rs);

                // Populate the schedule with its linked sections after the base row is loaded
                for (Section section : findSectionsForSchedule(scheduleId)) {
                    schedule.addSection(section);
                }

                return schedule;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find schedule by id: " + scheduleId, e);
        }
    }

    // Returns all schedules ordered by most recently created, then alphabetically by name
    // Sections are not populated here for performance — use findById for a fully loaded schedule
    public List<Schedule> findAll() {
        String sql = """
                SELECT schedule_id, schedule_name, term, created_at
                FROM schedules
                ORDER BY created_at DESC, schedule_name ASC
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Schedule> schedules = new ArrayList<>();

            while (rs.next()) {
                schedules.add(mapScheduleRow(rs));
            }

            return schedules;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load schedules", e);
        }
    }

    // Deletes a schedule row by ID
    // Cascading deletes in the schema also remove associated schedule_sections rows
    public void delete(int scheduleId) {
        String sql = "DELETE FROM schedules WHERE schedule_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete schedule id: " + scheduleId, e);
        }
    }

    // Links a section to a schedule via the schedule_sections join table
    // INSERT OR IGNORE prevents a duplicate error if the association already exists
    public void addSectionToSchedule(int scheduleId, int sectionId) {
        String sql = """
                INSERT OR IGNORE INTO schedule_sections (schedule_id, section_id)
                VALUES (?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to link section " + sectionId + " to schedule " + scheduleId, e
            );
        }
    }

    // Removes the association between a section and a schedule in the join table
    // Does not delete the section itself
    public void removeSectionFromSchedule(int scheduleId, int sectionId) {
        String sql = """
                DELETE FROM schedule_sections
                WHERE schedule_id = ? AND section_id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);
            ps.setInt(2, sectionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to unlink section " + sectionId + " from schedule " + scheduleId, e
            );
        }
    }

    // Returns the full Section objects associated with a given schedule
    // Joins schedule_sections to sections to get IDs, then delegates to SectionDao for full objects
    public List<Section> findSectionsForSchedule(int scheduleId) {
        String sql = """
                SELECT s.section_id
                FROM schedule_sections ss
                JOIN sections s ON ss.section_id = s.section_id
                WHERE ss.schedule_id = ?
                ORDER BY s.section_id
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, scheduleId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Section> sections = new ArrayList<>();

                while (rs.next()) {
                    Section section = sectionDao.findById(rs.getInt("section_id"));
                    if (section != null) {
                        sections.add(section);
                    }
                }

                return sections;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load sections for schedule id: " + scheduleId, e);
        }
    }

    // Maps the current row of a ResultSet to a Schedule object
    private Schedule mapScheduleRow(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule(rs.getString("schedule_name"), rs.getString("term"));
        schedule.setScheduleId(rs.getInt("schedule_id"));
        schedule.setCreatedAt(rs.getString("created_at"));
        return schedule;
    }
}