package ui;

import algorithm.*;
import db.DAO;
import model.*;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ════════════════════════════════════════════════════════════════
 *  GeneratePanel.java  —  GA Runner + Timetable Display
 * ════════════════════════════════════════════════════════════════
 *  Left column:
 *   • Class section selector (JComboBox)
 *   • Generate button
 *   • Progress bar + generation counter
 *   • Fitness score display
 *   • Saved timetable loader
 *
 *  Right column:
 *   • Tabbed timetable view (one tab per class section that has
 *     been generated this session)
 * ════════════════════════════════════════════════════════════════
 */
public class GeneratePanel extends JPanel {

    // ── Left panel components ─────────────────────────────
    private JComboBox<ClassSection> classPicker;
    private JButton                 generateBtn;
    private JProgressBar            progressBar;
    private JLabel                  genLabel;
    private JLabel                  fitnessLabel;
    private JButton                 viewSavedBtn;

    // ── Right panel ───────────────────────────────────────
    private JTabbedPane resultTabs;

    // ── Colour map: subjectId → colour chip ──────────────
    private final Map<Integer, Color> subjectColours = new HashMap<>();

    // ── Labels ────────────────────────────────────────────
    private static final String[] DAY_NAMES    = {"Monday","Tuesday","Wednesday","Thursday","Friday"};
    private static final String[] PERIOD_NAMES = {"Period 1","Period 2","Period 3","Period 4","Period 5","Period 6"};

