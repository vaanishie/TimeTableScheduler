package ui;

import db.DAO;
import model.Teacher;
import model.Subject;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * TeacherPanel.java — CRUD interface for the teachers table.
 *
 * Layout:
 *  Left  — JTable listing all teachers
 *  Right — Form to add / edit a teacher + subject assignment checkboxes
 */
public class TeacherPanel extends JPanel {

    private JTable           table;
    private DefaultTableModel tableModel;

    // Form fields
    private JTextField  tfName;
    private JTextField  tfEmail;
    private JTextField  tfSpec;
    private JPanel      subjectCheckPanel;
    private List<Subject> allSubjects;
    private List<JCheckBox> subjectCheckBoxes = new java.util.ArrayList<>();

    private Teacher selectedTeacher; // currently selected for editing

    public TeacherPanel() {
        setLayout(new BorderLayout(12, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildFormPanel(),  BorderLayout.EAST);

        refreshTable();
    }

    // ── Left: teacher list table ──────────────────────────

    private JPanel buildTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Theme.BG);

        JLabel heading = Theme.makeSectionLabel("👩‍🏫  Teacher List");
        panel.add(heading, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
            new Object[]{"ID", "Name", "Email", "Specialization"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);

        // Row selection → populate form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromSelection();
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        panel.add(scroll, BorderLayout.CENTER);

        // Delete button below table
        JButton deleteBtn = Theme.makeBtn("🗑  Delete Selected", Theme.DANGER);
        deleteBtn.addActionListener(e -> deleteSelected());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.setBackground(Theme.BG);
        btnRow.add(deleteBtn);
        panel.add(btnRow, BorderLayout.SOUTH);

        return panel;
    }

    // ── Right: add/edit form ──────────────────────────────

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setPreferredSize(new Dimension(280, 0));

        JPanel card = Theme.makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Title
        JLabel formTitle = Theme.makeSectionLabel("Add / Edit Teacher");
        formTitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(formTitle);
        card.add(Box.createVerticalStrut(14));

        // Name
        card.add(makeFieldLabel("Full Name"));
        tfName = makeTextField();
        card.add(tfName);
        card.add(Box.createVerticalStrut(10));

        // Email
        card.add(makeFieldLabel("Email"));
        tfEmail = makeTextField();
        card.add(tfEmail);
        card.add(Box.createVerticalStrut(10));

        // Specialization
        card.add(makeFieldLabel("Specialization"));
        tfSpec = makeTextField();
        card.add(tfSpec);
        card.add(Box.createVerticalStrut(14));

        // Subject assignment checkboxes
        card.add(makeFieldLabel("Can Teach Subjects:"));
        card.add(Box.createVerticalStrut(4));
        subjectCheckPanel = new JPanel();
        subjectCheckPanel.setLayout(new BoxLayout(subjectCheckPanel, BoxLayout.Y_AXIS));
        subjectCheckPanel.setBackground(Color.WHITE);
        subjectCheckPanel.setAlignmentX(LEFT_ALIGNMENT);
        rebuildSubjectCheckboxes();
        JScrollPane subScroll = new JScrollPane(subjectCheckPanel);
        subScroll.setPreferredSize(new Dimension(0, 130));
        subScroll.setAlignmentX(LEFT_ALIGNMENT);
        subScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        card.add(subScroll);
        card.add(Box.createVerticalStrut(14));

        // Buttons
        JButton saveBtn  = Theme.makeBtn("💾  Save",  Theme.PRIMARY_MED);
        JButton clearBtn = Theme.makeBtn("✖  Clear", Theme.TEXT_DIM);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        clearBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> save());
        clearBtn.addActionListener(e -> clearForm());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setBackground(Color.WHITE);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);
        btnRow.add(saveBtn);
        btnRow.add(clearBtn);
        card.add(btnRow);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ── Actions ───────────────────────────────────────────

    private void save() {
        String name  = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String spec  = tfSpec.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (selectedTeacher == null) {
            // Insert
            Teacher t = new Teacher(0, name, email, spec);
            int id = DAO.Teachers.insert(t);
            t.setId(id);
            saveSubjectAssignments(id);
        } else {
            // Update
            selectedTeacher.setName(name);
            selectedTeacher.setEmail(email);
            selectedTeacher.setSpecialization(spec);
            DAO.Teachers.update(selectedTeacher);
            saveSubjectAssignments(selectedTeacher.getId());
        }

        clearForm();
        refreshTable();
    }

    private void saveSubjectAssignments(int teacherId) {
        List<Integer> selected = new java.util.ArrayList<>();
        for (int i = 0; i < subjectCheckBoxes.size(); i++) {
            if (subjectCheckBoxes.get(i).isSelected()) {
                selected.add(allSubjects.get(i).getId());
            }
        }
        DAO.Teachers.setSubjects(teacherId, selected);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this teacher? This also removes their subject assignments.",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DAO.Teachers.delete(id);
            clearForm();
            refreshTable();
        }
    }

    private void populateFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) { clearForm(); return; }

        int id = (int) tableModel.getValueAt(row, 0);
        List<Teacher> all = DAO.Teachers.getAll();
        selectedTeacher = all.stream().filter(t -> t.getId() == id).findFirst().orElse(null);
        if (selectedTeacher == null) return;

        tfName.setText(selectedTeacher.getName());
        tfEmail.setText(selectedTeacher.getEmail() != null ? selectedTeacher.getEmail() : "");
        tfSpec.setText(selectedTeacher.getSpecialization() != null ? selectedTeacher.getSpecialization() : "");

        // Tick subject checkboxes
        List<Integer> assignedIds = DAO.Teachers.getTeacherIdsForSubject(0); // placeholder
        // Get subjects assigned to THIS teacher
        List<Integer> mySubjectIds = new java.util.ArrayList<>();
        for (Subject s : allSubjects) {
            List<Integer> tIds = DAO.Teachers.getTeacherIdsForSubject(s.getId());
            if (tIds.contains(id)) mySubjectIds.add(s.getId());
        }
        for (int i = 0; i < allSubjects.size(); i++) {
            subjectCheckBoxes.get(i).setSelected(mySubjectIds.contains(allSubjects.get(i).getId()));
        }
    }

    private void clearForm() {
        selectedTeacher = null;
        tfName.setText("");
        tfEmail.setText("");
        tfSpec.setText("");
        subjectCheckBoxes.forEach(cb -> cb.setSelected(false));
        table.clearSelection();
    }

    // ── Data refresh ──────────────────────────────────────

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Teacher t : DAO.Teachers.getAll()) {
            tableModel.addRow(new Object[]{
                t.getId(), t.getName(), t.getEmail(), t.getSpecialization()
            });
        }
        rebuildSubjectCheckboxes();
    }

    private void rebuildSubjectCheckboxes() {
        if (subjectCheckPanel == null) return;
        allSubjects = DAO.Subjects.getAll();
        subjectCheckPanel.removeAll();
        subjectCheckBoxes.clear();
        for (Subject s : allSubjects) {
            JCheckBox cb = new JCheckBox(s.getCode() + " – " + s.getName());
            cb.setBackground(Color.WHITE);
            cb.setFont(Theme.FONT_SMALL);
            subjectCheckPanel.add(cb);
            subjectCheckBoxes.add(cb);
        }
        subjectCheckPanel.revalidate();
        subjectCheckPanel.repaint();
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
        t.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        t.getColumnModel().getColumn(0).setMaxWidth(40);
    }

    private JLabel makeFieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(Theme.TEXT_DIM);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField makeTextField() {
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
