package com.agrimonitor.gui;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.dao.IrrigationDAO;
import com.agrimonitor.model.Irrigation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class IrrigationPanel extends JPanel {

    private static final Color SEL = new Color(222, 236, 228);
    private static final Color ZEBRA = new Color(250, 247, 240);
    private static final Color BLUE_DARK = new Color(20, 96, 134);

    private final IrrigationDAO irrigationDAO = new IrrigationDAO();
    private final FieldDAO fieldDAO = new FieldDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    private final JTextField fieldIdField = UITheme.createTextField();
    private final JTextField dateField = UITheme.createTextField();
    private final JTextField waterField = UITheme.createTextField();

    private final DefaultTableModel model = UIKit.model("Field ID", "Irrigation Date", "Water Quantity");
    private final JTable table = new JTable(model);
    private final WaterCard insight = new WaterCard();

    private double maxWater = 0;

    public IrrigationPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        JButton addBtn = UITheme.createStyledButton("Add Irrigation", UITheme.SUCCESS);
        JButton searchBtn = UITheme.createStyledButton("Search by Field", UITheme.INFO);
        JButton refreshBtn = UITheme.createStyledButton("Refresh", UITheme.TEXT_LIGHT);
        JButton clearBtn = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);
        addBtn.setFont(UIKit.BODY_BOLD);
        searchBtn.setFont(UIKit.BODY_BOLD);
        refreshBtn.setFont(UIKit.BODY_BOLD);
        clearBtn.setFont(UIKit.BODY_BOLD);

        JPanel fields = UIKit.stack(
                UIKit.field("Field ID", fieldIdField),
                UIKit.field("Irrigation Date (YYYY-MM-DD)", dateField),
                UIKit.field("Water Quantity (L)", waterField));

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setOpaque(false);
        body.add(fields, BorderLayout.NORTH);
        body.add(new TipPanel(), BorderLayout.CENTER);

        JPanel pair = new JPanel(new GridLayout(1, 2, 10, 0));
        pair.setOpaque(false);
        pair.add(refreshBtn);
        pair.add(clearBtn);

        JPanel foot = new JPanel(new GridLayout(0, 1, 0, 10));
        foot.setOpaque(false);
        foot.add(addBtn);
        foot.add(searchBtn);
        foot.add(pair);

        JPanel form = UIKit.formCard("Irrigation Details", body, foot);
        form.setPreferredSize(new Dimension(318, 420));

        table.getColumnModel().getColumn(0).setCellRenderer(new ChipRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new WaterRenderer());

        JPanel lower = new JPanel(new BorderLayout(18, 0));
        lower.setOpaque(false);
        lower.add(form, BorderLayout.WEST);
        lower.add(UIKit.tableCard("Irrigation Records", table, "Search records..."), BorderLayout.CENTER);

        add(insight, BorderLayout.NORTH);
        add(lower, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addIrrigation());
        searchBtn.addActionListener(e -> searchByField());
        refreshBtn.addActionListener(e -> loadData());
        clearBtn.addActionListener(e -> clearForm());

        loadData();
    }

    private void addIrrigation() {
        String fieldId = fieldIdField.getText().trim();
        String date = dateField.getText().trim();
        String waterText = waterField.getText().trim();
        if (fieldId.isEmpty() || date.isEmpty() || waterText.isEmpty()) {
            UIKit.warn(this, "Please fill in all fields.");
            return;
        }
        double water;
        try {
            water = Double.parseDouble(waterText);
        } catch (NumberFormatException ex) {
            UIKit.warn(this, "Water quantity must be a number.");
            return;
        }
        if (!fieldDAO.fieldExists(fieldId)) {
            UIKit.warn(this, "Field ID not found.");
            return;
        }
        irrigationDAO.addIrrigation(new Irrigation(fieldId, date, water));
        UIKit.info(this, "Irrigation record added.");
        loadData();
        clearForm();
    }

    private void searchByField() {
        String fieldId = fieldIdField.getText().trim();
        if (fieldId.isEmpty()) {
            loadData();
            return;
        }
        fillTable(irrigationDAO.getIrrigationByFieldId(fieldId));
    }

    private void loadData() {
        fillTable(irrigationDAO.getAllIrrigations());
    }

    private void fillTable(List<Irrigation> list) {
        model.setRowCount(0);
        double max = 0;
        for (Irrigation i : list) {
            model.addRow(new Object[]{i.getFieldId(), i.getIrrigationDate(), i.getWaterQuantity()});
            max = Math.max(max, i.getWaterQuantity());
        }
        maxWater = max;
        insight.setData(list);
        table.repaint();
    }

    private void clearForm() {
        fieldIdField.setText("");
        dateField.setText("");
        waterField.setText("");
        table.clearSelection();
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static Path2D droplet(double cx, double cy, double s) {
        Path2D p = new Path2D.Double();
        p.moveTo(cx, cy - s);
        p.curveTo(cx + s * 0.15, cy - s * 0.55, cx + s * 0.8, cy - s * 0.05, cx + s * 0.8, cy + s * 0.35);
        p.curveTo(cx + s * 0.8, cy + s * 0.85, cx + s * 0.42, cy + s, cx, cy + s);
        p.curveTo(cx - s * 0.42, cy + s, cx - s * 0.8, cy + s * 0.85, cx - s * 0.8, cy + s * 0.35);
        p.curveTo(cx - s * 0.8, cy - s * 0.05, cx - s * 0.15, cy - s * 0.55, cx, cy - s);
        p.closePath();
        return p;
    }

    private static String clip(String s, FontMetrics fm, int max) {
        if (fm.stringWidth(s) <= max) return s;
        String e = "...";
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n) + e) > max) n--;
        return s.substring(0, n) + e;
    }

    private class WaterRenderer extends DefaultTableCellRenderer {
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
            setText(df.format(v) + " L");
            frac = maxWater > 0 ? Math.max(0, Math.min(1, v / maxWater)) : 0;
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
                int alpha = selected ? 70 : 48;
                g.setPaint(new GradientPaint(8, 0, new Color(60, 170, 215, alpha / 3),
                        (float) (8 + bw), 0, new Color(60, 170, 215, alpha + 20)));
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
            g.setColor(new Color(36, 122, 160, 28));
            g.fill(new RoundRectangle2D.Double(px, py, pw, ph, ph, ph));
            g.setColor(new Color(36, 122, 160, 90));
            g.draw(new RoundRectangle2D.Double(px + 0.5, py + 0.5, pw - 1, ph - 1, ph, ph));
            g.setColor(BLUE_DARK);
            g.drawString(shown, px + 11, py + (ph - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    private static class TipPanel extends JComponent {
        TipPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            int w = getWidth();
            int h = getHeight();
            if (h < 70) return;
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int bh = Math.min(h, 92);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w - 1, bh - 1, 16, 16);
            g.setColor(new Color(234, 245, 250));
            g.fill(shape);
            g.setColor(new Color(198, 224, 236));
            g.draw(shape);

            double cy = bh / 2.0;
            g.setPaint(new GradientPaint(0, (float) (cy - 12), new Color(150, 214, 242), 0, (float) (cy + 12), new Color(46, 150, 206)));
            g.fill(droplet(26, cy, 12));

            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
            g.setColor(BLUE_DARK);
            g.drawString("Quick tip", 50, (int) cy - 8);
            g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 12));
            g.setColor(UITheme.TEXT_LIGHT);
            g.drawString("Enter a Field ID, then press", 50, (int) cy + 9);
            g.drawString("Search by Field to filter.", 50, (int) cy + 26);
            g.dispose();
        }
    }

    private static class WaterCard extends JPanel {
        private static final int MAX_BARS = 14;

        private final DecimalFormat f = new DecimalFormat("#,##0.##");
        private final List<String> ids = new ArrayList<>();
        private final List<String> dates = new ArrayList<>();
        private final List<Double> vals = new ArrayList<>();
        private double total;
        private double peak;
        private int count;
        private int hover = -1;
        private float progress = 1f;
        private final Timer anim;

        WaterCard() {
            setOpaque(false);
            setPreferredSize(new Dimension(10, 168));
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
                    int i = indexAt(e.getX());
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

        void setData(List<Irrigation> list) {
            ids.clear();
            dates.clear();
            vals.clear();
            total = 0;
            peak = 0;
            count = list == null ? 0 : list.size();
            if (list != null) {
                for (Irrigation i : list) {
                    total += i.getWaterQuantity();
                    peak = Math.max(peak, i.getWaterQuantity());
                }
                int start = Math.max(0, list.size() - MAX_BARS);
                for (int k = start; k < list.size(); k++) {
                    Irrigation i = list.get(k);
                    ids.add(String.valueOf(i.getFieldId()));
                    dates.add(String.valueOf(i.getIrrigationDate()));
                    vals.add(i.getWaterQuantity());
                }
            }
            hover = -1;
            progress = 0f;
            anim.restart();
        }

        private int chartLeft() {
            return 300;
        }

        private int chartRight() {
            return getWidth() - 28;
        }

        private double slot() {
            return (chartRight() - chartLeft()) / (double) Math.max(vals.size(), 8);
        }

        private int indexAt(int mx) {
            int n = vals.size();
            if (n == 0 || getWidth() < 520) return -1;
            double s = slot();
            if (mx < chartLeft() || mx >= chartLeft() + s * n) return -1;
            return (int) ((mx - chartLeft()) / s);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 22, 22);
            g.setPaint(new GradientPaint(0, 0, new Color(13, 56, 82), w, h, new Color(24, 116, 146)));
            g.fill(shape);
            g.setClip(shape);

            g.setColor(new Color(255, 255, 255, 12));
            g.fillOval(w - 180, -100, 270, 270);
            g.setColor(new Color(255, 255, 255, 8));
            g.fillOval(-60, h - 70, 220, 220);

            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int k = 0; k < 2; k++) {
                Path2D wave = new Path2D.Double();
                double base = h - 12 - k * 8;
                for (int x = 0; x <= w + 4; x += 4) {
                    double y = base + Math.sin(x / (110.0 + k * 40) * Math.PI * 2 + k * 1.3) * 3.5;
                    if (x == 0) wave.moveTo(x, y);
                    else wave.lineTo(x, y);
                }
                g.setColor(new Color(255, 255, 255, 34 - k * 14));
                g.draw(wave);
            }

            int lx = 30;
            g.setPaint(new GradientPaint(0, 24, new Color(190, 236, 255), 0, 40, new Color(70, 170, 225)));
            g.fill(droplet(lx + 5, 31, 7));
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 11));
            g.setColor(new Color(223, 196, 120));
            g.drawString("TOTAL WATER USED", lx + 20, 36);

            String big = f.format(total);
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 38));
            g.setColor(Color.WHITE);
            g.drawString(big, lx, 86);
            int bigW = g.getFontMetrics().stringWidth(big);
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 18));
            g.setColor(new Color(170, 224, 244));
            g.drawString("L", lx + bigW + 6, 86);

            g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 13));
            g.setColor(new Color(196, 226, 236));
            g.drawString(count + (count == 1 ? " record" : " records"), lx, 112);
            String avg = count == 0 ? "No data yet" : "Average " + f.format(total / count) + " L per record";
            g.drawString(avg, lx, 132);

            if (w >= 520) {
                int x0 = chartLeft();
                int x1 = chartRight();
                int top = 50;
                int base = h - 36;
                int plotH = base - top;

                g.setColor(new Color(255, 255, 255, 36));
                g.drawLine(x0 - 22, 24, x0 - 22, h - 24);

                g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 11));
                g.setColor(new Color(223, 196, 120));
                g.drawString("RECENT RECORDS", x0, 36);

                String right;
                if (hover >= 0 && hover < vals.size()) {
                    right = ids.get(hover) + "  -  " + dates.get(hover) + "  -  " + f.format(vals.get(hover)) + " L";
                    g.setColor(Color.WHITE);
                } else {
                    right = "Peak " + f.format(peak) + " L";
                    g.setColor(new Color(170, 214, 232));
                }
                g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
                FontMetrics rfm = g.getFontMetrics();
                g.drawString(right, x1 - rfm.stringWidth(right), 36);

                g.setStroke(new BasicStroke(1f));
                for (int i = 0; i <= 3; i++) {
                    int gy = top + plotH * i / 3;
                    g.setColor(new Color(255, 255, 255, i == 3 ? 70 : 20));
                    g.drawLine(x0, gy, x1, gy);
                }

                int n = vals.size();
                if (n == 0) {
                    g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 13));
                    g.setColor(new Color(170, 214, 232));
                    String msg = "No irrigation records yet";
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(msg, x0 + (x1 - x0 - fm.stringWidth(msg)) / 2, top + plotH / 2 + 4);
                } else {
                    double max = 0;
                    for (double v : vals) max = Math.max(max, v);
                    float eased = 1f - (float) Math.pow(1 - progress, 3);
                    double s = slot();
                    double bw = Math.min(32, s * 0.58);
                    for (int i = 0; i < n; i++) {
                        double v = vals.get(i);
                        double bh = max > 0 ? Math.max(4, plotH * (v / max) * eased) : 4;
                        double bx = x0 + i * s + (s - bw) / 2;
                        double by = base - bh;
                        boolean hot = i == hover;
                        Color c1 = hot ? new Color(246, 224, 150) : new Color(150, 220, 246);
                        Color c2 = hot ? new Color(214, 166, 62) : new Color(46, 150, 206);
                        g.setPaint(new GradientPaint(0, (float) by, c1, 0, (float) base, c2));
                        g.fill(new RoundRectangle2D.Double(bx, by, bw, bh, 8, 8));
                    }
                }
            }
            g.dispose();
        }
    }
}