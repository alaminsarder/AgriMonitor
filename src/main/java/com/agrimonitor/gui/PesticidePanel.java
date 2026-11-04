package com.agrimonitor.gui;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.dao.PesticideDAO;
import com.agrimonitor.model.Pesticide;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PesticidePanel extends JPanel {

    private static final Color SEL = new Color(222, 236, 228);
    private static final Color ZEBRA = new Color(250, 247, 240);
    private static final Color RUST = new Color(150, 78, 52);
    private static final Color[] PALETTE = {
            new Color(246, 196, 96),
            new Color(240, 128, 104),
            new Color(150, 208, 150),
            new Color(130, 190, 235),
            new Color(214, 160, 230),
            new Color(236, 160, 190)
    };

    private final PesticideDAO pesticideDAO = new PesticideDAO();
    private final FieldDAO fieldDAO = new FieldDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    private final JTextField fieldIdField = UITheme.createTextField();
    private final JTextField nameField = UITheme.createTextField();
    private final JTextField quantityField = UITheme.createTextField();
    private final JTextField dateField = UITheme.createTextField();

    private final DefaultTableModel model = UIKit.model("Field ID", "Pesticide Name", "Quantity", "Application Date");
    private final JTable table = new JTable(model);
    private final UsageCard insight = new UsageCard();

    private double maxQty = 0;

    public PesticidePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        JButton addBtn = UITheme.createStyledButton("Add Pesticide", UITheme.SUCCESS);
        JButton searchBtn = UITheme.createStyledButton("Search by Field", UITheme.INFO);
        JButton refreshBtn = UITheme.createStyledButton("Refresh", UITheme.TEXT_LIGHT);
        JButton clearBtn = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        JPanel form = UIKit.formCard("Pesticide Details",
                UIKit.formGrid(1, 4,
                        UIKit.field("Field ID", fieldIdField),
                        UIKit.field("Pesticide Name", nameField),
                        UIKit.field("Quantity", quantityField),
                        UIKit.field("Application Date (YYYY-MM-DD)", dateField)),
                UIKit.buttonBar(new JComponent[]{addBtn, searchBtn}, new JComponent[]{refreshBtn, clearBtn}));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        insight.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(insight);
        top.add(Box.createVerticalStrut(16));
        top.add(form);

        table.getColumnModel().getColumn(0).setCellRenderer(new ChipRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new QtyRenderer());

        add(top, BorderLayout.NORTH);
        add(UIKit.tableCard("Pesticide Records", table, "Search records..."), BorderLayout.CENTER);

        addBtn.addActionListener(e -> addPesticide());
        searchBtn.addActionListener(e -> searchByField());
        refreshBtn.addActionListener(e -> loadData());
        clearBtn.addActionListener(e -> clearForm());

        loadData();
    }

    private void addPesticide() {
        String fieldId = fieldIdField.getText().trim();
        String name = nameField.getText().trim();
        String qtyText = quantityField.getText().trim();
        String date = dateField.getText().trim();
        if (fieldId.isEmpty() || name.isEmpty() || qtyText.isEmpty() || date.isEmpty()) {
            UIKit.warn(this, "Please fill in all fields.");
            return;
        }
        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException ex) {
            UIKit.warn(this, "Quantity must be a number.");
            return;
        }
        if (!fieldDAO.fieldExists(fieldId)) {
            UIKit.warn(this, "Field ID not found.");
            return;
        }
        pesticideDAO.addPesticide(new Pesticide(fieldId, name, qty, date));
        UIKit.info(this, "Pesticide record added.");
        loadData();
        clearForm();
    }

    private void searchByField() {
        String fieldId = fieldIdField.getText().trim();
        if (fieldId.isEmpty()) {
            loadData();
            return;
        }
        fillTable(pesticideDAO.getPesticideByFieldId(fieldId));
    }

    private void loadData() {
        fillTable(pesticideDAO.getAllPesticides());
    }

    private void fillTable(List<Pesticide> list) {
        model.setRowCount(0);
        double max = 0;
        for (Pesticide p : list) {
            model.addRow(new Object[]{p.getFieldId(), p.getPesticideName(), p.getQuantity(), p.getApplicationDate()});
            max = Math.max(max, p.getQuantity());
        }
        maxQty = max;
        insight.setData(list);
        table.repaint();
    }

    private void clearForm() {
        fieldIdField.setText("");
        nameField.setText("");
        quantityField.setText("");
        dateField.setText("");
        table.clearSelection();
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static Color colorFor(String name) {
        int h = name == null ? 0 : Math.abs(name.toLowerCase().hashCode());
        return PALETTE[h % PALETTE.length];
    }

    private static String clip(String s, FontMetrics fm, int max) {
        if (fm.stringWidth(s) <= max) return s;
        String e = "...";
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n) + e) > max) n--;
        return s.substring(0, n) + e;
    }

    private static Path2D flask(double cx, double cy, double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx - s * 0.28, cy - s);
        p.lineTo(cx + s * 0.28, cy - s);
        p.lineTo(cx + s * 0.28, cy - s * 0.25);
        p.lineTo(cx + s * 0.9, cy + s * 0.8);
        p.quadTo(cx + s * 0.98, cy + s, cx + s * 0.75, cy + s);
        p.lineTo(cx - s * 0.75, cy + s);
        p.quadTo(cx - s * 0.98, cy + s, cx - s * 0.9, cy + s * 0.8);
        p.lineTo(cx - s * 0.28, cy - s * 0.25);
        p.closePath();
        return p;
    }

    private class QtyRenderer extends DefaultTableCellRenderer {
        private double frac;
        private boolean selected;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(t, value, false, false, row, column);
            double v = 0;
            if (value instanceof Number) {
                v = ((Number) value).doubleValue();
            } else if (value != null) {
                try {
                    v = Double.parseDouble(value.toString());
                } catch (NumberFormatException ignored) {
                }
            }
            setText(df.format(v));
            frac = maxQty > 0 ? Math.max(0, Math.min(1, v / maxQty)) : 0;
            selected = isSelected;
            setOpaque(false);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(0, 12, 0, 16));
            setFont(UIKit.BODY_BOLD);
            setForeground(UITheme.PRIMARY_DARK);
            setBackground(isSelected ? SEL : (row % 2 == 0 ? Color.WHITE : ZEBRA));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            if (frac > 0) {
                double bw = Math.max(10, (getWidth() - 16) * frac);
                double bh = getHeight() - 14;
                int alpha = selected ? 70 : 50;
                g.setPaint(new GradientPaint(8, 0, new Color(214, 120, 80, alpha / 3),
                        (float) (8 + bw), 0, new Color(214, 120, 80, alpha + 20)));
                g.fill(new RoundRectangle2D.Double(8, 7, bw, bh, 10, 10));
            }
            super.paintComponent(g);
            g.dispose();
        }
    }

    private static class ChipRenderer extends DefaultTableCellRenderer {
        private String text = "";
        private Color bg = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            text = value == null ? "" : value.toString();
            bg = isSelected ? SEL : (row % 2 == 0 ? Color.WHITE : ZEBRA);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String shown = clip(text, fm, getWidth() - 46);
            int pw = fm.stringWidth(shown) + 22;
            int ph = 24;
            int px = 12;
            int py = (getHeight() - ph) / 2;
            g.setColor(new Color(150, 78, 52, 26));
            g.fill(new RoundRectangle2D.Double(px, py, pw, ph, ph, ph));
            g.setColor(new Color(150, 78, 52, 90));
            g.draw(new RoundRectangle2D.Double(px + 0.5, py + 0.5, pw - 1, ph - 1, ph, ph));
            g.setColor(new Color(120, 60, 40));
            g.drawString(shown, px + 11, py + (ph - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    private static class NameRenderer extends DefaultTableCellRenderer {
        private String text = "";
        private Color bg = Color.WHITE;
        private Color dot = RUST;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            text = value == null ? "" : value.toString();
            bg = isSelected ? SEL : (row % 2 == 0 ? Color.WHITE : ZEBRA);
            dot = colorFor(text).darker();
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());
            int cy = getHeight() / 2;
            g.setColor(dot);
            g.fill(new Ellipse2D.Double(12, cy - 5, 10, 10));
            g.setFont(UIKit.BODY_BOLD);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(UITheme.TEXT_DARK);
            g.drawString(clip(text, fm, getWidth() - 44), 30, cy + (fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }

    private static class UsageCard extends JPanel {
        private static final int ROW0 = 44;
        private static final int STEP = 22;
        private static final int MAX_ROWS = 4;

        private final DecimalFormat f = new DecimalFormat("#,##0.##");
        private final List<String> names = new ArrayList<>();
        private final List<Double> vals = new ArrayList<>();
        private double total;
        private int count;
        private int types;
        private int hover = -1;
        private float progress = 1f;
        private final Timer anim;

        UsageCard() {
            setOpaque(false);
            setPreferredSize(new Dimension(10, 146));
            setMinimumSize(new Dimension(0, 146));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 146));
            anim = new Timer(16, e -> {
                progress += 0.045f;
                if (progress >= 1f) {
                    progress = 1f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int i = indexAt(e.getX(), e.getY());
                    if (i != hover) {
                        hover = i;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hover != -1) {
                        hover = -1;
                        repaint();
                    }
                }
            });
        }

        void setData(List<Pesticide> list) {
            names.clear();
            vals.clear();
            total = 0;
            count = list == null ? 0 : list.size();
            Map<String, Double> agg = new LinkedHashMap<>();
            if (list != null) {
                for (Pesticide p : list) {
                    total += p.getQuantity();
                    String n = String.valueOf(p.getPesticideName());
                    agg.merge(n, p.getQuantity(), Double::sum);
                }
            }
            types = agg.size();
            List<Map.Entry<String, Double>> entries = new ArrayList<>(agg.entrySet());
            entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            for (int i = 0; i < Math.min(MAX_ROWS, entries.size()); i++) {
                names.add(entries.get(i).getKey());
                vals.add(entries.get(i).getValue());
            }
            hover = -1;
            progress = 0f;
            anim.restart();
        }

        private int chartLeft() {
            return 340;
        }

        private int chartRight() {
            return getWidth() - 30;
        }

        private int indexAt(int mx, int my) {
            if (getWidth() < 560 || vals.isEmpty()) return -1;
            if (mx < chartLeft() || mx > chartRight()) return -1;
            int i = (my - ROW0) / STEP;
            if (my < ROW0 || i < 0 || i >= vals.size()) return -1;
            return i;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 22, 22);
            g.setPaint(new GradientPaint(0, 0, new Color(62, 34, 28), w, h, new Color(150, 78, 52)));
            g.fill(shape);
            g.setClip(shape);

            g.setColor(new Color(255, 255, 255, 12));
            g.fillOval(w - 170, -110, 280, 280);
            g.setColor(new Color(255, 255, 255, 8));
            g.fillOval(-70, h - 80, 240, 240);

            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(255, 255, 255, 10));
            for (int x = -h; x < w; x += 22) {
                g.drawLine(x, h, x + h, 0);
            }

            int lx = 30;
            g.setPaint(new GradientPaint(0, 20, new Color(250, 214, 140), 0, 42, new Color(226, 150, 90)));
            g.fill(flask(lx + 6, 31, 8));
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 11));
            g.setColor(new Color(240, 206, 140));
            g.drawString("TOTAL APPLIED", lx + 22, 36);

            String big = f.format(total);
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 38));
            g.setColor(Color.WHITE);
            g.drawString(big, lx, 86);
            int bigW = g.getFontMetrics().stringWidth(big);
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 14));
            g.setColor(new Color(240, 200, 160));
            g.drawString("units", lx + bigW + 8, 86);

            g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 13));
            g.setColor(new Color(244, 222, 204));
            String line = count + (count == 1 ? " application" : " applications") + "  |  " + types + (types == 1 ? " type" : " types");
            g.drawString(line, lx, 114);

            if (w >= 560) {
                int x0 = chartLeft();
                int x1 = chartRight();

                g.setColor(new Color(255, 255, 255, 36));
                g.drawLine(x0 - 22, 22, x0 - 22, h - 22);

                g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 11));
                g.setColor(new Color(240, 206, 140));
                g.drawString("TOP PESTICIDES BY QUANTITY", x0, 30);

                int n = vals.size();
                if (n == 0) {
                    g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 13));
                    g.setColor(new Color(244, 222, 204));
                    String msg = "No pesticide records yet";
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(msg, x0 + (x1 - x0 - fm.stringWidth(msg)) / 2, h / 2 + 8);
                } else {
                    double max = 0;
                    for (double v : vals) max = Math.max(max, v);
                    float eased = 1f - (float) Math.pow(1 - progress, 3);
                    int nameW = 120;
                    int valW = 64;
                    int bx0 = x0 + nameW + 10;
                    int bx1 = x1 - valW - 10;
                    for (int i = 0; i < n; i++) {
                        int base = ROW0 + i * STEP + 14;
                        boolean hot = i == hover;
                        Color c = colorFor(names.get(i));

                        if (hot) {
                            g.setColor(new Color(255, 255, 255, 22));
                            g.fill(new RoundRectangle2D.Double(x0 - 8, base - 15, x1 - x0 + 16, 21, 10, 10));
                        }

                        g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
                        FontMetrics fm = g.getFontMetrics();
                        g.setColor(hot ? Color.WHITE : new Color(250, 236, 224));
                        g.drawString(clip(names.get(i), fm, nameW), x0, base);

                        g.setColor(new Color(255, 255, 255, 30));
                        g.fill(new RoundRectangle2D.Double(bx0, base - 9, bx1 - bx0, 8, 8, 8));
                        double bw = Math.max(8, (bx1 - bx0) * (vals.get(i) / max) * eased);
                        g.setPaint(new GradientPaint(bx0, 0, new Color(c.getRed(), c.getGreen(), c.getBlue(), 150), (float) (bx0 + bw), 0, c));
                        g.fill(new RoundRectangle2D.Double(bx0, base - 9, bw, 8, 8, 8));

                        String val = hot && total > 0
                                ? String.format("%.1f%%", vals.get(i) / total * 100)
                                : f.format(vals.get(i));
                        g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
                        fm = g.getFontMetrics();
                        g.setColor(hot ? new Color(250, 214, 140) : new Color(244, 222, 204));
                        g.drawString(val, x1 - fm.stringWidth(val), base);
                    }
                }
            }
            g.dispose();
        }
    }
}