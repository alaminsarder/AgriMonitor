package com.agrimonitor.gui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

public class UITheme {

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

    public static class Icons {

        private Icons() {
        }

        public static String keyFor(String text) {
            String t = text == null ? "" : text.toLowerCase();
            if (t.contains("logout") || t.contains("log out")) return "logout";
            if (t.contains("home")) return "home";
            if (t.contains("field")) return "field";
            if (t.contains("crop")) return "crop";
            if (t.contains("irrigation")) return "water";
            if (t.contains("pesticide")) return "pesticide";
            if (t.contains("cost")) return "cost";
            if (t.contains("sales")) return "sales";
            if (t.contains("profit")) return "profit";
            if (t.contains("weather")) return "weather";
            if (t.contains("assistant") || t.contains("chat")) return "chat";
            if (t.contains("report")) return "report";
            if (t.contains("profile")) return "profile";
            if (t.contains("search")) return "search";
            if (t.contains("bell")) return "bell";
            return "dot";
        }

        private static Path2D poly(boolean close, double... p) {
            Path2D path = new Path2D.Double();
            path.moveTo(p[0], p[1]);
            for (int i = 2; i + 1 < p.length; i += 2) {
                path.lineTo(p[i], p[i + 1]);
            }
            if (close) {
                path.closePath();
            }
            return path;
        }

