package ui;

import db.DAO;
import model.ClassSection;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/**
 * ClassPanel.java — CRUD interface for the class_sections table.
 *
 * Layout:
 *  Left  — JTable listing all class sections
 *  Right — Form to add / edit a class section
 */
public class ClassPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        tfName, tfBranch;
    private JSpinner          spYear, spStrength;
    private ClassSection      selected;

    public ClassPanel() {
        setLayout(new BorderLayout(12, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(),  BorderLayout.EAST);
        refresh();
    }

    // ── Left: class list table ────────────────────────────

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG);
        p.add(Theme.makeSectionLabel("🏫  Class Sections"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Year", "Branch", "Strength"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populate();
        });

        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        p.add(sc, BorderLayout.CENTER);

        JButton del = Theme.makeBtn("🗑  Delete Selected", Theme.DANGER);
        del.addActionListener(e -> delete());
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(Theme.BG);
        row.add(del);
        p.add(row, BorderLayout.SOUTH);
        return p;
    }

    // ── Right: add / edit form ────────────────────────────

    private JPanel buildForm() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setPreferredSize(new Dimension(250, 0));
        outer.setBackground(Theme.BG);

        JPanel card = Theme.makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = Theme.makeSectionLabel("Add / Edit Class");
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        card.add(lbl("Section Name  (e.g. 11-A)"));
        tfName = field();
        card.add(tfName);
        card.add(Box.createVerticalStrut(10));

        card.add(lbl("Branch / Stream  (e.g. CS, Science)"));
        tfBranch = field();
        card.add(tfBranch);
        card.add(Box.createVerticalStrut(10));

        card.add(lbl("Year  (1 = First Year, 2 = Second, …)"));
        spYear = new JSpinner(new SpinnerNumberModel(1, 1, 6, 1));
        spYear.setFont(Theme.FONT_BODY);
        spYear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        spYear.setAlignmentX(LEFT_ALIGNMENT);
        card.add(spYear);
        card.add(Box.createVerticalStrut(10));

        card.add(lbl("Student Strength"));
        spStrength = new JSpinner(new SpinnerNumberModel(60, 1, 300, 1));
        spStrength.setFont(Theme.FONT_BODY);
        spStrength.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        spStrength.setAlignmentX(LEFT_ALIGNMENT);
        card.add(spStrength);
        card.add(Box.createVerticalStrut(18));

        JButton save  = Theme.makeBtn("💾  Save",  Theme.PRIMARY_MED);
        JButton clear = Theme.makeBtn("✖  Clear", Theme.TEXT_DIM);
        save.addActionListener(e  -> save());
        clear.addActionListener(e -> clear());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.add(save);
        btnRow.add(clear);
        card.add(btnRow);

        outer.add(card, BorderLayout.NORTH);
        return outer;
    }

    // ── Actions ───────────────────────────────────────────

    private void save() {
        String name   = tfName.getText().trim();
        String branch = tfBranch.getText().trim();
        int    year   = (Integer) spYear.getValue();
        int    str    = (Integer) spStrength.getValue();

        if (name.isEmpty() || branch.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Section Name and Branch are required.", "Validation",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected == null) {
            DAO.ClassSections.insert(new ClassSection(0, name, year, branch, str));
        } else {
            selected.setName(name);
            selected.setBranch(branch);
            selected.setYear(year);
            selected.setStrength(str);
            DAO.ClassSections.update(selected);
        }
        clear();
        refresh();
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this class section?\nThis will also delete any saved timetable for it.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DAO.ClassSections.delete(id);
            clear();
            refresh();
        }
    }

    private void populate() {
        int row = table.getSelectedRow();
        if (row < 0) { clear(); return; }

        int id = (int) tableModel.getValueAt(row, 0);
        selected = DAO.ClassSections.getAll().stream()
            .filter(cs -> cs.getId() == id)
            .findFirst()
            .orElse(null);

        if (selected == null) return;
        tfName.setText(selected.getName());
        tfBranch.setText(selected.getBranch());
        spYear.setValue(selected.getYear());
        spStrength.setValue(selected.getStrength());
    }

    private void clear() {
        selected = null;
        tfName.setText("");
        tfBranch.setText("");
        spYear.setValue(1);
        spStrength.setValue(60);
        table.clearSelection();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (ClassSection cs : DAO.ClassSections.getAll()) {
            tableModel.addRow(new Object[]{
                cs.getId(), cs.getName(), cs.getYear(),
                cs.getBranch(), cs.getStrength()
            });
        }
    }

    // ── Helpers ───────────────────────────────────────────

    private void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setFont(Theme.FONT_BODY);
        t.setGridColor(Theme.BORDER);
        t.setShowGrid(true);
        t.setSelectionBackground(Theme.ROW_SEL);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(Theme.PRIMARY);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setReorderingAllowed(false);
        t.getColumnModel().getColumn(0).setMaxWidth(40);
        t.getColumnModel().getColumn(2).setMaxWidth(50);
        t.getColumnModel().getColumn(4).setMaxWidth(80);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Theme.TEXT_DIM);
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JTextField field() {
        JTextField tf = new JTextField();
        tf.setFont(Theme.FONT_BODY);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        tf.setAlignmentX(LEFT_ALIGNMENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return tf;
    }
}
