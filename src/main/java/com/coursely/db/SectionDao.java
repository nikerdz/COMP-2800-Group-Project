package com.coursely.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.coursely.model.Section;
import com.coursely.model.SectionType;
import com.coursely.model.TimeBlock;
import com.coursely.ui.Theme;

// Data Access Object for CRUD operations on the sections and time_blocks tables
// Section inserts and updates are transactional, as time blocks are written in the same operation
public class SectionDao {

    // Retrieves a section by its primary key with all associated time blocks populated
    // Returns null if no section with the given ID exists
    public Section findById(int sectionId) {
        String sql = """
                SELECT section_id, course_id, section_code, section_type, instructor, location, color
                FROM sections
                WHERE section_id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Section section = mapSectionRow(rs);
                // Load time blocks using the same connection to avoid opening a second one
                loadTimeBlocks(conn, section);
                return section;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find section by id: " + sectionId, e);
        }
    }

    // Inserts a section and all of its time blocks in a single transaction
    // Rolls back both the section and any partially inserted time blocks if any step fails
    public Section insert(Section section) {
        if (section.getCourseId() == null) {
            throw new IllegalArgumentException("Cannot insert section without courseId");
        }

        String sectionSql = """
                INSERT INTO sections (course_id, section_code, section_type, instructor, location, color)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String timeBlockSql = """
                INSERT INTO time_blocks (section_id, day_of_week, start_time, end_time)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement sectionPs = conn.prepareStatement(sectionSql, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement timeBlockPs = conn.prepareStatement(timeBlockSql, PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                sectionPs.setInt(1, section.getCourseId());
                sectionPs.setString(2, section.getSectionCode());
                sectionPs.setString(3, section.getSectionType().name());
                sectionPs.setString(4, section.getInstructor());
                sectionPs.setString(5, section.getLocation());
                // Color is stored as a hex string in the database
                sectionPs.setString(6, Theme.colorToHex(section.getColor()));
                sectionPs.executeUpdate();

                // Write the generated section ID back before inserting time blocks, as they reference it
                try (ResultSet keys = sectionPs.getGeneratedKeys()) {
                    if (keys.next()) {
                        section.setSectionId(keys.getInt(1));
                    }
                }

                for (TimeBlock tb : section.getTimeBlocks()) {
                    timeBlockPs.setInt(1, section.getSectionId());
                    timeBlockPs.setString(2, tb.getDayOfWeek());
                    timeBlockPs.setString(3, tb.getStartTime().toString());
                    timeBlockPs.setString(4, tb.getEndTime().toString());
                    timeBlockPs.executeUpdate();

                    // Write the generated ID and section reference back to the time block object
                    try (ResultSet tbKeys = timeBlockPs.getGeneratedKeys()) {
                        if (tbKeys.next()) {
                            tb.setTimeBlockId(tbKeys.getInt(1));
                        }
                    }

                    tb.setSectionId(section.getSectionId());
                }

                conn.commit();
                return section;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert section: " + section.getSectionCode(), e);
        }
    }

    // Updates a section and replaces all of its time blocks in a single transaction
    // Time blocks are deleted and re-inserted rather than diffed, since ordering and count may change
    public void update(Section section) {
        if (section.getSectionId() == null) {
            throw new IllegalArgumentException("Cannot update section without sectionId");
        }
        if (section.getCourseId() == null) {
            throw new IllegalArgumentException("Cannot update section without courseId");
        }

        String updateSectionSql = """
                UPDATE sections
                SET course_id = ?, section_code = ?, section_type = ?, instructor = ?, location = ?, color = ?
                WHERE section_id = ?
                """;

        String deleteTimeBlocksSql = "DELETE FROM time_blocks WHERE section_id = ?";

        String insertTimeBlockSql = """
                INSERT INTO time_blocks (section_id, day_of_week, start_time, end_time)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (
                PreparedStatement updatePs = conn.prepareStatement(updateSectionSql);
                PreparedStatement deleteTbPs = conn.prepareStatement(deleteTimeBlocksSql);
                PreparedStatement insertTbPs = conn.prepareStatement(insertTimeBlockSql, PreparedStatement.RETURN_GENERATED_KEYS)
            ) {
                updatePs.setInt(1, section.getCourseId());
                updatePs.setString(2, section.getSectionCode());
                updatePs.setString(3, section.getSectionType().name());
                updatePs.setString(4, section.getInstructor());
                updatePs.setString(5, section.getLocation());
                updatePs.setString(6, Theme.colorToHex(section.getColor()));
                updatePs.setInt(7, section.getSectionId());
                updatePs.executeUpdate();

                // Remove all existing time blocks so they can be replaced with the current set
                deleteTbPs.setInt(1, section.getSectionId());
                deleteTbPs.executeUpdate();

                for (TimeBlock tb : section.getTimeBlocks()) {
                    insertTbPs.setInt(1, section.getSectionId());
                    insertTbPs.setString(2, tb.getDayOfWeek());
                    insertTbPs.setString(3, tb.getStartTime().toString());
                    insertTbPs.setString(4, tb.getEndTime().toString());
                    insertTbPs.executeUpdate();

                    // Write the new generated ID and section reference back to the time block object
                    try (ResultSet tbKeys = insertTbPs.getGeneratedKeys()) {
                        if (tbKeys.next()) {
                            tb.setTimeBlockId(tbKeys.getInt(1));
                        }
                    }

                    tb.setSectionId(section.getSectionId());
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update section id: " + section.getSectionId(), e);
        }
    }

    // Deletes a section row by ID
    // Cascading deletes in the schema also remove all associated time_blocks rows
    public void delete(int sectionId) {
        String sql = "DELETE FROM sections WHERE section_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete section id: " + sectionId, e);
        }
    }

    // Returns all time blocks for a given section, ordered by day and start time
    public List<TimeBlock> findTimeBlocksBySectionId(int sectionId) {
        String sql = """
                SELECT time_block_id, section_id, day_of_week, start_time, end_time
                FROM time_blocks
                WHERE section_id = ?
                ORDER BY day_of_week, start_time
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                List<TimeBlock> blocks = new ArrayList<>();
                while (rs.next()) {
                    blocks.add(mapTimeBlockRow(rs));
                }
                return blocks;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find time blocks for section id: " + sectionId, e);
        }
    }

    // Loads and attaches time blocks to a section using an existing connection
    // Reuses the caller's connection to avoid nesting connections during findById
    private void loadTimeBlocks(Connection conn, Section section) throws SQLException {
        String sql = """
                SELECT time_block_id, section_id, day_of_week, start_time, end_time
                FROM time_blocks
                WHERE section_id = ?
                ORDER BY day_of_week, start_time
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, section.getSectionId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    section.addTimeBlock(mapTimeBlockRow(rs));
                }
            }
        }
    }

    // Maps the current row of a ResultSet to a Section object
    // Color is stored as a hex string in the DB and converted back to a Color object
    private Section mapSectionRow(ResultSet rs) throws SQLException {
        return new Section(
                rs.getInt("section_id"),
                rs.getInt("course_id"),
                rs.getString("section_code"),
                SectionType.fromString(rs.getString("section_type")),
                rs.getString("instructor"),
                rs.getString("location"),
                Theme.hexToColor(rs.getString("color"))
        );
    }

    // Maps the current row of a ResultSet to a TimeBlock object
    // start_time and end_time are stored as strings and parsed to LocalTime
    private TimeBlock mapTimeBlockRow(ResultSet rs) throws SQLException {
        return new TimeBlock(
                rs.getInt("time_block_id"),
                rs.getInt("section_id"),
                rs.getString("day_of_week"),
                LocalTime.parse(rs.getString("start_time")),
                LocalTime.parse(rs.getString("end_time"))
        );
    }
}