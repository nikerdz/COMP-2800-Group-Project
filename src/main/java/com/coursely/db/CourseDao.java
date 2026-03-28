package com.coursely.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.coursely.model.Course;

// Data Access Object for CRUD operations on the courses table
public class CourseDao {

    // Retrieves a course by its primary key, returns null if not found
    public Course findById(int courseId) {
        String sql = """
                SELECT course_id, course_code, course_name, faculty, term
                FROM courses
                WHERE course_id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find course by id: " + courseId, e);
        }
    }

    // Retrieves a course matching both code and term
    // Handles the case where term is null on both sides to avoid SQL NULL comparison issues
    public Course findByCodeAndTerm(String courseCode, String term) {
        String sql = """
                SELECT course_id, course_code, course_name, faculty, term
                FROM courses
                WHERE course_code = ? AND (
                    (term IS NULL AND ? IS NULL) OR term = ?
                )
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // term is bound twice to cover both sides of the NULL-safe comparison
            ps.setString(1, courseCode);
            ps.setString(2, term);
            ps.setString(3, term);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find course by code and term", e);
        }
    }

    // Inserts a new course row and assigns the generated primary key back to the object
    public Course insert(Course course) {
        String sql = """
                INSERT INTO courses (course_code, course_name, faculty, term)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setString(3, course.getFaculty());
            ps.setString(4, course.getTerm());

            ps.executeUpdate();

            // Write the auto-generated ID back to the object so the caller has it
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    course.setCourseId(keys.getInt(1));
                }
            }

            return course;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert course: " + course.getCourseCode(), e);
        }
    }

    // Updates all editable fields of an existing course row
    // Requires courseId to be set, as it is used to target the correct row
    public void update(Course course) {
        if (course.getCourseId() == null) {
            throw new IllegalArgumentException("Cannot update course without courseId");
        }

        String sql = """
                UPDATE courses
                SET course_code = ?, course_name = ?, faculty = ?, term = ?
                WHERE course_id = ?
                """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, course.getCourseCode());
            ps.setString(2, course.getCourseName());
            ps.setString(3, course.getFaculty());
            ps.setString(4, course.getTerm());
            ps.setInt(5, course.getCourseId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update course id: " + course.getCourseId(), e);
        }
    }

    // Returns an existing course matching the code and term, or inserts and returns a new one
    // Used to avoid duplicate course entries when importing or syncing data
    public Course findOrCreate(String courseCode, String courseName, String faculty, String term) {
        Course existing = findByCodeAndTerm(courseCode, term);
        if (existing != null) {
            return existing;
        }

        Course course = new Course(courseCode, courseName);
        course.setFaculty(faculty);
        course.setTerm(term);
        return insert(course);
    }

    // Maps the current row of a ResultSet to a Course object
    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("course_id"),
                rs.getString("course_code"),
                rs.getString("course_name"),
                rs.getString("faculty"),
                rs.getString("term")
        );
    }
}