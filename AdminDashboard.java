package ui;

import javax.swing.*;
import java.awt.*;

/**
 * ════════════════════════════════════════════════════════
 *  AdminDashboard.java  —  Main Application Window
 * ════════════════════════════════════════════════════════
 *  A tabbed JFrame with five tabs:
 *
 *  1. 🏠 Dashboard  — summary stats + quick-generate
 *  2. 👩‍🏫 Teachers   — add / edit / delete teachers
 *  3. 📚 Subjects   — add / edit / delete subjects
 *  4. 🏫 Classes    — add / edit / delete class sections
 *  5. ⚙  Generate  — run GA, choose class, view result
 * ════════════════════════════════════════════════════════
 */
public class AdminDashboard {

    private JFrame     frame;
    private JTabbedPane tabs;

    public void show() {
        frame = new JFrame("🗓  Timetable Generator v3 — Admin Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(Theme.BG);

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildTabs(),   BorderLayout.CENTER);
        frame.add(buildStatus(), BorderLayout.SOUTH);

        frame.setSize(1100, 720);
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Cleanup DB on close
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                db.DatabaseManager.getInstance().close();
            }
        });
    }

    // ── Header bar ────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(
                    0, 0, Theme.PRIMARY,
                    getWidth(), 0, new Color(0x283593)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setLayout(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(12, 22, 12, 22));
        header.setPreferredSize(new Dimension(0, 68));

        // Left: icon + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel icon = new JLabel("🗓");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        left.add(icon);

        JPanel titleStack = new JPanel();
        titleStack.setLayout(new BoxLayout(titleStack, BoxLayout.Y_AXIS));
        titleStack.setOpaque(false);

        JLabel title = new JLabel("Timetable Generator v3");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        titleStack.add(title);

        JLabel sub = new JLabel("Admin Dashboard  •  Genetic Algorithm Scheduler  •  SQLite");
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(new Color(0xC5CAE9));
        titleStack.add(sub);

        left.add(titleStack);
        header.add(left, BorderLayout.WEST);

        // Right: version badge
        JLabel badge = new JLabel(" v3.0 ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Theme.PRIMARY);
        badge.setBackground(new Color(0xFFD54F));
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);
        right.add(badge);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ── Tabs ──────────────────────────────────────────────

    private JTabbedPane buildTabs() {
        tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabs.setBackground(Theme.BG);
        tabs.setForeground(Theme.TEXT);

        // Each tab gets a panel from its dedicated class
        tabs.addTab("🏠  Dashboard",  new DashboardPanel());
        tabs.addTab("👩‍🏫  Teachers",   new TeacherPanel());
        tabs.addTab("📚  Subjects",   new SubjectPanel());
        tabs.addTab("🏫  Classes",    new ClassPanel());
        tabs.addTab("⚙  Generate",   new GeneratePanel());

        // Tab sizing
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        return tabs;
    }

    // ── Status bar ────────────────────────────────────────

    private JPanel buildStatus() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        bar.setBackground(new Color(0xE8EAF6));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JLabel status = new JLabel("✅  Connected to SQLite database  |  " +
            db.DAO.Teachers.getAll().size()      + " teachers  |  " +
            db.DAO.Subjects.getAll().size()      + " subjects  |  " +
            db.DAO.ClassSections.getAll().size() + " class sections");
        status.setFont(Theme.FONT_SMALL);
        status.setForeground(Theme.TEXT_DIM);
        bar.add(status);

        return bar;
    }
}
