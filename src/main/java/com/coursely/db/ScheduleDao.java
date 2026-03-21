package com.coursely.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.coursely.model.Schedule;
import com.coursely.model.Section;

public class ScheduleDao {

    private final SectionDao sectionDao = new SectionDao();

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

                for (Section section : findSectionsForSchedule(scheduleId)) {
                    schedule.addSection(section);
                }

                return schedule;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find schedule by id: " + scheduleId, e);
        }
    }

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

    private Schedule mapScheduleRow(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule(rs.getString("schedule_name"), rs.getString("term"));
        schedule.setScheduleId(rs.getInt("schedule_id"));
        schedule.setCreatedAt(rs.getString("created_at"));
        return schedule;
    }
}