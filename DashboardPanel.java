package ui;

import db.DAO;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * DashboardPanel.java — Home tab with summary statistics.
 */
public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildStatsRow(), BorderLayout.NORTH);
        add(buildInfoCard(), BorderLayout.CENTER);
    }

    // ── Stat cards row ────────────────────────────────────

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setBackground(Theme.BG);

        int teachers = DAO.Teachers.getAll().size();
        int subjects = DAO.Subjects.getAll().size();
        int classes  = DAO.ClassSections.getAll().size();

        // Count how many classes have a generated timetable
        long generated = DAO.ClassSections.getAll().stream()
            .filter(cs -> DAO.Timetable.existsForClass(cs.getId()))
            .count();

        row.add(statCard("👩‍🏫", String.valueOf(teachers), "Teachers",    Theme.PRIMARY));
        row.add(statCard("📚", String.valueOf(subjects), "Subjects",     new Color(0x00695C)));
        row.add(statCard("🏫", String.valueOf(classes),  "Classes",      new Color(0x4527A0)));
        row.add(statCard("📅", String.valueOf(generated),"Timetables",   new Color(0xBF360C)));

        return row;
    }

    private JPanel statCard(String emoji, String value, String label, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(20, 16, 20, 16)));

        JLabel emojiLbl = new JLabel(emoji, SwingConstants.CENTER);
        emojiLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        emojiLbl.setAlignmentX(CENTER_ALIGNMENT);
        card.add(emojiLbl);

        card.add(Box.createVerticalStrut(8));

        JLabel valueLbl = new JLabel(value, SwingConstants.CENTER);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLbl.setForeground(accent);
        valueLbl.setAlignmentX(CENTER_ALIGNMENT);
        card.add(valueLbl);

        JLabel labelLbl = new JLabel(label, SwingConstants.CENTER);
        labelLbl.setFont(Theme.FONT_BODY);
        labelLbl.setForeground(Theme.TEXT_DIM);
        labelLbl.setAlignmentX(CENTER_ALIGNMENT);
        card.add(labelLbl);

        return card;
    }

    // ── Info / instructions card ──────────────────────────

    private JPanel buildInfoCard() {
        JPanel card = Theme.makeCard();
        card.setLayout(new BorderLayout(0, 12));

        JLabel heading = Theme.makeSectionLabel("🚀  Getting Started");
        card.add(heading, BorderLayout.NORTH);

        JTextArea info = new JTextArea("""
            Welcome to Timetable Generator v3!

            WORKFLOW
            ────────────────────────────────────────────────────────────────────
            1. Teachers tab   →  Add teachers and assign which subjects they can teach.
            2. Subjects tab   →  Add subjects with their weekly hour requirements.
            3. Classes tab    →  Add class sections (e.g., 11-A CS, 12-B Science).
            4. Generate tab   →  Pick a class section and run the Genetic Algorithm.
                                  The GA evolves over 200 generations to find the best
                                  conflict-free timetable. Results are saved to SQLite.

            ALGORITHM
            ────────────────────────────────────────────────────────────────────
            The Genetic Algorithm (GA) works like biological evolution:
              • Population  — 80 candidate timetables per generation
              • Fitness     — scored by constraint violations (max 1000)
              • Selection   — tournament selection picks the best parents
              • Crossover   — two parents combine to create a child timetable
              • Mutation    — random gene changes prevent getting stuck
              • Elitism     — top 6 chromosomes survive unchanged each generation

            CONSTRAINTS ENFORCED
            ────────────────────────────────────────────────────────────────────
              ✅  No teacher double-booked in the same period           (hard)
              ✅  No same subject in consecutive periods                (soft)
              ✅  Subject weekly count matches hours_per_week target    (soft)
              ✅  No subject more than 2 times in a day                 (soft)

            All sample data has been pre-loaded. Switch to Generate tab to try it!
            """);
        info.setFont(Theme.FONT_SMALL);
        info.setEditable(false);
        info.setBackground(new Color(0xF8F9FF));
        info.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        info.setLineWrap(true);
        info.setWrapStyleWord(true);

        card.add(new JScrollPane(info), BorderLayout.CENTER);
        return card;
    }
}
