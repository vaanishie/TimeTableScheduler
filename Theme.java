package ui;

import java.awt.*;

/**
 * Theme.java — Central colour palette and font constants.
 * All UI panels reference this class so changing a colour
 * here updates the entire application.
 */
public class Theme {

    // ── Primary palette ───────────────────────────────────
    public static final Color PRIMARY      = new Color(0x1A237E); // deep indigo
    public static final Color PRIMARY_MED  = new Color(0x3F51B5);
    public static final Color PRIMARY_LITE = new Color(0xC5CAE9);
    public static final Color ACCENT       = new Color(0xFF6F00); // amber

    // ── Semantic colours ──────────────────────────────────
    public static final Color SUCCESS      = new Color(0x2E7D32);
    public static final Color WARNING      = new Color(0xE65100);
    public static final Color DANGER       = new Color(0xB71C1C);
    public static final Color INFO         = new Color(0x01579B);

    // ── Neutrals ──────────────────────────────────────────
    public static final Color BG           = new Color(0xF0F2FF); // app background
    public static final Color CARD         = Color.WHITE;
    public static final Color BORDER       = new Color(0xDDE1F0);
    public static final Color TEXT         = new Color(0x212121);
    public static final Color TEXT_DIM     = new Color(0x757575);

    // ── Table row colours ─────────────────────────────────
    public static final Color ROW_A        = new Color(0xEEF2FF);
    public static final Color ROW_B        = Color.WHITE;
    public static final Color ROW_SEL      = new Color(0xBBDEFB);

    // ── Subject colour chips (12 distinct) ────────────────
    public static final Color[] SUBJECT_CHIPS = {
        new Color(0x4DB6AC), new Color(0xFF8A65), new Color(0x9575CD),
        new Color(0x4FC3F7), new Color(0xAED581), new Color(0xFFD54F),
        new Color(0xF48FB1), new Color(0x80DEEA), new Color(0xBCAAA4),
        new Color(0xA5D6A7), new Color(0xCE93D8), new Color(0x90CAF9)
    };

    // ── Fonts ─────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font FONT_HEADING  = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);

    // ── Factory methods ───────────────────────────────────

    /** Creates a styled action button. */
    public static javax.swing.JButton makeBtn(String label, Color bg) {
        javax.swing.JButton btn = new javax.swing.JButton(label);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bg;
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(original.darker()); }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(original); }
        });
        return btn;
    }

    /** Creates a card panel with a white background and subtle shadow border. */
    public static javax.swing.JPanel makeCard() {
        javax.swing.JPanel card = new javax.swing.JPanel();
        card.setBackground(CARD);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(BORDER, 1, true),
            javax.swing.BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return card;
    }

    /** Section header label */
    public static javax.swing.JLabel makeSectionLabel(String text) {
        javax.swing.JLabel lbl = new javax.swing.JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(PRIMARY);
        return lbl;
    }
}