        public static void paint(Graphics2D g0, String key, double x, double y, double size, Color color) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.translate(x, y);
            double s = size / 20.0;
            g.scale(s, s);
            g.setColor(color);
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (key) {
                case "home": {
                    g.draw(poly(false, 2.5, 10, 10, 3.5, 17.5, 10));
                    g.draw(poly(false, 4.5, 8.5, 4.5, 16.5, 15.5, 16.5, 15.5, 8.5));
                    g.draw(poly(false, 8, 16.5, 8, 12, 12, 12, 12, 16.5));
                    break;
                }
                case "field": {
                    g.draw(new RoundRectangle2D.Double(3, 3, 14, 14, 4, 4));
                    g.draw(new Line2D.Double(3, 10, 17, 10));
                    g.draw(new Line2D.Double(10, 3, 10, 17));
                    break;
                }
                case "crop": {
                    g.draw(new Line2D.Double(10, 17.5, 10, 8));
                    Path2D left = new Path2D.Double();
                    left.moveTo(10, 12);
                    left.quadTo(3.5, 12, 3.5, 5.5);
                    left.quadTo(10, 5.5, 10, 12);
                    g.draw(left);
                    Path2D right = new Path2D.Double();
                    right.moveTo(10, 9);
                    right.quadTo(10, 3, 16.5, 3);
                    right.quadTo(16.5, 9, 10, 9);
                    g.draw(right);
                    break;
                }
                case "water": {
                    Path2D drop = new Path2D.Double();
                    drop.moveTo(10, 2.5);
                    drop.curveTo(10, 2.5, 4, 9, 4, 12);
                    drop.curveTo(4, 15.5, 6.7, 18, 10, 18);
                    drop.curveTo(13.3, 18, 16, 15.5, 16, 12);
                    drop.curveTo(16, 9, 10, 2.5, 10, 2.5);
                    drop.closePath();
                    g.draw(drop);
                    break;
                }
                case "pesticide": {
                    g.draw(poly(false, 8, 3, 8, 8.5, 3.8, 16, 4.5, 17.5, 15.5, 17.5, 16.2, 16, 12, 8.5, 12, 3));
                    g.draw(new Line2D.Double(7, 3, 13, 3));
                    g.draw(new Line2D.Double(6, 13.5, 14, 13.5));
                    break;
                }
                case "cost": {
                    g.draw(new Ellipse2D.Double(3, 3, 14, 14));
                    g.draw(new Line2D.Double(7.5, 8, 12.5, 8));
                    g.draw(new Line2D.Double(7.5, 11.5, 12.5, 11.5));
                    break;
                }
                case "sales": {
                    g.draw(new RoundRectangle2D.Double(4, 7, 12, 10.5, 4, 4));
                    g.draw(new Arc2D.Double(7, 3, 6, 8, 0, 180, Arc2D.OPEN));
                    break;
                }
                case "profit": {
                    g.draw(poly(false, 2.5, 14.5, 7.5, 9.5, 11, 12.5, 17.5, 5.5));
                    g.draw(poly(false, 13, 5.5, 17.5, 5.5, 17.5, 10));
                    break;
                }
                case "weather": {
                    g.draw(new Ellipse2D.Double(6, 6, 8, 8));
                    for (int k = 0; k < 8; k++) {
                        double a = k * Math.PI / 4;
                        g.draw(new Line2D.Double(10 + Math.cos(a) * 6, 10 + Math.sin(a) * 6,
                                10 + Math.cos(a) * 8.5, 10 + Math.sin(a) * 8.5));
                    }
                    break;
                }
                case "chat": {
                    g.draw(new RoundRectangle2D.Double(2.5, 3, 15, 11, 5, 5));
                    g.draw(poly(false, 6.5, 14, 6, 18, 10.5, 14));
                    break;
                }
                case "report": {
                    g.draw(poly(true, 5, 2.5, 12, 2.5, 16, 6.5, 16, 17.5, 5, 17.5));
                    g.draw(new Line2D.Double(8, 10.5, 13, 10.5));
                    g.draw(new Line2D.Double(8, 13.5, 13, 13.5));
                    break;
                }
                case "profile": {
                    g.draw(new Ellipse2D.Double(6.5, 3, 7, 7));
                    g.draw(new Arc2D.Double(3.5, 11.5, 13, 11, 0, 180, Arc2D.OPEN));
                    break;
                }
                case "logout": {
                    g.draw(poly(false, 8.5, 3, 4, 3, 4, 17, 8.5, 17));
                    g.draw(new Line2D.Double(8, 10, 17, 10));
                    g.draw(poly(false, 13.5, 6.5, 17, 10, 13.5, 13.5));
                    break;
                }
                case "search": {
                    g.draw(new Ellipse2D.Double(3, 3, 10, 10));
                    g.draw(new Line2D.Double(11, 11, 17, 17));
                    break;
                }
                case "bell": {
                    Path2D bell = new Path2D.Double();
                    bell.moveTo(4.5, 14.5);
                    bell.curveTo(5.5, 13, 6, 11.5, 6, 9);
                    bell.curveTo(6, 6, 7.5, 4.5, 10, 4.5);
                    bell.curveTo(12.5, 4.5, 14, 6, 14, 9);
                    bell.curveTo(14, 11.5, 14.5, 13, 15.5, 14.5);
                    bell.closePath();
                    g.draw(bell);
                    g.draw(new Line2D.Double(10, 2.5, 10, 4.5));
                    g.draw(new Arc2D.Double(8, 15.5, 4, 3, 180, 180, Arc2D.OPEN));
                    break;
                }
                default: {
                    g.fill(new Ellipse2D.Double(7, 7, 6, 6));
                    break;
                }
            }
            g.dispose();
        }
    }

    public static JComponent createAvatar(String name, int size) {
        String t = name == null ? "" : name.trim();
        String initial = t.isEmpty() ? "F" : t.substring(0, 1).toUpperCase();
        JComponent c = new JComponent() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                smooth(g);
                int w = getWidth();
                int h = getHeight();
                g.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 160));
                g.fillOval(0, 0, w, h);
                g.setPaint(new GradientPaint(0, 0, PRIMARY_LIGHT, w, h, PRIMARY));
                g.fillOval(3, 3, w - 6, h - 6);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, (int) (w * 0.4))));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(initial, (w - fm.stringWidth(initial)) / 2f,
                        (h - fm.getHeight()) / 2f + fm.getAscent());
                g.dispose();
            }
        };
        Dimension d = new Dimension(size, size);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        return c;
    }

    public static JButton createBellButton() {
        JButton b = new JButton() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                smooth(g);
                int w = getWidth();
                int h = getHeight();
                g.setColor(Color.WHITE);
                g.fillOval(0, 0, w - 1, h - 1);
                g.setColor(BORDER_COLOR);
                g.drawOval(0, 0, w - 1, h - 1);
                Icons.paint(g, "bell", w / 2.0 - 9, h / 2.0 - 9, 18, PRIMARY_DARK);
                g.setColor(ACCENT);
                g.fillOval(w - 14, 8, 8, 8);
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(w - 14, 8, 8, 8);
                g.dispose();
            }
        };
        b.setPreferredSize(new Dimension(42, 42));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

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
                g2.setColor(new Color(15, 23, 42, Math.max(2, 12 - i * 2)));
                g2.fillRoundRect(i, i, w, h, radius + 2, radius + 2);
            }
            g2.setColor(CARD_BG);
            g2.fillRoundRect(0, 0, w, h, radius, radius);
            g2.setColor(new Color(236, 240, 238));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JPanel createCardPanel() {
        RoundedPanel panel = new RoundedPanel(22);
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
        container.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

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
        container.add(titleRow);

        return container;
    }

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
            g2.fillRoundRect(2, 3 - lift, w - 4, h, 14, 14);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRoundRect(0, 1 - lift, w, h, 14, 14);
            g2.translate(0, -lift);
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static JButton createStyledButton(String text, Color bgColor) {
        return new StyledButton(text, bgColor);
    }

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
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            if (focused) {
                g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 45));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            }
            g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
            g2.setColor(focused ? PRIMARY : new Color(209, 213, 219));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
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
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            if (focused) {
                g2.setColor(new Color(PRIMARY.getRed(), PRIMARY.getGreen(), PRIMARY.getBlue(), 45));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            }
            g2.setStroke(new BasicStroke(focused ? 1.6f : 1f));
            g2.setColor(focused ? PRIMARY : new Color(209, 213, 219));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
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
        table.setGridColor(new Color(240, 244, 241));
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(209, 250, 229));
        table.setSelectionForeground(PRIMARY_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 44));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, false, false, row, column);
                l.setOpaque(true);
                l.setBackground(new Color(236, 247, 242));
                l.setForeground(PRIMARY_DARK);
                l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                l.setHorizontalAlignment(SwingConstants.LEFT);
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(214, 232, 224)),
                        BorderFactory.createEmptyBorder(0, 14, 0, 14)));
                return l;
            }
        });

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? CARD_BG : new Color(248, 251, 249));
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
        private final String label;
        private final String iconKey;

        public SidebarButton(String text) {
            super(text);
            String clean = text == null ? "" : text.replaceAll("^[^\\p{L}]+", "").trim();
            this.label = clean.isEmpty() ? (text == null ? "" : text) : clean;
            this.iconKey = Icons.keyFor(this.label);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setForeground(new Color(209, 213, 219));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(14, 60, 14, 16));
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
            int w = getWidth();
            int h = getHeight();
            if (selected) {
                g2.setPaint(new GradientPaint(0, 0, SIDEBAR_SELECTED, w, 0, new Color(4, 120, 87)));
                g2.fillRoundRect(12, 3, w - 24, h - 6, 18, 18);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 12, 4, h - 24, 4, 4);
            } else if (hover) {
                g2.setColor(SIDEBAR_HOVER);
                g2.fillRoundRect(12, 3, w - 24, h - 6, 18, 18);
            }
            Color ic = selected ? Color.WHITE : new Color(150, 168, 160);
            Icons.paint(g2, iconKey, 30, (h - 19) / 2.0, 19, ic);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, 62, (h - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    public static SidebarButton createSidebarButton(String text) {
        return new SidebarButton(text);
    }
}