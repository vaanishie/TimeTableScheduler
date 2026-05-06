package ui;

import db.DAO;
import model.Subject;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * SubjectPanel.java — CRUD interface for the subjects table.
 *
 * Layout:
 *  Left  — JTable listing all subjects
 *  Right — Form to add / edit a subject
 */
public class SubjectPanel extends JPanel {

    private JTable            table;
    private DefaultTableModel tableModel;
    private JTextField        tfName, tfCode;
    private JSpinner          spHours;
    private Subject           selected;

    public SubjectPanel() {
        setLayout(new BorderLayout(12, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        add(buildTable(), BorderLayout.CENTER);
        add(buildForm(),  BorderLayout.EAST);
        refresh();
    }

    // ── Left: subject list table ──────────────────────────

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(Theme.BG);
        p.add(Theme.makeSectionLabel("📚  Subject List"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Code", "Hrs/Week"}, 0) {
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

        JLabel title = Theme.makeSectionLabel("Add / Edit Subject");
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(14));

        card.add(lbl("Subject Name"));
        tfName = field();
        card.add(tfName);
        card.add(Box.createVerticalStrut(10));

        card.add(lbl("Subject Code  (e.g. CS301)"));
        tfCode = field();
        card.add(tfCode);
        card.add(Box.createVerticalStrut(10));

        card.add(lbl("Hours Per Week  (used by GA)"));
        spHours = new JSpinner(new SpinnerNumberModel(4, 1, 10, 1));
        spHours.setFont(Theme.FONT_BODY);
        spHours.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        spHours.setAlignmentX(LEFT_ALIGNMENT);
        card.add(spHours);
        card.add(Box.createVerticalStrut(6));

        // Hours hint
        JLabel hint = new JLabel("  ↑ GA tries to schedule this many periods/week");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hint.setForeground(Theme.TEXT_DIM);
        hint.setAlignmentX(LEFT_ALIGNMENT);
        card.add(hint);
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
        String name = tfName.getText().trim();
        String code = tfCode.getText().trim();
        int    hrs  = (Integer) spHours.getValue();

        if (name.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Name and Code are required.", "Validation",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selected == null) {
            DAO.Subjects.insert(new Subject(0, name, code, hrs));
        } else {
            selected.setName(name);
            selected.setCode(code);
            selected.setHoursPerWeek(hrs);
            DAO.Subjects.update(selected);
        }
        clear();
        refresh();
    }

    private void delete() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this subject?\nThis will also remove teacher assignments for it.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DAO.Subjects.delete(id);
            clear();
            refresh();
        }
    }

    private void populate() {
        int row = table.getSelectedRow();
        if (row < 0) { clear(); return; }

        int id = (int) tableModel.getValueAt(row, 0);
        selected = DAO.Subjects.getAll().stream()
            .filter(s -> s.getId() == id)
            .findFirst()
            .orElse(null);

        if (selected == null) return;
        tfName.setText(selected.getName());
        tfCode.setText(selected.getCode());
        spHours.setValue(selected.getHoursPerWeek());
    }

    private void clear() {
        selected = null;
        tfName.setText("");
        tfCode.setText("");
        spHours.setValue(4);
        table.clearSelection();
    }

    private void refresh() {
        tableModel.setRowCount(0);
        for (Subject s : DAO.Subjects.getAll()) {
            tableModel.addRow(new Object[]{
                s.getId(), s.getName(), s.getCode(), s.getHoursPerWeek()
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
        t.getColumnModel().getColumn(3).setMaxWidth(80);
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
