package db;

import model.*;
import java.sql.*;
import java.util.*;

/**
 * ════════════════════════════════════════════════════════
 *  DAO.java  —  Data Access Object (all entities)
 * ════════════════════════════════════════════════════════
 *  Contains inner static classes, one per entity:
 *    DAO.Teachers       — CRUD for teachers table
 *    DAO.Subjects       — CRUD for subjects table
 *    DAO.ClassSections  — CRUD for class_sections
 *    DAO.Timetable      — save / load timetable entries
 *
 *  All methods are static for simplicity.
 *  Each method fetches the Connection from DatabaseManager.
 * ════════════════════════════════════════════════════════
 */
public class DAO {

    private static Connection conn() {
        return DatabaseManager.getInstance().getConnection();
    }

    // ══════════════════════════════════════════════════════
    //  TEACHERS
    // ══════════════════════════════════════════════════════
    public static class Teachers {

        /** Returns all teachers ordered by name. */
        public static List<model.Teacher> getAll() {
            List<model.Teacher> list = new ArrayList<>();
            try (Statement s = conn().createStatement();
                 ResultSet rs = s.executeQuery(
                     "SELECT id,name,email,specialization FROM teachers ORDER BY name")) {
                while (rs.next()) {
                    list.add(new model.Teacher(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("specialization")));
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return list;
        }

        /** Inserts a new teacher; returns the generated id. */
        public static int insert(model.Teacher t) {
            String sql = "INSERT INTO teachers(name,email,specialization) VALUES(?,?,?)";
            try (PreparedStatement ps = conn().prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getName());
                ps.setString(2, t.getEmail());
                ps.setString(3, t.getSpecialization());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            } catch (SQLException e) { e.printStackTrace(); }
            return -1;
        }

        /** Updates an existing teacher row. */
        public static void update(model.Teacher t) {
            String sql = "UPDATE teachers SET name=?,email=?,specialization=? WHERE id=?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setString(1, t.getName());
                ps.setString(2, t.getEmail());
                ps.setString(3, t.getSpecialization());
                ps.setInt   (4, t.getId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        /** Deletes a teacher and their subject assignments. */
        public static void delete(int id) {
            try {
                try (PreparedStatement ps = conn().prepareStatement(
                        "DELETE FROM teacher_subjects WHERE teacher_id=?")) {
                    ps.setInt(1, id); ps.executeUpdate();
                }
                try (PreparedStatement ps = conn().prepareStatement(
                        "DELETE FROM teachers WHERE id=?")) {
                    ps.setInt(1, id); ps.executeUpdate();
                }
            } catch (SQLException e) { e.printStackTrace(); }
        }

        /** Returns teacher IDs who can teach a given subject. */
        public static List<Integer> getTeacherIdsForSubject(int subjectId) {
            List<Integer> ids = new ArrayList<>();
            String sql = "SELECT teacher_id FROM teacher_subjects WHERE subject_id=?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setInt(1, subjectId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) ids.add(rs.getInt(1));
            } catch (SQLException e) { e.printStackTrace(); }
            return ids;
        }

        /** Assigns a subject to a teacher. */
        public static void assignSubject(int teacherId, int subjectId) {
            String sql = "INSERT OR IGNORE INTO teacher_subjects(teacher_id,subject_id) VALUES(?,?)";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setInt(1, teacherId); ps.setInt(2, subjectId);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        /** Removes all subject assignments for a teacher, then re-inserts. */
        public static void setSubjects(int teacherId, List<Integer> subjectIds) {
            try {
                try (PreparedStatement ps = conn().prepareStatement(
                        "DELETE FROM teacher_subjects WHERE teacher_id=?")) {
                    ps.setInt(1, teacherId); ps.executeUpdate();
                }
                for (int sid : subjectIds) assignSubject(teacherId, sid);
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════
    //  SUBJECTS
    // ══════════════════════════════════════════════════════
    public static class Subjects {

        public static List<model.Subject> getAll() {
            List<model.Subject> list = new ArrayList<>();
            try (Statement s = conn().createStatement();
                 ResultSet rs = s.executeQuery(
                     "SELECT id,name,code,hours_per_week FROM subjects ORDER BY name")) {
                while (rs.next()) {
                    list.add(new model.Subject(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("code"),
                        rs.getInt("hours_per_week")));
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return list;
        }

        public static int insert(model.Subject subj) {
            String sql = "INSERT INTO subjects(name,code,hours_per_week) VALUES(?,?,?)";
            try (PreparedStatement ps = conn().prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, subj.getName());
                ps.setString(2, subj.getCode());
                ps.setInt   (3, subj.getHoursPerWeek());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            } catch (SQLException e) { e.printStackTrace(); }
            return -1;
        }

        public static void update(model.Subject subj) {
            String sql = "UPDATE subjects SET name=?,code=?,hours_per_week=? WHERE id=?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setString(1, subj.getName());
                ps.setString(2, subj.getCode());
                ps.setInt   (3, subj.getHoursPerWeek());
                ps.setInt   (4, subj.getId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        public static void delete(int id) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "DELETE FROM subjects WHERE id=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════
    //  CLASS SECTIONS
    // ══════════════════════════════════════════════════════
    public static class ClassSections {

        public static List<model.ClassSection> getAll() {
            List<model.ClassSection> list = new ArrayList<>();
            try (Statement s = conn().createStatement();
                 ResultSet rs = s.executeQuery(
                     "SELECT id,name,year,branch,strength FROM class_sections ORDER BY year,name")) {
                while (rs.next()) {
                    list.add(new model.ClassSection(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("year"),
                        rs.getString("branch"),
                        rs.getInt("strength")));
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return list;
        }

        public static int insert(model.ClassSection cs) {
            String sql = "INSERT INTO class_sections(name,year,branch,strength) VALUES(?,?,?,?)";
            try (PreparedStatement ps = conn().prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cs.getName());
                ps.setInt   (2, cs.getYear());
                ps.setString(3, cs.getBranch());
                ps.setInt   (4, cs.getStrength());
                ps.executeUpdate();
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) return keys.getInt(1);
            } catch (SQLException e) { e.printStackTrace(); }
            return -1;
        }

        public static void update(model.ClassSection cs) {
            String sql = "UPDATE class_sections SET name=?,year=?,branch=?,strength=? WHERE id=?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setString(1, cs.getName());
                ps.setInt   (2, cs.getYear());
                ps.setString(3, cs.getBranch());
                ps.setInt   (4, cs.getStrength());
                ps.setInt   (5, cs.getId());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }

        public static void delete(int id) {
            try (PreparedStatement ps = conn().prepareStatement(
                    "DELETE FROM class_sections WHERE id=?")) {
                ps.setInt(1, id); ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ══════════════════════════════════════════════════════
    //  TIMETABLE ENTRIES
    // ══════════════════════════════════════════════════════
    public static class Timetable {

        /**
         * Deletes all existing entries for a class section,
         * then bulk-inserts the new generated entries.
         */
        public static void saveForClass(int classSectionId,
                                         List<model.TimetableEntry> entries) {
            try {
                // Clear old entries
                try (PreparedStatement ps = conn().prepareStatement(
                        "DELETE FROM timetable_entries WHERE class_section_id=?")) {
                    ps.setInt(1, classSectionId);
                    ps.executeUpdate();
                }

                // Insert new entries
                String sql = """
                    INSERT INTO timetable_entries
                        (class_section_id, subject_id, teacher_id, day_of_week, period_number)
                    VALUES (?,?,?,?,?)
                    """;
                try (PreparedStatement ps = conn().prepareStatement(sql)) {
                    for (model.TimetableEntry e : entries) {
                        ps.setInt(1, classSectionId);
                        ps.setInt(2, e.getSubjectId());
                        ps.setInt(3, e.getTeacherId());
                        ps.setInt(4, e.getDayOfWeek());
                        ps.setInt(5, e.getPeriodNumber());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

            } catch (SQLException e) { e.printStackTrace(); }
        }

        /**
         * Loads a saved timetable for a class section.
         * Returns entries enriched with subject/teacher names.
         */
        public static List<model.TimetableEntry> loadForClass(int classSectionId) {
            List<model.TimetableEntry> list = new ArrayList<>();
            String sql = """
                SELECT te.id, te.subject_id, te.teacher_id, te.day_of_week, te.period_number,
                       s.name AS subject_name, s.code AS subject_code,
                       t.name AS teacher_name,
                       cs.name AS class_name
                FROM timetable_entries te
                JOIN subjects       s  ON s.id  = te.subject_id
                JOIN teachers       t  ON t.id  = te.teacher_id
                JOIN class_sections cs ON cs.id = te.class_section_id
                WHERE te.class_section_id = ?
                ORDER BY te.day_of_week, te.period_number
                """;
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setInt(1, classSectionId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    model.TimetableEntry e = new model.TimetableEntry();
                    e.setId           (rs.getInt("id"));
                    e.setClassSectionId(classSectionId);
                    e.setSubjectId    (rs.getInt("subject_id"));
                    e.setTeacherId    (rs.getInt("teacher_id"));
                    e.setDayOfWeek    (rs.getInt("day_of_week"));
                    e.setPeriodNumber (rs.getInt("period_number"));
                    e.setSubjectName  (rs.getString("subject_name"));
                    e.setSubjectCode  (rs.getString("subject_code"));
                    e.setTeacherName  (rs.getString("teacher_name"));
                    e.setClassName    (rs.getString("class_name"));
                    list.add(e);
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return list;
        }

        /** True if a saved timetable exists for this class. */
        public static boolean existsForClass(int classSectionId) {
            String sql = "SELECT COUNT(*) FROM timetable_entries WHERE class_section_id=?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setInt(1, classSectionId);
                ResultSet rs = ps.executeQuery();
                return rs.next() && rs.getInt(1) > 0;
            } catch (SQLException e) { return false; }
        }
    }
}
