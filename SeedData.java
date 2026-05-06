package db;

import java.sql.*;

/**
 * ════════════════════════════════════════════════════════
 *  SeedData.java
 * ════════════════════════════════════════════════════════
 *  Inserts realistic sample data into a fresh database.
 *  Called automatically by DatabaseManager on first launch.
 *
 *  Sample data includes:
 *   • 10 teachers with specializations
 *   •  8 subjects with weekly hour counts
 *   •  6 class sections across 3 years
 *   • Teacher-subject assignments
 * ════════════════════════════════════════════════════════
 */
public class SeedData {

    private final DatabaseManager db;

    public SeedData(DatabaseManager db) {
        this.db = db;
    }

    /** Inserts all sample data. Safe to call multiple times (uses INSERT OR IGNORE). */
    public void insertAll() {
        try {
            insertTeachers();
            insertSubjects();
            insertClassSections();
            insertTeacherSubjects();
            System.out.println("[DB] Sample data seeded successfully.");
        } catch (SQLException e) {
            System.err.println("[DB] Seeding failed: " + e.getMessage());
        }
    }

    // ── Teachers ──────────────────────────────────────────

    private void insertTeachers() throws SQLException {
        String sql = "INSERT OR IGNORE INTO teachers(name, email, specialization) VALUES(?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {

            Object[][] teachers = {
                {"Mr. Rajiv Sharma",  "r.sharma@college.edu",  "Mathematics"},
                {"Ms. Priya Verma",   "p.verma@college.edu",   "Physics"},
                {"Mr. Anil Gupta",    "a.gupta@college.edu",   "Chemistry"},
                {"Ms. Sunita Nair",   "s.nair@college.edu",    "Biology"},
                {"Mr. Vikram Singh",  "v.singh@college.edu",   "English"},
                {"Ms. Deepa Joshi",   "d.joshi@college.edu",   "Computer Science"},
                {"Mr. Ravi Kumar",    "r.kumar@college.edu",   "Electronics"},
                {"Ms. Anita Patel",   "a.patel@college.edu",   "Mathematics"},
                {"Mr. Suresh Rao",    "s.rao@college.edu",     "Data Structures"},
                {"Ms. Kavita Mehta",  "k.mehta@college.edu",   "Algorithms"},
            };

            for (Object[] t : teachers) {
                ps.setString(1, (String) t[0]);
                ps.setString(2, (String) t[1]);
                ps.setString(3, (String) t[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Subjects ──────────────────────────────────────────

    private void insertSubjects() throws SQLException {
        String sql = "INSERT OR IGNORE INTO subjects(name, code, hours_per_week) VALUES(?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {

            Object[][] subjects = {
                {"Mathematics",       "MATH301", 5},
                {"Physics",           "PHY201",  4},
                {"Chemistry",         "CHEM201", 4},
                {"Data Structures",   "CS301",   5},
                {"Algorithms",        "CS302",   4},
                {"Computer Networks", "CS401",   4},
                {"Operating Systems", "CS403",   4},
                {"English",           "ENG101",  3},
                {"Electronics",       "ECE201",  4},
                {"Computer Science",  "CS101",   5},
            };

            for (Object[] s : subjects) {
                ps.setString(1, (String)  s[0]);
                ps.setString(2, (String)  s[1]);
                ps.setInt   (3, (Integer) s[2]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Class sections ────────────────────────────────────

    private void insertClassSections() throws SQLException {
        String sql = "INSERT OR IGNORE INTO class_sections(name, year, branch, strength) VALUES(?,?,?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {

            Object[][] sections = {
                {"10-A", 1, "Science",     65},
                {"10-B", 1, "Commerce",    60},
                {"11-A", 2, "CS",          55},
                {"11-B", 2, "Electronics", 58},
                {"12-A", 3, "CS",          52},
                {"12-B", 3, "Science",     60},
            };

            for (Object[] s : sections) {
                ps.setString(1, (String)  s[0]);
                ps.setInt   (2, (Integer) s[1]);
                ps.setString(3, (String)  s[2]);
                ps.setInt   (4, (Integer) s[3]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // ── Teacher–Subject assignments ───────────────────────

    private void insertTeacherSubjects() throws SQLException {
        String sql = "INSERT OR IGNORE INTO teacher_subjects(teacher_id, subject_id) VALUES(?,?)";
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {

            // [teacher_id, subject_id]  (IDs match AUTOINCREMENT insertion order above)
            int[][] mappings = {
                {1, 1},   // Sharma → Mathematics
                {8, 1},   // Patel  → Mathematics
                {2, 2},   // Verma  → Physics
                {4, 2},   // Nair   → Physics
                {3, 3},   // Gupta  → Chemistry
                {4, 3},   // Nair   → Chemistry
                {9, 4},   // Kumar  → Data Structures
                {6, 4},   // Joshi  → Data Structures
                {10,5},   // Mehta  → Algorithms
                {9, 6},   // Kumar  → Computer Networks
                {7, 6},   // Rao    → Computer Networks
                {6, 7},   // Joshi  → Operating Systems
                {5, 8},   // Singh  → English
                {7, 9},   // Rao    → Electronics
                {6,10},   // Joshi  → Computer Science
            };

            for (int[] m : mappings) {
                ps.setInt(1, m[0]);
                ps.setInt(2, m[1]);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
