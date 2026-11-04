package com.agrimonitor.gui;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Crop;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CropPanel extends JPanel {

    private static final Color O_TOP = new Color(104, 112, 30);
    private static final Color O_BOTTOM = new Color(28, 34, 14);
    private static final Color LIME = new Color(212, 232, 120);
    private static final Color MUTED = new Color(176, 188, 140);
    private static final Color INK = new Color(52, 58, 24);

    private static final Color[] BRIGHT = {
            new Color(212, 232, 120), new Color(240, 196, 64), new Color(150, 206, 110),
            new Color(232, 160, 70), new Color(120, 196, 170)
    };
    private static final Color[] DEEP = {
            new Color(120, 140, 40), new Color(160, 120, 20), new Color(60, 120, 50),
            new Color(160, 90, 30), new Color(40, 110, 90)
    };

    private final CropDAO cropDAO = new CropDAO();
    private final FieldDAO fieldDAO = new FieldDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    private final JTextField tfId = UITheme.createTextField();
    private final JTextField tfName = UITheme.createTextField();
    private final JTextField tfType = UITheme.createTextField();
    private final JTextField tfDuration = UITheme.createTextField();
    private final JTextField tfField = UITheme.createTextField();

    private final DefaultTableModel model;
    private final JTable table;
    private final GrowCard card;
    private int maxDuration = 1;

    public CropPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        card = new GrowCard(this::selectById);
        add(card, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Crop ID", "Crop Name", "Type", "Growth Duration", "Field ID"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        UIKit.styleTable(table);
        table.setRowHeight(42);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {64, 120, 100, 150, 64};
        Cell[] cells = {new IdCell(), new NameCell(), new TypeCell(), new DurationCell(), new FieldCell()};
        for (int i = 0; i < cells.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(cells[i]);
        }

        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int r = table.getSelectedRow();
            if (r < 0) return;
            int m = table.convertRowIndexToModel(r);
            tfId.setText(str(model.getValueAt(m, 0)));
            tfName.setText(str(model.getValueAt(m, 1)));
            tfType.setText(str(model.getValueAt(m, 2)));
            tfDuration.setText(str(model.getValueAt(m, 3)));
            tfField.setText(str(model.getValueAt(m, 4)));
        });

        JComponent tableCard = UIKit.tableCard("All Crops", table, "Click a row to edit");

        JButton addBtn = UITheme.createStyledButton("Add Crop", UITheme.PRIMARY);
        JButton updBtn = UITheme.createStyledButton("Update", UITheme.INFO);
        JButton refBtn = UITheme.createStyledButton("Refresh", UITheme.TEXT_LIGHT);
        JButton clrBtn = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        addBtn.addActionListener(e -> addCrop());
        updBtn.addActionListener(e -> updateCrop());
        refBtn.addActionListener(e -> loadData());
        clrBtn.addActionListener(e -> clearForm());

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridy = 0;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 10, 6);
        body.add(UIKit.field("Crop ID", tfId), gc);
        gc.gridx = 1;
        gc.insets = new Insets(0, 6, 10, 0);
        body.add(UIKit.field("Field ID", tfField), gc);
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(0, 0, 10, 0);
        gc.gridy = 1;
        body.add(UIKit.field("Crop Name", tfName), gc);
        gc.gridy = 2;
        body.add(UIKit.field("Crop Type", tfType), gc);
        gc.gridy = 3;
        gc.insets = new Insets(0, 0, 0, 0);
        body.add(UIKit.field("Growth Duration (days)", tfDuration), gc);

        JPanel footer = new JPanel(new GridLayout(2, 2, 8, 8));
        footer.setOpaque(false);
        footer.add(addBtn);
        footer.add(updBtn);
        footer.add(refBtn);
        footer.add(clrBtn);

        JComponent form = UIKit.formCard("Crop Details", body, footer);

        TipPanel tip = new TipPanel();
        tip.setPreferredSize(new Dimension(100, 86));

        JPanel tipWrap = new JPanel(new BorderLayout());
        tipWrap.setOpaque(false);
        tipWrap.add(tip, BorderLayout.NORTH);

        ScrollBody column = new ScrollBody(new BorderLayout(0, 14));
        column.setOpaque(false);
        column.add(form, BorderLayout.NORTH);
        column.add(tipWrap, BorderLayout.CENTER);

        JScrollPane side = new JScrollPane(column, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        side.setBorder(null);
        side.setOpaque(false);
        side.getViewport().setOpaque(false);
        side.getVerticalScrollBar().setUnitIncrement(16);
        side.setPreferredSize(new Dimension(318, 10));

        JPanel middle = new JPanel(new BorderLayout(16, 0));
        middle.setOpaque(false);
        middle.add(side, BorderLayout.WEST);
        middle.add(tableCard, BorderLayout.CENTER);
        add(middle, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadData();
            }
        });

        loadData();
    }

    private static String str(Object o) {
        return o == null ? "" : o.toString();
    }

    private static Font f(int style, int size) {
        return new Font(UIKit.FAMILY, style, size);
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static String fit(String s, FontMetrics fm, int w) {
        if (s == null) return "";
        if (fm.stringWidth(s) <= w) return s;
        String e = "...";
        while (s.length() > 1 && fm.stringWidth(s + e) > w) s = s.substring(0, s.length() - 1);
        return s + e;
    }

    private static double ease(double p) {
        p = Math.max(0, Math.min(1, p));
        return 1 - Math.pow(1 - p, 3);
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "AgriMonitor", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "AgriMonitor", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        tfId.setText("");
        tfName.setText("");
        tfType.setText("");
        tfDuration.setText("");
        tfField.setText("");
        table.clearSelection();
    }

    private void selectById(String id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (str(model.getValueAt(i, 0)).equals(id)) {
                int v = table.convertRowIndexToView(i);
                if (v < 0) return;
                table.setRowSelectionInterval(v, v);
                table.scrollRectToVisible(table.getCellRect(v, 0, true));
                return;
            }
        }
    }

    private void loadData() {
        try {
            List<Crop> list = cropDAO.getAllCrops();
            model.setRowCount(0);
            int max = 1;
            for (Crop c : list) {
                model.addRow(new Object[]{c.getCropId(), c.getCropName(), c.getCropType(),
                        c.getGrowthDuration(), c.getFieldId()});
                max = Math.max(max, c.getGrowthDuration());
            }
            maxDuration = max;
            card.setData(list, df);
            table.repaint();
        } catch (Exception ex) {
            warn("Could not load crops: " + ex.getMessage());
        }
    }

    private Crop readCrop() {
        String id = tfId.getText().trim();
        String name = tfName.getText().trim();
        String type = tfType.getText().trim();
        String dur = tfDuration.getText().trim();
        String fid = tfField.getText().trim();

        if (id.isEmpty() || name.isEmpty() || type.isEmpty() || dur.isEmpty() || fid.isEmpty()) {
            warn("All fields are required.");
            return null;
        }
        int days;
        try {
            days = Integer.parseInt(dur);
        } catch (NumberFormatException ex) {
            warn("Growth duration must be a whole number.");
            return null;
        }
        if (days <= 0) {
            warn("Growth duration must be greater than zero.");
            return null;
        }
        if (!fieldDAO.fieldExists(fid)) {
            warn("Field ID does not exist.");
            return null;
        }
        return new Crop(id, name, type, days, fid);
    }

    private void addCrop() {
        Crop crop = readCrop();
        if (crop == null) return;
        try {
            if (cropDAO.cropExists(crop.getCropId())) {
                warn("This Crop ID already exists.");
                return;
            }
            cropDAO.addCrop(crop);
            info("Crop added successfully.");
            clearForm();
            loadData();
        } catch (Exception ex) {
            warn("Could not add crop: " + ex.getMessage());
        }
    }

    private void updateCrop() {
        Crop crop = readCrop();
        if (crop == null) return;
        try {
            if (!cropDAO.cropExists(crop.getCropId())) {
                warn("Crop not found. Use Add Crop for a new one.");
                return;
            }
            cropDAO.updateCrop(crop.getCropId(), crop.getCropName(), crop.getCropType(), crop.getGrowthDuration());
            info("Crop updated successfully.");
            loadData();
        } catch (Exception ex) {
            warn("Could not update crop: " + ex.getMessage());
        }
    }

    private static class ScrollBody extends JPanel implements Scrollable {
        ScrollBody(LayoutManager lm) {
            super(lm);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle r, int o, int d) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle r, int o, int d) {
            return Math.max(16, r.height - 24);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    private static class TipPanel extends JComponent {
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            g.setColor(new Color(243, 245, 226));
            g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g.setColor(new Color(214, 220, 178));
            g.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

            g.setColor(O_TOP);
            g.fillRoundRect(0, 14, 4, h - 28, 4, 4);

            float s = 18f;
            float x = 20f;
            float y = 18f;
            Path2D.Float leaf = new Path2D.Float();
            leaf.moveTo(x, y + s);
            leaf.curveTo(x, y + s * 0.2f, x + s * 0.3f, y, x + s, y);
            leaf.curveTo(x + s, y + s * 0.7f, x + s * 0.8f, y + s, x, y + s);
            leaf.closePath();
            g.setColor(new Color(124, 142, 40));
            g.fill(leaf);

            g.setFont(f(Font.BOLD, 13));
            g.setColor(INK);
            g.drawString("Quick tip", 48, 32);

            g.setFont(f(Font.PLAIN, 12));
            g.setColor(new Color(96, 104, 52));
            FontMetrics fm = g.getFontMetrics();
            int maxW = w - 48 - 14;
            g.drawString(fit("Click a table row or a timeline bar", fm, maxW), 48, 52);
            g.drawString(fit("to load that crop into the form.", fm, maxW), 48, 70);
            g.dispose();
        }
    }

    private static class GrowCard extends JPanel {
        private final Consumer<String> onPick;
        private final javax.swing.Timer timer;
        private double progress = 1;
        private int total;
        private int fieldsInUse;
        private int maxD = 120;
        private String avgText = "0 d";
        private String longestName = "-";
        private int longestDays;
        private List<Crop> top = new ArrayList<>();
        private Rectangle[] rows = new Rectangle[0];
        private int hover = -1;

        GrowCard(Consumer<String> onPick) {
            this.onPick = onPick;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 172));
            setMinimumSize(new Dimension(100, 172));

            timer = new javax.swing.Timer(16, e -> {
                progress += 0.035;
                if (progress >= 1) {
                    progress = 1;
                    ((javax.swing.Timer) e.getSource()).stop();
                }
                repaint();
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = -1;
                    for (int i = 0; i < rows.length; i++) {
                        if (rows[i] != null && rows[i].contains(e.getPoint())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx != hover) {
                        hover = idx;
                        setCursor(idx >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (hover >= 0 && hover < top.size()) {
                        GrowCard.this.onPick.accept(String.valueOf(top.get(hover).getCropId()));
                    }
                }
            });
        }

        void setData(List<Crop> list, DecimalFormat df) {
            total = list.size();
            Set<String> fields = new HashSet<>();
            long sum = 0;
            for (Crop c : list) {
                fields.add(String.valueOf(c.getFieldId()));
                sum += c.getGrowthDuration();
            }
            fieldsInUse = fields.size();
            avgText = total == 0 ? "0 d" : df.format((double) sum / total) + " d";

            List<Crop> sorted = new ArrayList<>(list);
            sorted.sort((a, b) -> Integer.compare(b.getGrowthDuration(), a.getGrowthDuration()));
            top = new ArrayList<>(sorted.subList(0, Math.min(5, sorted.size())));
            if (top.isEmpty()) {
                longestName = "-";
                longestDays = 0;
                maxD = 120;
            } else {
                longestName = top.get(0).getCropName();
                longestDays = top.get(0).getGrowthDuration();
                maxD = Math.max(120, longestDays);
            }
            hover = -1;
            progress = 0;
            timer.restart();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 20, 20);
            g.setPaint(new GradientPaint(0, 0, O_TOP, w, h, O_BOTTOM));
            g.fill(shape);

            g.setClip(shape);
            g.setColor(new Color(255, 255, 255, 12));
            g.fillOval(w - 190, -90, 280, 280);
            g.setColor(new Color(255, 255, 255, 9));
            g.fillOval(-60, h - 70, 190, 190);
            g.setClip(null);

            boolean wide = w >= 640;
            int rx = (int) (w * 0.5);
            int leftW = wide ? rx - 44 : w - 52;
            double e = ease(progress);

            g.setFont(f(Font.BOLD, 11));
            g.setColor(MUTED);
            g.drawString("TOTAL CROPS", 26, 36);

            g.setFont(f(Font.BOLD, 42));
            g.setColor(Color.WHITE);
            g.drawString(String.valueOf((int) Math.round(total * e)), 26, 86);

            int col = Math.max(60, leftW / 3);
            String[] labels = {"FIELDS IN USE", "AVG DURATION", "LONGEST"};
            String[] values = {String.valueOf(fieldsInUse), avgText,
                    longestDays > 0 ? longestName + " (" + longestDays + "d)" : "-"};
            for (int i = 0; i < 3; i++) {
                int x = 26 + i * col;
                g.setFont(f(Font.BOLD, 10));
                g.setColor(MUTED);
                g.drawString(labels[i], x, 124);
                g.setFont(f(Font.BOLD, 15));
                g.setColor(LIME);
                g.drawString(fit(values[i], g.getFontMetrics(), col - 12), x, 148);
            }

            if (!wide || top.isEmpty()) {
                rows = new Rectangle[0];
                g.dispose();
                return;
            }

            int rw = w - rx - 24;
            g.setColor(new Color(255, 255, 255, 34));
            g.drawLine(rx - 20, 22, rx - 20, h - 22);

            g.setFont(f(Font.BOLD, 11));
            g.setColor(MUTED);
            g.drawString("GROWTH TIMELINE (days)", rx, 30);

            int labelW = 104;
            int valW = 56;
            int bx = rx + labelW;
            int bw = rw - labelW - valW;
            double scale = maxD;

            int[] marks = {30, 60, 120};
            Stroke dash = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{3f, 4f}, 0f);
            for (int d : marks) {
                int x = bx + (int) (bw * (d / scale));
                g.setStroke(dash);
                g.setColor(new Color(255, 255, 255, 46));
                g.drawLine(x, 48, x, 158);
                g.setStroke(new BasicStroke(1f));
                g.setFont(f(Font.PLAIN, 10));
                g.setColor(new Color(176, 188, 140, 200));
                String s = String.valueOf(d);
                g.drawString(s, x - g.getFontMetrics().stringWidth(s) / 2, 44);
            }

            rows = new Rectangle[top.size()];
            for (int i = 0; i < top.size(); i++) {
                Crop c = top.get(i);
                int y = 56 + i * 20;
                rows[i] = new Rectangle(rx - 8, y, rw + 14, 20);
                boolean hv = i == hover;

                if (hv) {
                    g.setColor(new Color(255, 255, 255, 24));
                    g.fillRoundRect(rows[i].x, rows[i].y, rows[i].width, rows[i].height, 10, 10);
                }

                g.setFont(f(hv ? Font.BOLD : Font.PLAIN, 12));
                g.setColor(hv ? Color.WHITE : new Color(230, 236, 205));
                g.drawString(fit(c.getCropName(), g.getFontMetrics(), labelW - 10), rx, y + 14);

                g.setColor(new Color(255, 255, 255, 28));
                g.fillRoundRect(bx, y + 6, bw, 8, 8, 8);

                double ei = ease(progress * 1.4 - i * 0.12);
                int len = Math.max(6, (int) (bw * (c.getGrowthDuration() / scale) * ei));
                Color bright = BRIGHT[i % BRIGHT.length];
                Color deep = DEEP[i % DEEP.length];
                g.setPaint(new GradientPaint(bx, 0, deep, bx + len, 0, hv ? Color.WHITE : bright));
                g.fillRoundRect(bx, y + 6, len, 8, 8, 8);

                g.setFont(f(Font.BOLD, 11));
                g.setColor(hv ? Color.WHITE : LIME);
                g.drawString(c.getGrowthDuration() + " d", bx + bw + 10, y + 14);
            }
            g.dispose();
        }
    }

    private abstract static class Cell extends JComponent implements TableCellRenderer {
        protected Object value;
        protected boolean sel;
        protected int row;

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean focus, int r, int c) {
            value = v;
            sel = s;
            row = r;
            return this;
        }

        protected String text() {
            return value == null ? "" : value.toString();
        }

        protected int baseline(Graphics2D g, int h) {
            FontMetrics fm = g.getFontMetrics();
            return (h + fm.getAscent() - fm.getDescent()) / 2;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(sel ? new Color(228, 234, 196) : (row % 2 == 0 ? Color.WHITE : new Color(251, 251, 246)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(236, 236, 226));
            g.drawLine(0, h - 1, w, h - 1);
            draw(g, w, h);
            g.dispose();
        }

        abstract void draw(Graphics2D g, int w, int h);
    }

    private static class IdCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            g.setColor(O_TOP);
            g.drawString(fit(text(), g.getFontMetrics(), w - 20), 14, baseline(g, h));
        }
    }

    private static class NameCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            float s = 14f;
            float x = 14f;
            float y = (h - s) / 2f;
            Path2D.Float leaf = new Path2D.Float();
            leaf.moveTo(x, y + s);
            leaf.curveTo(x, y + s * 0.2f, x + s * 0.3f, y, x + s, y);
            leaf.curveTo(x + s, y + s * 0.7f, x + s * 0.8f, y + s, x, y + s);
            leaf.closePath();
            g.setColor(new Color(124, 142, 40));
            g.fill(leaf);
            g.setColor(new Color(255, 255, 255, 170));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine((int) (x + 2), (int) (y + s - 2), (int) (x + s - 3), (int) (y + 3));

            g.setFont(f(Font.BOLD, 13));
            g.setColor(INK);
            g.drawString(fit(text(), g.getFontMetrics(), w - 52), 36, baseline(g, h));
        }
    }

    private static class TypeCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            String s = text();
            if (s.isEmpty()) return;
            int idx = Math.abs(s.toLowerCase().hashCode()) % DEEP.length;
            Color base = DEEP[idx];
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String t = fit(s, fm, w - 44);
            int pw = fm.stringWidth(t) + 22;
            int ph = 24;
            int py = (h - ph) / 2;
            g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 36));
            g.fillRoundRect(10, py, pw, ph, ph, ph);
            g.setColor(base);
            g.drawString(t, 21, baseline(g, h));
        }
    }

    private class DurationCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            int days = 0;
            try {
                days = Integer.parseInt(text().trim());
            } catch (NumberFormatException ignored) {
            }
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String label = days + " days";
            int tw = fm.stringWidth("000 days");
            int bw = Math.max(20, w - 28 - tw - 10);
            int by = (h - 8) / 2;
            g.setColor(new Color(234, 236, 220));
            g.fillRoundRect(14, by, bw, 8, 8, 8);
            double ratio = maxDuration <= 0 ? 0 : (double) days / maxDuration;
            int len = Math.max(6, (int) (bw * ratio));
            g.setPaint(new GradientPaint(14, 0, new Color(124, 142, 40), 14 + len, 0, new Color(214, 178, 52)));
            g.fillRoundRect(14, by, len, 8, 8, 8);
            g.setColor(INK);
            g.drawString(label, 14 + bw + 10, baseline(g, h));
        }
    }

    private static class FieldCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            String s = text();
            if (s.isEmpty()) return;
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String t = fit(s, fm, w - 40);
            int pw = fm.stringWidth(t) + 20;
            int ph = 24;
            int py = (h - ph) / 2;
            g.setColor(new Color(240, 241, 228));
            g.fillRoundRect(10, py, pw, ph, 8, 8);
            g.setColor(new Color(214, 216, 190));
            g.drawRoundRect(10, py, pw, ph, 8, 8);
            g.setColor(new Color(96, 104, 52));
            g.drawString(t, 20, baseline(g, h));
        }
    }
}