    public GeneratePanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildControlPanel(), BorderLayout.WEST);
        add(buildResultPanel(),  BorderLayout.CENTER);

        assignSubjectColours();
    }

    // ══════════════════════════════════════════════════════
    //  LEFT — Controls
    // ══════════════════════════════════════════════════════

    private JPanel buildControlPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setPreferredSize(new Dimension(280, 0));
        outer.setBackground(Theme.BG);

        JPanel card = Theme.makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Title
        JLabel title = Theme.makeSectionLabel("⚙  Generate Timetable");
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(16));

        // Class picker
        JLabel pickLbl = new JLabel("Select Class Section:");
        pickLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pickLbl.setForeground(Theme.TEXT_DIM);
        pickLbl.setAlignmentX(LEFT_ALIGNMENT);
        card.add(pickLbl);
        card.add(Box.createVerticalStrut(4));

        classPicker = new JComboBox<>();
        classPicker.setFont(Theme.FONT_BODY);
        classPicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        classPicker.setAlignmentX(LEFT_ALIGNMENT);
        refreshClassPicker();
        card.add(classPicker);
        card.add(Box.createVerticalStrut(16));

        // GA info box
        JTextArea info = new JTextArea(
            "Algorithm: Genetic Algorithm\n" +
            "Population: 80 chromosomes\n" +
            "Generations: up to 200\n" +
            "Fitness max: 1000 pts\n\n" +
            "Constraints:\n" +
            "• No teacher double-booking\n" +
            "• No consecutive repeats\n" +
            "• Hours/week balanced");
        info.setFont(Theme.FONT_SMALL);
        info.setEditable(false);
        info.setBackground(new Color(0xF0F4FF));
        info.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        info.setAlignmentX(LEFT_ALIGNMENT);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        card.add(info);
        card.add(Box.createVerticalStrut(14));

        // Generate button
        generateBtn = Theme.makeBtn("▶  Run Genetic Algorithm", Theme.ACCENT);
        generateBtn.setAlignmentX(LEFT_ALIGNMENT);
        generateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        generateBtn.addActionListener(e -> runGA());
        card.add(generateBtn);
        card.add(Box.createVerticalStrut(12));

        // Progress bar
        progressBar = new JProgressBar(0, 200);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setFont(Theme.FONT_SMALL);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        progressBar.setAlignmentX(LEFT_ALIGNMENT);
        progressBar.setForeground(Theme.PRIMARY_MED);
        card.add(progressBar);
        card.add(Box.createVerticalStrut(6));

        // Generation counter
        genLabel = new JLabel("Generation: —");
        genLabel.setFont(Theme.FONT_SMALL);
        genLabel.setForeground(Theme.TEXT_DIM);
        genLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(genLabel);

        // Fitness label
        fitnessLabel = new JLabel("Best Fitness: —");
        fitnessLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fitnessLabel.setForeground(Theme.SUCCESS);
        fitnessLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(fitnessLabel);
        card.add(Box.createVerticalStrut(16));

        // View saved
        viewSavedBtn = Theme.makeBtn("📂  Load Saved Timetable", Theme.PRIMARY);
        viewSavedBtn.setAlignmentX(LEFT_ALIGNMENT);
        viewSavedBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        viewSavedBtn.addActionListener(e -> loadSaved());
        card.add(viewSavedBtn);

        outer.add(card, BorderLayout.NORTH);
        return outer;
    }

    // ══════════════════════════════════════════════════════
    //  RIGHT — Result tabs
    // ══════════════════════════════════════════════════════

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG);

        JLabel heading = Theme.makeSectionLabel("📅  Generated Timetables");
        heading.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        panel.add(heading, BorderLayout.NORTH);

        resultTabs = new JTabbedPane();
        resultTabs.setFont(Theme.FONT_BODY);
        resultTabs.setBackground(Theme.BG);

        // Placeholder
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(Color.WHITE);
        JLabel hint = new JLabel("Select a class section and click 'Run Genetic Algorithm'");
        hint.setFont(Theme.FONT_BODY);
        hint.setForeground(Theme.TEXT_DIM);
        placeholder.add(hint);
        resultTabs.addTab("  No timetable yet  ", placeholder);

        panel.add(resultTabs, BorderLayout.CENTER);
        return panel;
    }

    // ══════════════════════════════════════════════════════
    //  GA RUNNER
    // ══════════════════════════════════════════════════════

    private void runGA() {
        ClassSection cs = (ClassSection) classPicker.getSelectedItem();
        if (cs == null) {
            JOptionPane.showMessageDialog(this,
                "Please add at least one class section first.",
                "No Class", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Subject> subjects = DAO.Subjects.getAll();
        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please add subjects before generating.",
                "No Subjects", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Build subjectId → [teacherIds] map
        Map<Integer, List<Integer>> subjectTeacherMap = new HashMap<>();
        for (Subject s : subjects) {
            subjectTeacherMap.put(s.getId(), DAO.Teachers.getTeacherIdsForSubject(s.getId()));
        }

        // Disable UI
        generateBtn.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("Initialising population…");
        genLabel.setText("Generation: 0 / 200");
        fitnessLabel.setText("Best Fitness: —");

        GeneticAlgorithm ga = new GeneticAlgorithm(cs, subjects, subjectTeacherMap);

        // Use SwingWorker to run GA off the EDT
        SwingWorker<Chromosome, int[]> worker = new SwingWorker<>() {

            @Override
            protected Chromosome doInBackground() {
                ga.setProgressCallback((gen, max, fitness) ->
                    publish(new int[]{gen, max, fitness}));
                return ga.run();
            }

            @Override
            protected void process(List<int[]> chunks) {
                // Update progress bar with latest update
                int[] latest = chunks.get(chunks.size() - 1);
                int gen = latest[0], max = latest[1], fit = latest[2];
                progressBar.setValue(gen);
                progressBar.setString("Gen " + gen + " / " + max);
                genLabel.setText("Generation: " + gen + " / " + max);
                fitnessLabel.setText("Best Fitness: " + fit + " / 1000");
                fitnessLabel.setForeground(fit >= 900 ? Theme.SUCCESS :
                                           fit >= 700 ? Theme.WARNING : Theme.DANGER);
            }

            @Override
            protected void done() {
                try {
                    Chromosome best = get();
                    progressBar.setValue(200);
                    progressBar.setString("Complete!");

                    // Convert chromosome → TimetableEntry list and save
                    List<TimetableEntry> entries = chromosomeToEntries(
                        best, cs.getId(), subjects, subjectTeacherMap);
                    DAO.Timetable.saveForClass(cs.getId(), entries);

                    // Display
                    showTimetable(cs, entries);

                    JOptionPane.showMessageDialog(GeneratePanel.this,
                        "Timetable generated and saved!\nFinal fitness: " +
                        best.getFitness() + " / 1000",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(GeneratePanel.this,
                        "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    generateBtn.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // ══════════════════════════════════════════════════════
    //  CONVERT CHROMOSOME → ENTRIES
    // ══════════════════════════════════════════════════════

    /**
     * Converts the winning Chromosome (array of Genes) into
     * a list of TimetableEntry objects ready for DB insertion.
     */
    private List<TimetableEntry> chromosomeToEntries(
            Chromosome c, int classSectionId,
            List<Subject> subjects,
            Map<Integer, List<Integer>> subjectTeacherMap) {

        List<TimetableEntry> entries = new ArrayList<>();

        for (int day = 0; day < GeneticAlgorithm.DAYS; day++) {
            for (int period = 0; period < GeneticAlgorithm.PERIODS; period++) {
                Gene gene = c.getGeneAt(day, period);

                int subjectId = gene.getSubjectId();
                int teacherId = gene.getTeacherId();

                // Fallback: pick first valid teacher if none assigned
                if (teacherId == 0) {
                    List<Integer> tIds = subjectTeacherMap.getOrDefault(
                        subjectId, Collections.emptyList());
                    if (!tIds.isEmpty()) teacherId = tIds.get(0);
                }

                TimetableEntry entry = new TimetableEntry(
                    classSectionId, subjectId, teacherId, day, period);

                // Enrich with names for display
                subjects.stream()
                    .filter(s -> s.getId() == subjectId)
                    .findFirst()
                    .ifPresent(s -> {
                        entry.setSubjectName(s.getName());
                        entry.setSubjectCode(s.getCode());
                    });

                entries.add(entry);
            }
        }
        return entries;
    }

    // ══════════════════════════════════════════════════════
    //  DISPLAY TIMETABLE IN A NEW TAB
    // ══════════════════════════════════════════════════════

    private void showTimetable(ClassSection cs, List<TimetableEntry> entries) {
        // Remove placeholder tab if present
        if (resultTabs.getTabCount() == 1 &&
            resultTabs.getTitleAt(0).contains("No timetable")) {
            resultTabs.removeAll();
        }

        // Build tab content
        JPanel content = buildTimetableGrid(cs, entries);

        // Remove old tab for same class if exists
        for (int i = 0; i < resultTabs.getTabCount(); i++) {
            if (resultTabs.getTitleAt(i).contains(cs.getName())) {
                resultTabs.removeTabAt(i);
                break;
            }
        }

        resultTabs.addTab("  " + cs.getName() + " " + cs.getBranch() + "  ", content);
        resultTabs.setSelectedIndex(resultTabs.getTabCount() - 1);
    }

    /**
     * Builds a styled JTable grid for the given timetable entries.
     */
    private JPanel buildTimetableGrid(ClassSection cs,
                                       List<TimetableEntry> entries) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Header
        JLabel header = new JLabel("📅  " + cs.toString() + "  —  Generated Timetable",
            SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setForeground(Theme.PRIMARY);
        wrapper.add(header, BorderLayout.NORTH);

        // Build 5×6 grid from entries
        // entries are ordered by day then period
        String[][] grid = new String[GeneticAlgorithm.DAYS][GeneticAlgorithm.PERIODS];
        String[][] teachers = new String[GeneticAlgorithm.DAYS][GeneticAlgorithm.PERIODS];
        int[][]    subIds   = new int[GeneticAlgorithm.DAYS][GeneticAlgorithm.PERIODS];

        for (TimetableEntry e : entries) {
            int d = e.getDayOfWeek();
            int p = e.getPeriodNumber();
            if (d >= 0 && d < 5 && p >= 0 && p < 6) {
                grid[d][p]     = e.getSubjectName() != null ? e.getSubjectName() : "—";
                teachers[d][p] = e.getTeacherName() != null ? e.getTeacherName() : "";
                subIds[d][p]   = e.getSubjectId();
            }
        }

        // Table model: columns = periods, rows = days
        String[] cols = new String[GeneticAlgorithm.PERIODS + 1];
        cols[0] = "Day";
        for (int p = 0; p < GeneticAlgorithm.PERIODS; p++) cols[p+1] = "P" + (p+1);

        Object[][] rowData = new Object[GeneticAlgorithm.DAYS][GeneticAlgorithm.PERIODS + 1];
        for (int d = 0; d < GeneticAlgorithm.DAYS; d++) {
            rowData[d][0] = DAY_NAMES[d];
            for (int p = 0; p < GeneticAlgorithm.PERIODS; p++) {
                String subj  = grid[d][p] != null ? grid[d][p] : "—";
                String teach = teachers[d][p] != null ? teachers[d][p] : "";
                rowData[d][p+1] = "<html><center><b>" + subj + "</b><br>" +
                    "<font color='#546E7A' size='-1'><i>" + teach + "</i></font></center></html>";
            }
        }

        DefaultTableModel model = new DefaultTableModel(rowData, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(60);
        table.setShowGrid(true);
        table.setGridColor(Theme.BORDER);
        table.setFont(Theme.FONT_BODY);
        table.setSelectionBackground(Theme.ROW_SEL);

        // Custom cell renderer — colour by subject
        final int[][] finalSubIds = subIds;
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, val, sel, focus, row, col);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setOpaque(true);

                if (col == 0) {
                    // Day name column
                    lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    lbl.setBackground(Theme.PRIMARY);
                    lbl.setForeground(Color.WHITE);
                } else if (sel) {
                    lbl.setBackground(Theme.ROW_SEL);
                    lbl.setForeground(Theme.TEXT);
                } else {
                    int subjectId = (row < 5 && col-1 < 6) ? finalSubIds[row][col-1] : 0;
                    Color chipColor = subjectColours.getOrDefault(subjectId, Theme.ROW_A);
                    // Lighten the chip colour for the background
                    lbl.setBackground(lighten(chipColor, 0.75f));
                    lbl.setForeground(chipColor.darker().darker());
                }
                return lbl;
            }
        });

        // Header styling
        JTableHeader hdr = table.getTableHeader();
        hdr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        hdr.setBackground(Theme.PRIMARY);
        hdr.setForeground(Color.WHITE);
        hdr.setReorderingAllowed(false);
        hdr.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t,v,s,f,r,c);
                l.setBackground(Theme.PRIMARY); l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(SwingConstants.CENTER);
                l.setOpaque(true); return l;
            }
        });

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        for (int c = 1; c <= GeneticAlgorithm.PERIODS; c++)
            table.getColumnModel().getColumn(c).setPreferredWidth(145);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        wrapper.add(scroll, BorderLayout.CENTER);

        // Legend
        wrapper.add(buildLegend(entries), BorderLayout.SOUTH);

        return wrapper;
    }

    private JPanel buildLegend(List<TimetableEntry> entries) {
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        legend.setBackground(new Color(0xF5F5F5));
        legend.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JLabel heading = new JLabel("Legend: ");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 11));
        legend.add(heading);

        // Distinct subjects in this timetable
        entries.stream()
            .filter(e -> e.getSubjectName() != null)
            .collect(Collectors.toMap(
                TimetableEntry::getSubjectId,
                e -> e.getSubjectName() + " (" + e.getTeacherName() + ")",
                (a, b) -> a,
                java.util.LinkedHashMap::new))
            .forEach((id, label) -> {
                Color chip = subjectColours.getOrDefault(id, Color.GRAY);
                JLabel lbl = new JLabel("⬤ " + label);
                lbl.setFont(Theme.FONT_SMALL);
                lbl.setForeground(chip.darker());
                legend.add(lbl);
            });

        return legend;
    }

    // ── Load saved ────────────────────────────────────────

    private void loadSaved() {
        ClassSection cs = (ClassSection) classPicker.getSelectedItem();
        if (cs == null) return;
        if (!DAO.Timetable.existsForClass(cs.getId())) {
            JOptionPane.showMessageDialog(this,
                "No saved timetable found for " + cs.getName() + ".\nGenerate one first.",
                "Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<TimetableEntry> entries = DAO.Timetable.loadForClass(cs.getId());
        showTimetable(cs, entries);
    }

    // ── Helpers ───────────────────────────────────────────

    private void refreshClassPicker() {
        classPicker.removeAllItems();
        for (ClassSection cs : DAO.ClassSections.getAll()) {
            classPicker.addItem(cs);
        }
    }

    /** Assigns a stable colour to each subject ID. */
    private void assignSubjectColours() {
        List<Subject> subjects = DAO.Subjects.getAll();
        for (int i = 0; i < subjects.size(); i++) {
            subjectColours.put(subjects.get(i).getId(),
                Theme.SUBJECT_CHIPS[i % Theme.SUBJECT_CHIPS.length]);
        }
    }

    /** Lightens a colour by blending with white. factor 0=same, 1=white. */
    private Color lighten(Color c, float factor) {
        int r = (int)(c.getRed()   + (255 - c.getRed())   * factor);
        int g = (int)(c.getGreen() + (255 - c.getGreen()) * factor);
        int b = (int)(c.getBlue()  + (255 - c.getBlue())  * factor);
        return new Color(r, g, b);
    }
}
