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
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.ButtonModel;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
        return new StyledButton(label, primary);
    }

    public static JButton navigationButton(String label) {
        StyledButton button = new StyledButton(label, false);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        button.setPreferredSize(new Dimension(168, 44));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return button;
    }

    public static void selectNavigation(JButton button, boolean selected) {
        if (button instanceof StyledButton styled) {
            styled.setPrimary(selected);
        }
    }

    private static final class StyledButton extends JButton {
        private boolean primary;

        StyledButton(String label, boolean primary) {
            super(label);
            setUI(new BasicButtonUI());
            setPrimary(primary);
            setFocusPainted(false);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setRolloverEnabled(true);
            setFont(getFont().deriveFont(Font.BOLD, 12f));
            setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
            setPreferredSize(new Dimension(Math.max(95, getPreferredSize().width), 38));
            getAccessibleContext().setAccessibleDescription(label);
        }

        void setPrimary(boolean primary) {
            this.primary = primary;
            setForeground(primary ? Color.WHITE : INK);
            setBackground(primary ? ACCENT : new Color(231, 238, 244));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics original) {
            Graphics2D graphics = (Graphics2D) original.create();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            ButtonModel model = getModel();
            Color fill = isEnabled() ? getBackground() : new Color(229, 232, 235);
            if (isEnabled() && model.isPressed()) {
                fill = primary ? new Color(20, 56, 88) : new Color(211, 221, 230);
            } else if (isEnabled() && model.isRollover()) {
                fill = primary ? new Color(41, 93, 135) : new Color(218, 228, 237);
            }
            graphics.setColor(fill);
            graphics.fillRoundRect(0, 0, getWidth(), getHeight(), 9, 9);
            if (isFocusOwner()) {
                graphics.setColor(new Color(81, 137, 191));
                graphics.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 8, 8);
            }
            graphics.dispose();
            setForeground(!isEnabled() ? MUTED : primary ? Color.WHITE : INK);
            super.paintComponent(original);
        }
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
