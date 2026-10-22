package com.agrimonitor.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class UITheme {

    public static final Color PRIMARY_DARK = new Color(15, 76, 92);
    public static final Color PRIMARY = new Color(13, 148, 136);
    public static final Color PRIMARY_LIGHT = new Color(94, 234, 212);
    public static final Color ACCENT = new Color(251, 146, 60);
    public static final Color BACKGROUND = new Color(241, 245, 249);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(30, 41, 59);
    public static final Color TEXT_LIGHT = new Color(100, 116, 139);
    public static final Color SIDEBAR_BG = new Color(17, 24, 39);
    public static final Color SIDEBAR_HOVER = new Color(31, 41, 55);
    public static final Color SIDEBAR_SELECTED = new Color(13, 148, 136);
    public static final Color DANGER = new Color(220, 53, 69);
    public static final Color SUCCESS = new Color(25, 135, 84);
    public static final Color INFO = new Color(37, 99, 235);
    public static final Color NEUTRAL = new Color(100, 116, 139);
    public static final Color BORDER_COLOR = new Color(226, 232, 240);

    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    static class RoundedPanel extends JPanel {
        private int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int gap = 4;
            g2.setColor(new Color(15, 23, 42, 18));
            g2.fillRoundRect(gap, gap, getWidth() - gap, getHeight() - gap, radius, radius);
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, getWidth() - gap, getHeight() - gap, radius, radius);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(0, 0, getWidth() - gap - 1, getHeight() - gap - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel createCardPanel() {
        RoundedPanel panel = new RoundedPanel(16);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    public static JComponent createCardTitle(String text) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel accent = new JLabel();
        accent.setOpaque(true);
        accent.setBackground(PRIMARY);
        accent.setPreferredSize(new Dimension(5, 22));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(TEXT_DARK);

        titleRow.add(accent);
        titleRow.add(label);

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_COLOR);
        sep.setBackground(BORDER_COLOR);
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        container.add(titleRow);
        container.add(Box.createRigidArea(new Dimension(0, 12)));
        container.add(sep);

        return container;
    }

    static class StyledButton extends JButton {
        private Color baseColor;
        private Color hoverColor;
        private boolean hover = false;

        StyledButton(String text, Color bgColor) {
            super(text);
            this.baseColor = bgColor;
            this.hoverColor = bgColor.brighter();
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hover ? hoverColor : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JButton createStyledButton(String text, Color bgColor) {
        return new StyledButton(text, bgColor);
    }

    static class StyledTextField extends JTextField {
        StyledTextField() {
            super();
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setBackground(Color.WHITE);
            applyIdleBorder();
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(PRIMARY, 2, true),
                            BorderFactory.createEmptyBorder(7, 9, 7, 9)));
                }

                public void focusLost(FocusEvent e) {
                    applyIdleBorder();
                }
            });
        }

        private void applyIdleBorder() {
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        }
    }

    public static JTextField createTextField() {
        return new StyledTextField();
    }

    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(TEXT_LIGHT);
        return label;
    }

    public static JPanel labeledField(String labelText, JComponent field) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel label = createFormLabel(labelText);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(field);
        return panel;
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(204, 251, 241));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
        header.setBorder(BorderFactory.createEmptyBorder());
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(248, 250, 252));
                }
                return c;
            }
        });
    }

    public static class SidebarButton extends JButton {
        private boolean selected = false;
        private boolean hover = false;

        public SidebarButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        public boolean isSelectedButton() {
            return selected;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (selected) {
                g2.setColor(SIDEBAR_SELECTED);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(ACCENT);
                g2.fillRect(0, 0, 4, getHeight());
            } else if (hover) {
                g2.setColor(SIDEBAR_HOVER);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static SidebarButton createSidebarButton(String text) {
        return new SidebarButton(text);
    }
}