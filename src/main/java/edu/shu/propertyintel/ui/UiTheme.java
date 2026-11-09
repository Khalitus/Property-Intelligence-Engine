package edu.shu.propertyintel.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

public final class UiTheme {
    public static final Color BACKGROUND = new Color(246, 248, 250);
    public static final Color SURFACE = Color.WHITE;
    public static final Color INK = new Color(32, 43, 58);
    public static final Color MUTED = new Color(98, 112, 129);
    public static final Color ACCENT = new Color(31, 73, 111);
    public static final Color BORDER = new Color(221, 227, 233);
    public static final Color ERROR = new Color(169, 46, 46);

    private UiTheme() {
    }

    public static void install() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // The cross-platform look and feel remains usable.
        }
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        label.setForeground(INK);
        return label;
    }

    public static JLabel section(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 15f));
        label.setForeground(INK);
        return label;
    }

    public static JLabel muted(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel surface() {
        JPanel panel = new JPanel();
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)));
        return panel;
    }

    public static JButton button(String label, boolean primary) {
        JButton button = new JButton(label);
        button.setFocusPainted(false);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 12f));
        button.setMargin(new Insets(8, 14, 8, 14));
        if (primary) {
            button.setOpaque(true);
            button.setBackground(ACCENT);
            button.setForeground(Color.WHITE);
        }
        return button;
    }

    public static void field(JTextField field, String hint) {
        field.setToolTipText(hint);
        field.getAccessibleContext().setAccessibleDescription(hint);
        field.setColumns(10);
    }

    public static void combo(JComboBox<?> combo, String hint) {
        combo.setToolTipText(hint);
        combo.getAccessibleContext().setAccessibleDescription(hint);
    }

    public static JScrollPane tableScroll(JTable table) {
        table.setRowHeight(28);
        table.setSelectionBackground(new Color(220, 234, 246));
        table.setSelectionForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(BORDER);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new StripedRenderer());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        return scroll;
    }

    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    public static void spacing(JComponent component, int top, int left, int bottom, int right) {
        component.setBorder(padding(top, left, bottom, right));
    }

    private static final class StripedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                        boolean selected, boolean focused, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, focused, row, column);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            setForeground(INK);
            if (!selected) {
                setBackground(row % 2 == 0 ? SURFACE : BACKGROUND);
            }
            return this;
        }
    }
}
