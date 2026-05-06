package db;

import java.sql.*;
import java.io.File;

/**
 * ════════════════════════════════════════════════════════
 *  DatabaseManager.java
 * ════════════════════════════════════════════════════════
 *  Singleton that manages the SQLite JDBC connection.
 *
 *  PATTERN: Singleton — only ONE connection is ever open.
 *
 *  The database file is stored at:   data/timetable.db
 *  It is auto-created on first launch with all tables
 *  and seed data already populated.
 *
 *  ADDING REAL SQLITE:
 *   Run:  python setup.py
 *   This downloads sqlite-jdbc.jar into lib/ and the Java
 *   Extension Pack in VS Code auto-adds it to the classpath.
 * ════════════════════════════════════════════════════════
 */
public class DatabaseManager {

    // ── Singleton instance ────────────────────────────────
    private static DatabaseManager instance;

    // ── JDBC settings ─────────────────────────────────────
    private static final String DB_PATH = "data/timetable.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    private Connection connection;

    // Private constructor (Singleton pattern)
    private DatabaseManager() {}

    /** Returns the single shared instance. */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    // ══════════════════════════════════════════════════════
    //  INITIALISE
    // ══════════════════════════════════════════════════════

    /**
     * Opens the connection, creates the schema if needed,
     * and seeds sample data on first run.
     */
    public void initialise() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");

            // Ensure data/ directory exists
            new File("data").mkdirs();

            // Check if this is a fresh database (for seeding)
            boolean isNew = !new File(DB_PATH).exists()
                         || new File(DB_PATH).length() == 0;

            // Open connection
            connection = DriverManager.getConnection(JDBC_URL);

            // Enable foreign keys
            execute("PRAGMA foreign_keys = ON;");

            // Create schema
            createSchema();

            // Seed sample data only on fresh database
            if (isNew) {
                new SeedData(this).insertAll();
            }

            System.out.println("[DB] Connected to: " + DB_PATH);

        } catch (ClassNotFoundException e) {
            showDriverError();
        } catch (SQLException e) {
            System.err.println("[DB] Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════
    //  SCHEMA
    // ══════════════════════════════════════════════════════

    private void createSchema() throws SQLException {
        // Teachers table
        execute("""
            CREATE TABLE IF NOT EXISTS teachers (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                name            TEXT    NOT NULL,
                email           TEXT,
                specialization  TEXT
            );
        """);

        // Subjects table
        execute("""
            CREATE TABLE IF NOT EXISTS subjects (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                name            TEXT    NOT NULL,
                code            TEXT    NOT NULL UNIQUE,
                hours_per_week  INTEGER DEFAULT 5
            );
        """);

        // Class sections (e.g., 11-A CS, 12-B Science)
        execute("""
            CREATE TABLE IF NOT EXISTS class_sections (
                id       INTEGER PRIMARY KEY AUTOINCREMENT,
                name     TEXT    NOT NULL,
                year     INTEGER NOT NULL,
                branch   TEXT    NOT NULL,
                strength INTEGER DEFAULT 60
            );
        """);

        // Which teacher can teach which subject
        execute("""
            CREATE TABLE IF NOT EXISTS teacher_subjects (
                teacher_id  INTEGER NOT NULL,
                subject_id  INTEGER NOT NULL,
                PRIMARY KEY (teacher_id, subject_id),
                FOREIGN KEY (teacher_id) REFERENCES teachers(id) ON DELETE CASCADE,
                FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
            );
        """);

        // Generated timetable entries
        execute("""
            CREATE TABLE IF NOT EXISTS timetable_entries (
                id               INTEGER PRIMARY KEY AUTOINCREMENT,
                class_section_id INTEGER NOT NULL,
                subject_id       INTEGER NOT NULL,
                teacher_id       INTEGER NOT NULL,
                day_of_week      INTEGER NOT NULL,
                period_number    INTEGER NOT NULL,
                FOREIGN KEY (class_section_id) REFERENCES class_sections(id) ON DELETE CASCADE,
                FOREIGN KEY (subject_id)       REFERENCES subjects(id),
                FOREIGN KEY (teacher_id)       REFERENCES teachers(id)
            );
        """);
    }

    // ══════════════════════════════════════════════════════
    //  PUBLIC HELPERS
    // ══════════════════════════════════════════════════════

    /**
     * Returns the live JDBC Connection.
     * All DAO classes use this to run their queries.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Convenience method: run a DDL or DML statement with no result set.
     */
    public void execute(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    /** Closes the connection cleanly (call on application exit). */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Error closing: " + e.getMessage());
        }
    }

    // ── Error UI ──────────────────────────────────────────

    private void showDriverError() {
        String msg = """
            SQLite JDBC driver not found!
            
            Please run the setup script first:
                python setup.py
            
            This downloads sqlite-jdbc.jar into lib/ and
            configures VS Code to use it automatically.
            """;
        javax.swing.JOptionPane.showMessageDialog(null, msg,
            "Missing Driver", javax.swing.JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
