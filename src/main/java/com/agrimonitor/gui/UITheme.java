package com.agrimonitor.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class UITheme {

    // Premium palette: deep emerald + warm gold
    public static final Color PRIMARY_DARK = new Color(6, 78, 59);
    public static final Color PRIMARY = new Color(5, 150, 105);
    public static final Color PRIMARY_LIGHT = new Color(110, 231, 183);
    public static final Color ACCENT = new Color(245, 158, 11);
    public static final Color BACKGROUND = new Color(246, 248, 250);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_DARK = new Color(17, 24, 39);
    public static final Color TEXT_LIGHT = new Color(107, 114, 128);
    public static final Color SIDEBAR_BG = new Color(10, 20, 18);
    public static final Color SIDEBAR_HOVER = new Color(24, 40, 36);
    public static final Color SIDEBAR_SELECTED = new Color(5, 150, 105);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color INFO = new Color(37, 99, 235);
    public static final Color NEUTRAL = new Color(100, 116, 139);
    public static final Color BORDER_COLOR = new Color(229, 231, 235);

    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI Semibold", Font.PLAIN, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);

    private static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    // Card with layered soft shadow
    static class RoundedPanel extends JPanel {
        private int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int gap = 6;
            int w = getWidth() - gap;
            int h = getHeight() - gap;
            for (int i = gap; i >= 1; i--) {
                int alpha = 3 + (gap - i) * 1;
                g2.setColor(new Color(15, 23, 42, Math.max(2, 14 - i * 2)));
                g2.fillRoundRect(i, i, w, h, radius + 2, radius + 2);
            }
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, w, h, radius, radius);
            g2.setColor(BORDER_COLOR);
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel createCardPanel() {
        RoundedPanel panel = new RoundedPanel(18);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 30, 28));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    public static JComponent createCardTitle(String text) {
        JPanel container = new JPanel();
        container.setOpaque(false);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setAlignmentX(Component.LEFT_ALIGNMENT);
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel accent = new JLabel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                smooth(g2);
                g2.setPaint(new GradientPaint(0, 0, ACCENT, 0, getHeight(), PRIMARY));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.dispose();
            }
        };
        accent.setPreferredSize(new Dimension(5, 24));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 19));
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

    // Gradient button with smooth animated hover, shadow and pressed state
    static class StyledButton extends JButton {
        private Color baseColor;
        private boolean hover = false;
        private boolean pressed = false;
        private float hoverT = 0f;
        private final Timer anim;

        StyledButton(String text, Color bgColor) {
            super(text);
            this.baseColor = bgColor;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(BorderFactory.createEmptyBorder(11, 24, 13, 24));

            anim = new Timer(16, e -> {
                float target = hover ? 1f : 0f;
                hoverT += (target - hoverT) * 0.28f;
                if (Math.abs(target - hoverT) < 0.02f) {
                    hoverT = target;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    anim.start();
                }

                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    anim.start();
                }

                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        private Color mix(Color a, Color b, float t) {
            int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
            int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
            int bl = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
            return new Color(r, g, bl);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth();
            int h = getHeight() - 4;
            int lift = pressed ? 0 : Math.round(hoverT * 2);
            Color top = mix(baseColor, baseColor.brighter(), hoverT);
            Color bottom = mix(baseColor.darker(), baseColor, hoverT);
            if (pressed) {
                top = baseColor.darker();
                bottom = baseColor.darker();
            }
            int shadowAlpha = (int) (35 + 45 * hoverT);
            g2.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), shadowAlpha));
            g2.fillRoundRect(2, 3 - lift, w - 4, h, 12, 12);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRoundRect(0, 1 - lift, w, h, 12, 12);
            g2.translate(0, -lift);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static JButton createStyledButton(String text, Color bgColor) {
        return new StyledButton(text, bgColor);
    }

    // Rounded input fields painted by hand
    static class StyledTextField extends JTextField {
        private boolean focused = false;

        StyledTextField() {
            super();
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(TEXT_DARK);
            setCaretColor(PRIMARY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(focused ? Color.WHITE : new Color(249, 250, 251));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            if (focused) {
                g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 45));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            }
            g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
            g2.setColor(focused ? PRIMARY : new Color(209, 213, 219));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JTextField createTextField() {
        return new StyledTextField();
    }

    static class StyledPasswordField extends JPasswordField {
        private boolean focused = false;

        StyledPasswordField() {
            super();
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(TEXT_DARK);
            setCaretColor(PRIMARY);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) {
                    focused = true;
                    repaint();
                }

                public void focusLost(FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(focused ? Color.WHITE : new Color(249, 250, 251));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            if (focused) {
                g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 45));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
            }
            g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
            g2.setColor(focused ? PRIMARY : new Color(209, 213, 219));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPasswordField createPasswordField() {
        return new StyledPasswordField();
    }

    public static JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 12));
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
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setForeground(TEXT_DARK);
        table.setGridColor(new Color(243, 244, 246));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(209, 250, 229));
        table.setSelectionForeground(PRIMARY_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 42));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, column);
                l.setOpaque(true);
                l.setBackground(PRIMARY_DARK);
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return l;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(249, 250, 251));
                }
                return c;
            }
        });

        Container parent = table.getParent();
        if (parent instanceof JViewport && parent.getParent() instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) parent.getParent();
            sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            sp.getViewport().setBackground(CARD_BG);
        }
    }

    public static class SidebarButton extends JButton {
        private boolean selected = false;
        private boolean hover = false;

        public SidebarButton(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(new Color(209, 213, 219));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(14, 28, 14, 16));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
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
            setForeground(selected ? Color.WHITE : new Color(209, 213, 219));
            setFont(new Font("Segoe UI", selected ? Font.BOLD : Font.PLAIN, 14));
            repaint();
        }

        public boolean isSelectedButton() {
            return selected;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            if (selected) {
                g2.setPaint(new GradientPaint(0, 0, SIDEBAR_SELECTED, getWidth(), 0, new Color(4, 120, 87)));
                g2.fillRoundRect(10, 3, getWidth() - 20, getHeight() - 6, 12, 12);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 10, 4, getHeight() - 20, 4, 4);
            } else if (hover) {
                g2.setColor(SIDEBAR_HOVER);
                g2.fillRoundRect(10, 3, getWidth() - 20, getHeight() - 6, 12, 12);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static SidebarButton createSidebarButton(String text) {
        return new SidebarButton(text);
    }
}