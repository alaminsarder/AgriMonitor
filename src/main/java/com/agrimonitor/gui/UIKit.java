package com.agrimonitor.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

public final class UIKit {

    static final String FAMILY = "Segoe UI";
    static final Font BODY = new Font(FAMILY, Font.PLAIN, 13);
    static final Font BODY_BOLD = new Font(FAMILY, Font.BOLD, 13);
    static final Font LABEL = new Font(FAMILY, Font.PLAIN, 12);
    static final Font SMALL = new Font(FAMILY, Font.PLAIN, 12);
    static final Font HEADING = new Font(FAMILY, Font.BOLD, 15);

    private static final DecimalFormat NUM = new DecimalFormat("#,##0.##");
    private static final Color ZEBRA = new Color(250, 247, 240);
    private static final Color SELECT = new Color(222, 236, 228);

    private UIKit() {
    }

    static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "AgriMonitor", JOptionPane.INFORMATION_MESSAGE);
    }

    static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "AgriMonitor", JOptionPane.WARNING_MESSAGE);
    }

    static class CardPanel extends JPanel {
        private final Color accent;

        CardPanel(LayoutManager layout, int v, int h, Color accent) {
            super(layout);
            this.accent = accent;
            setOpaque(false);
            setBorder(new EmptyBorder(v + 4, h + 4, v + 5, h + 4));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            for (int i = 0; i < 4; i++) {
                g2.setColor(new Color(60, 50, 20, 7));
                g2.fillRoundRect(i, i + 1, w - 2 * i, h - 2 * i - 1, 20, 20);
            }
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(4, 3, w - 8, h - 9, 16, 16);
            g2.setColor(UITheme.BORDER_COLOR);
            g2.drawRoundRect(4, 3, w - 9, h - 10, 16, 16);
            if (accent != null) {
                g2.setColor(accent);
                g2.fillRoundRect(4, 3, 6, h - 9, 6, 6);
            }
            g2.dispose();
        }
    }

    static JPanel card(LayoutManager layout) {
        return new CardPanel(layout, 20, 24, null);
    }

    static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(HEADING);
        l.setForeground(UITheme.PRIMARY_DARK);
        l.setBorder(new CompoundBorder(new MatteBorder(0, 3, 0, 0, UITheme.ACCENT), new EmptyBorder(0, 10, 0, 0)));
        return l;
    }

    static JPanel statCard(String title, JLabel value, Color accent) {
        JLabel t = new JLabel(title);
        return statCard(t, value, accent, 22);
    }

    static JPanel statCard(JLabel title, JLabel value, Color accent, int valueSize) {
        CardPanel c = new CardPanel(new BorderLayout(0, 6), 14, 22, accent);
        title.setFont(LABEL);
        title.setForeground(UITheme.TEXT_LIGHT);
        value.setFont(new Font(FAMILY, Font.BOLD, valueSize));
        value.setForeground(UITheme.TEXT_DARK);
        c.add(title, BorderLayout.NORTH);
        c.add(value, BorderLayout.CENTER);
        return c;
    }

    static JPanel statRow(JComponent... cards) {
        JPanel p = new JPanel(new GridLayout(1, cards.length, 16, 0));
        p.setOpaque(false);
        for (JComponent c : cards) {
            p.add(c);
        }
        return p;
    }

    static JPanel stack(JComponent... parts) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        for (int i = 0; i < parts.length; i++) {
            parts[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(parts[i]);
            if (i < parts.length - 1) {
                p.add(Box.createVerticalStrut(16));
            }
        }
        return p;
    }

    static JPanel field(String label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(LABEL);
        l.setForeground(UITheme.TEXT_DARK);
        comp.setFont(BODY);
        Dimension d = comp.getPreferredSize();
        comp.setPreferredSize(new Dimension(d.width, Math.max(d.height, 34)));
        p.add(l, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    static JPanel formGrid(int rows, int cols, JComponent... items) {
        JPanel p = new JPanel(new GridLayout(rows, cols, 16, 12));
        p.setOpaque(false);
        for (JComponent c : items) {
            p.add(c);
        }
        return p;
    }

    static JPanel buttonBar(JComponent[] left, JComponent[] right) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        l.setOpaque(false);
        for (JComponent c : left) {
            c.setFont(BODY_BOLD);
            l.add(c);
        }
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        r.setOpaque(false);
        for (JComponent c : right) {
            c.setFont(BODY_BOLD);
            r.add(c);
        }
        bar.add(l, BorderLayout.WEST);
        bar.add(r, BorderLayout.EAST);
        return bar;
    }

    static JPanel formCard(String title, JComponent body, JComponent footer) {
        JPanel c = card(new BorderLayout(0, 14));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR), new EmptyBorder(0, 0, 10, 0)));
        head.add(heading(title), BorderLayout.WEST);
        c.add(head, BorderLayout.NORTH);
        c.add(body, BorderLayout.CENTER);
        if (footer != null) {
            c.add(footer, BorderLayout.SOUTH);
        }
        return c;
    }

    static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                for (int r = 0; r < getRowCount(); r++) {
                    Object v = getValueAt(r, column);
                    if (v != null) {
                        return v.getClass();
                    }
                }
                return Object.class;
            }
        };
    }

    static void styleTable(JTable t) {
        UITheme.styleTable(t);
        CellRenderer r = new CellRenderer(null, false);
        t.setDefaultRenderer(Object.class, r);
        t.setDefaultRenderer(Number.class, r);
        t.setDefaultRenderer(Double.class, r);
        t.setDefaultRenderer(Float.class, r);
        t.setFont(BODY);
        if (t.getTableHeader() != null) {
            t.getTableHeader().setFont(BODY_BOLD);
        }
        t.setRowHeight(34);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setGridColor(new Color(238, 233, 220));
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);
    }

    static void moneyColumn(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new CellRenderer(new DecimalFormat("#,##0.00"), false));
    }

    static void profitColumn(JTable t, int col) {
        t.getColumnModel().getColumn(col).setCellRenderer(new CellRenderer(new DecimalFormat("+#,##0.00;-#,##0.00"), true));
    }

    static JPanel tableCard(String title, JTable table, String hint, JComponent... actions) {
        styleTable(table);
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);

        SearchField search = new SearchField(hint == null ? "Search..." : hint);
        JLabel count = new JLabel("0 records");
        count.setFont(SMALL);
        count.setForeground(UITheme.TEXT_LIGHT);

        Runnable update = () -> {
            int n = table.getRowCount();
            count.setText(n + (n == 1 ? " record" : " records"));
        };
        sorter.addRowSorterListener(e -> update.run());

        Runnable filter = () -> {
            String q = search.getText().trim();
            if (q.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(q)));
            }
            update.run();
        };
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filter.run();
            }

            public void removeUpdate(DocumentEvent e) {
                filter.run();
            }

            public void changedUpdate(DocumentEvent e) {
                filter.run();
            }
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(count);
        right.add(search);
        for (JComponent a : actions) {
            a.setFont(BODY_BOLD);
            right.add(a);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading(title), BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        JScrollPane sp = new JScrollPane();
        sp.setViewport(new EmptyViewport(table));
        sp.setViewportView(table);
        styleScroll(sp);

        JPanel c = card(new BorderLayout(0, 14));
        c.add(top, BorderLayout.NORTH);
        c.add(sp, BorderLayout.CENTER);
        SwingUtilities.invokeLater(update);
        return c;
    }

    static JScrollPane styledScroll(Component view) {
        JScrollPane sp = new JScrollPane(view);
        styleScroll(sp);
        return sp;
    }

    static void styleScroll(JScrollPane sp) {
        sp.setBorder(new LineBorder(UITheme.BORDER_COLOR));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new SlimScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 10));
        sp.getVerticalScrollBar().setBackground(Color.WHITE);
        sp.getHorizontalScrollBar().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
    }

    static JComboBox<String> createComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setUI(new ComboUI());
        cb.setFont(BODY);
        cb.setBackground(Color.WHITE);
        cb.setForeground(UITheme.TEXT_DARK);
        cb.setBorder(new LineBorder(UITheme.BORDER_COLOR));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(BODY);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                setBackground(isSelected ? SELECT : Color.WHITE);
                setForeground(UITheme.TEXT_DARK);
                return this;
            }
        });
        return cb;
    }

    static class ComboUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton b = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(UITheme.PRIMARY);
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;
                    g2.fillPolygon(new int[]{cx - 4, cx + 4, cx}, new int[]{cy - 2, cy - 2, cy + 3}, 3);
                    g2.dispose();
                }
            };
            b.setBorder(BorderFactory.createEmptyBorder());
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            return b;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
            g.setColor(Color.WHITE);
            g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }

    static class SearchField extends JTextField {
        private final String hint;

        SearchField(String hint) {
            this.hint = hint;
            setOpaque(false);
            setFont(BODY);
            setForeground(UITheme.TEXT_DARK);
            setCaretColor(UITheme.PRIMARY);
            setBorder(new EmptyBorder(7, 36, 7, 14));
            setPreferredSize(new Dimension(230, 34));
            addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    repaint();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(ZEBRA);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(isFocusOwner() ? UITheme.ACCENT : UITheme.BORDER_COLOR);
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(UITheme.TEXT_LIGHT);
            g2.setStroke(new BasicStroke(1.6f));
            g2.drawOval(14, h / 2 - 6, 9, 9);
            g2.drawLine(22, h / 2 + 2, 26, h / 2 + 6);
            if (getText().isEmpty()) {
                g2.setFont(BODY);
                g2.setColor(new Color(160, 160, 150));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(hint, 36, (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class SlimScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            super.configureScrollBarColors();
            thumbColor = new Color(200, 192, 170);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return zero();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return zero();
        }

        private JButton zero() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            b.setMinimumSize(d);
            b.setMaximumSize(d);
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    static class CellRenderer extends DefaultTableCellRenderer {
        private final DecimalFormat fmt;
        private final boolean profit;

        CellRenderer(DecimalFormat fmt, boolean profit) {
            this.fmt = fmt;
            this.profit = profit;
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, false, false, r, c);
            if (v instanceof Number) {
                double d = ((Number) v).doubleValue();
                if (fmt != null) {
                    setText(fmt.format(d));
                } else if (v instanceof Double || v instanceof Float) {
                    setText(NUM.format(d));
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (profit) {
                    setForeground(d >= 0 ? UITheme.SUCCESS : UITheme.DANGER);
                    setFont(BODY_BOLD);
                } else {
                    setForeground(UITheme.TEXT_DARK);
                    setFont(BODY);
                }
            } else {
                setHorizontalAlignment(SwingConstants.LEFT);
                setForeground(UITheme.TEXT_DARK);
                setFont(BODY);
            }
            setBackground(sel ? SELECT : (r % 2 == 0 ? Color.WHITE : ZEBRA));
            setBorder(new EmptyBorder(0, 12, 0, 12));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            super.paintComponent(g);
        }
    }

    static class EmptyViewport extends JViewport {
        private final JTable table;

        EmptyViewport(JTable table) {
            this.table = table;
            setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (table.getRowCount() == 0) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(new Font(FAMILY, Font.PLAIN, 13));
                g2.setColor(new Color(165, 160, 145));
                String s = "No records found";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, getHeight() / 2 + fm.getAscent() / 2 + 10);
                g2.dispose();
            }
        }
    }
}
