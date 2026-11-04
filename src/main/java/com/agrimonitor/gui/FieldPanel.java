package com.agrimonitor.gui;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Field;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FieldPanel extends JPanel {

    private static final Color SEL = new Color(222, 236, 228);
    private static final Color ZEBRA = new Color(250, 247, 240);
    private static final Color[] PALETTE = {
            new Color(74, 140, 98),
            new Color(122, 168, 78),
            new Color(52, 120, 110),
            new Color(184, 152, 68),
            new Color(96, 132, 72),
            new Color(60, 104, 84),
            new Color(150, 182, 108),
            new Color(206, 170, 80),
            new Color(80, 150, 140),
            new Color(116, 112, 72)
    };

    private final FieldDAO fieldDAO = new FieldDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    private final JTextField idField = UITheme.createTextField();
    private final JTextField nameField = UITheme.createTextField();
    private final JTextField locationField = UITheme.createTextField();
    private final JTextField areaField = UITheme.createTextField();

    private final DefaultTableModel model = UIKit.model("Field ID", "Field Name", "Location", "Area Size");
    private final JTable table = new JTable(model);
    private final LandCard insight = new LandCard();

    private double maxArea = 0;

    public FieldPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        insight.setOnPick(this::selectById);
        add(insight, BorderLayout.NORTH);

        table.getColumnModel().getColumn(0).setCellRenderer(new ChipRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(new AreaRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = table.getSelectedRow();
            if (row < 0) {
                return;
            }
            int m = table.convertRowIndexToModel(row);
            idField.setText(String.valueOf(model.getValueAt(m, 0)));
            nameField.setText(String.valueOf(model.getValueAt(m, 1)));
            locationField.setText(String.valueOf(model.getValueAt(m, 2)));
            areaField.setText(String.valueOf(model.getValueAt(m, 3)));
        });

        JComponent tableCard = UIKit.tableCard("All Fields", table, "Search fields...");

        JButton addBtn = UITheme.createStyledButton("Add Field", UITheme.SUCCESS);
        JButton updateBtn = UITheme.createStyledButton("Update", UITheme.INFO);
        JButton deleteBtn = UITheme.createStyledButton("Delete", UITheme.DANGER);
        JButton refreshBtn = UITheme.createStyledButton("Refresh", UITheme.TEXT_LIGHT);
        JButton clearBtn = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        addBtn.addActionListener(e -> addField());
        updateBtn.addActionListener(e -> updateField());
        deleteBtn.addActionListener(e -> deleteField());
        refreshBtn.addActionListener(e -> loadData());
        clearBtn.addActionListener(e -> clearForm());

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridy = 0;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 10, 6);
        body.add(UIKit.field("Field ID", idField), gc);
        gc.gridx = 1;
        gc.insets = new Insets(0, 6, 10, 0);
        body.add(UIKit.field("Area Size", areaField), gc);
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(0, 0, 10, 0);
        gc.gridy = 1;
        body.add(UIKit.field("Field Name", nameField), gc);
        gc.gridy = 2;
        gc.insets = new Insets(0, 0, 0, 0);
        body.add(UIKit.field("Location", locationField), gc);

        JPanel row1 = new JPanel(new GridLayout(1, 2, 8, 8));
        row1.setOpaque(false);
        row1.add(addBtn);
        row1.add(updateBtn);
        JPanel row2 = new JPanel(new GridLayout(1, 3, 8, 8));
        row2.setOpaque(false);
        row2.add(deleteBtn);
        row2.add(refreshBtn);
        row2.add(clearBtn);
        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 8));
        footer.setOpaque(false);
        footer.add(row1);
        footer.add(row2);

        JComponent form = UIKit.formCard("Field Details", body, footer);

        TipPanel tip = new TipPanel();
        tip.setPreferredSize(new Dimension(100, 86));
        tip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));
        tip.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.add(form);
        stack.add(Box.createVerticalStrut(14));
        stack.add(tip);

        ScrollBody column = new ScrollBody(new BorderLayout());
        column.setOpaque(false);
        column.add(stack, BorderLayout.NORTH);

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

        loadData();
    }

    private void selectById(String id) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (String.valueOf(model.getValueAt(i, 0)).equals(id)) {
                int v = table.convertRowIndexToView(i);
                if (v >= 0) {
                    table.setRowSelectionInterval(v, v);
                    table.scrollRectToVisible(table.getCellRect(v, 0, true));
                }
                return;
            }
        }
    }

    private void addField() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String loc = locationField.getText().trim();
        String areaText = areaField.getText().trim();
        if (id.isEmpty() || name.isEmpty() || loc.isEmpty() || areaText.isEmpty()) {
            UIKit.warn(this, "Please fill in all fields.");
            return;
        }
        double area;
        try {
            area = Double.parseDouble(areaText);
        } catch (NumberFormatException ex) {
            UIKit.warn(this, "Area size must be a number.");
            return;
        }
        if (fieldDAO.fieldExists(id)) {
            UIKit.warn(this, "This Field ID already exists.");
            return;
        }
        fieldDAO.addField(new Field(id, name, loc, area));
        UIKit.info(this, "Field added successfully.");
        loadData();
        clearForm();
    }

    private void updateField() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String loc = locationField.getText().trim();
        String areaText = areaField.getText().trim();
        if (id.isEmpty() || name.isEmpty() || loc.isEmpty() || areaText.isEmpty()) {
            UIKit.warn(this, "Please fill in all fields.");
            return;
        }
        double area;
        try {
            area = Double.parseDouble(areaText);
        } catch (NumberFormatException ex) {
            UIKit.warn(this, "Area size must be a number.");
            return;
        }
        if (!fieldDAO.fieldExists(id)) {
            UIKit.warn(this, "Field ID not found.");
            return;
        }
        fieldDAO.updateField(id, name, loc, area);
        UIKit.info(this, "Field updated successfully.");
        loadData();
        clearForm();
    }

    private void deleteField() {
        String id = idField.getText().trim();
        if (id.isEmpty()) {
            UIKit.warn(this, "Select or enter a Field ID to delete.");
            return;
        }
        if (!fieldDAO.fieldExists(id)) {
            UIKit.warn(this, "Field ID not found.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this, "Delete field " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) {
            return;
        }
        fieldDAO.deleteField(id);
        UIKit.info(this, "Field deleted.");
        loadData();
        clearForm();
    }

    private void loadData() {
        model.setRowCount(0);
        List<Field> list = fieldDAO.getAllFields();
        double max = 0;
        for (Field f : list) {
            model.addRow(new Object[]{f.getFieldId(), f.getFieldName(), f.getLocation(), f.getAreaSize()});
            max = Math.max(max, f.getAreaSize());
        }
        maxArea = max;
        insight.setData(list);
        table.repaint();
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        locationField.setText("");
        areaField.setText("");
        table.clearSelection();
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static Color colorFor(String id) {
        int h = id == null ? 0 : Math.abs(id.toLowerCase().hashCode());
        return PALETTE[h % PALETTE.length];
    }

    private static String clip(String s, FontMetrics fm, int max) {
        if (s == null) return "";
        if (max <= 0) return "";
        if (fm.stringWidth(s) <= max) return s;
        String e = "...";
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n) + e) > max) n--;
        return s.substring(0, n) + e;
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

            g.setColor(new Color(236, 244, 236));
            g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g.setColor(new Color(196, 214, 200));
            g.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);
            g.setColor(new Color(46, 110, 84));
            g.fillRoundRect(0, 14, 4, h - 28, 4, 4);

            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 13));
            g.setColor(new Color(31, 80, 58));
            g.drawString("Quick tip", 20, 32);

            g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 12));
            g.setColor(new Color(70, 104, 86));
            FontMetrics fm = g.getFontMetrics();
            int maxW = w - 34;
            g.drawString(clip("Click a land plot or a table row", fm, maxW), 20, 52);
            g.drawString(clip("to load that field into the form.", fm, maxW), 20, 70);
            g.dispose();
        }
    }

    private class AreaRenderer extends DefaultTableCellRenderer {
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
            frac = maxArea > 0 ? Math.max(0, Math.min(1, v / maxArea)) : 0;
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
                int alpha = selected ? 80 : 56;
                g.setPaint(new GradientPaint(8, 0, new Color(74, 140, 98, alpha / 3),
                        (float) (8 + bw), 0, new Color(74, 140, 98, alpha + 20)));
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
            g.setColor(new Color(46, 110, 84, 28));
            g.fill(new RoundRectangle2D.Double(px, py, pw, ph, ph, ph));
            g.setColor(new Color(46, 110, 84, 90));
            g.draw(new RoundRectangle2D.Double(px + 0.5, py + 0.5, pw - 1, ph - 1, ph, ph));
            g.setColor(new Color(31, 80, 58));
            g.drawString(shown, px + 11, py + (ph - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    private static class NameRenderer extends DefaultTableCellRenderer {
        private String text = "";
        private Color bg = Color.WHITE;
        private Color swatch = PALETTE[0];

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            text = value == null ? "" : value.toString();
            bg = isSelected ? SEL : (row % 2 == 0 ? Color.WHITE : ZEBRA);
            Object id = t.getValueAt(row, 0);
            swatch = colorFor(id == null ? "" : id.toString());
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(bg);
            g.fillRect(0, 0, getWidth(), getHeight());
            int cy = getHeight() / 2;
            g.setColor(swatch);
            g.fill(new RoundRectangle2D.Double(12, cy - 6, 12, 12, 4, 4));
            g.setFont(UIKit.BODY_BOLD);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(UITheme.TEXT_DARK);
            g.drawString(clip(text, fm, getWidth() - 46), 32, cy + (fm.getAscent() - fm.getDescent()) / 2);
            g.dispose();
        }
    }

    private static class LandCard extends JPanel {
        private static final int MAX_TILES = 10;
        private static final int GAP = 5;
        private static final int MIN_W = 22;

        private final DecimalFormat f = new DecimalFormat("#,##0.##");
        private final List<Field> shown = new ArrayList<>();
        private double total;
        private int count;
        private int hover = -1;
        private float progress = 1f;
        private final Timer anim;
        private Consumer<String> onPick;

        LandCard() {
            setOpaque(false);
            setPreferredSize(new Dimension(10, 160));
            setMinimumSize(new Dimension(0, 160));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
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
                    setCursor(i >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
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

                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = indexAt(e.getX(), e.getY());
                    if (i >= 0 && onPick != null) {
                        onPick.accept(String.valueOf(shown.get(i).getFieldId()));
                    }
                }
            });
        }

        void setOnPick(Consumer<String> c) {
            onPick = c;
        }

        void setData(List<Field> list) {
            shown.clear();
            total = 0;
            count = list == null ? 0 : list.size();
            if (list != null) {
                for (Field fd : list) {
                    total += fd.getAreaSize();
                }
                List<Field> sorted = new ArrayList<>(list);
                sorted.sort((a, b) -> Double.compare(b.getAreaSize(), a.getAreaSize()));
                for (int i = 0; i < Math.min(MAX_TILES, sorted.size()); i++) {
                    shown.add(sorted.get(i));
                }
            }
            hover = -1;
            progress = 0f;
            anim.restart();
        }

        private int plotLeft() {
            return 330;
        }

        private int plotRight() {
            return getWidth() - 30;
        }

        private int plotTop() {
            return 54;
        }

        private int plotHeight() {
            return getHeight() - 54 - 46;
        }

        private double[] widths() {
            int n = shown.size();
            double[] ws = new double[n];
            if (n == 0) return ws;
            double sum = 0;
            for (Field fd : shown) sum += fd.getAreaSize();
            double avail = plotRight() - plotLeft() - GAP * (n - 1);
            double free = avail - MIN_W * n;
            for (int i = 0; i < n; i++) {
                if (free > 0 && sum > 0) {
                    ws[i] = MIN_W + free * (shown.get(i).getAreaSize() / sum);
                } else {
                    ws[i] = avail / n;
                }
            }
            return ws;
        }

        private int indexAt(int mx, int my) {
            if (getWidth() < 560 || shown.isEmpty()) return -1;
            if (my < plotTop() || my > plotTop() + plotHeight()) return -1;
            double[] ws = widths();
            double x = plotLeft();
            for (int i = 0; i < ws.length; i++) {
                if (mx >= x && mx <= x + ws[i]) return i;
                x += ws[i] + GAP;
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 22, 22);
            g.setPaint(new GradientPaint(0, 0, new Color(20, 52, 40), w, h, new Color(54, 114, 82)));
            g.fill(shape);
            g.setClip(shape);

            g.setColor(new Color(255, 255, 255, 12));
            g.fillOval(w - 170, -110, 280, 280);
            g.setColor(new Color(255, 255, 255, 8));
            g.fillOval(-70, h - 80, 240, 240);

            int lx = 30;
            g.setColor(new Color(223, 196, 120));
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    g.fill(new RoundRectangle2D.Double(lx + i * 8, 22 + j * 8, 6, 6, 2, 2));
                }
            }
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 11));
            g.drawString("TOTAL AREA", lx + 24, 36);

            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 38));
            g.setColor(Color.WHITE);
            g.drawString(f.format(total), lx, 86);

            g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 13));
            g.setColor(new Color(206, 228, 214));
            g.drawString(count + (count == 1 ? " field" : " fields"), lx, 112);
            String avg = count == 0 ? "No data yet" : "Average " + f.format(total / count) + " per field";
            g.drawString(avg, lx, 132);

            if (w >= 560) {
                int x0 = plotLeft();
                int x1 = plotRight();
                int ty = plotTop();
                int th = plotHeight();

                g.setColor(new Color(255, 255, 255, 36));
                g.drawLine(x0 - 22, 22, x0 - 22, h - 22);

                g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 11));
                g.setColor(new Color(223, 196, 120));
                g.drawString("LAND DISTRIBUTION", x0, 36);

                if (!shown.isEmpty()) {
                    String right = "Largest: " + shown.get(0).getFieldName() + " (" + f.format(shown.get(0).getAreaSize()) + ")";
                    g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
                    FontMetrics rfm = g.getFontMetrics();
                    right = clip(right, rfm, x1 - x0 - 150);
                    g.setColor(new Color(190, 220, 202));
                    g.drawString(right, x1 - rfm.stringWidth(right), 36);
                }

                if (shown.isEmpty()) {
                    g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 13));
                    g.setColor(new Color(206, 228, 214));
                    String msg = "No fields registered yet";
                    FontMetrics fm = g.getFontMetrics();
                    g.drawString(msg, x0 + (x1 - x0 - fm.stringWidth(msg)) / 2, h / 2 + 4);
                } else {
                    float eased = 1f - (float) Math.pow(1 - progress, 3);
                    double[] ws = widths();
                    double x = x0;
                    for (int i = 0; i < ws.length; i++) {
                        Field fd = shown.get(i);
                        Color c = colorFor(String.valueOf(fd.getFieldId()));
                        boolean hot = i == hover;
                        double curH = Math.max(4, th * eased);
                        double by = ty + th - curH;
                        RoundRectangle2D tile = new RoundRectangle2D.Double(x, by, ws[i], curH, 14, 14);

                        Color top = hot ? c.brighter() : c;
                        Color bot = hot ? c : c.darker();
                        g.setPaint(new GradientPaint(0, (float) by, top, 0, (float) (by + curH), bot));
                        g.fill(tile);

                        Shape old = g.getClip();
                        g.clip(tile);
                        g.setStroke(new BasicStroke(1f));
                        g.setColor(new Color(255, 255, 255, 26));
                        for (int k = -th; k < ws[i] + th; k += 9) {
                            g.drawLine((int) x + k, (int) (by + curH), (int) x + k + (int) curH, (int) by);
                        }
                        g.setClip(old);

                        if (hot) {
                            g.setColor(new Color(246, 224, 150));
                            g.setStroke(new BasicStroke(2f));
                            g.draw(new RoundRectangle2D.Double(x + 1, by + 1, ws[i] - 2, curH - 2, 13, 13));
                        }

                        if (eased > 0.9f && ws[i] >= 64) {
                            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
                            FontMetrics fm = g.getFontMetrics();
                            g.setColor(Color.WHITE);
                            g.drawString(clip(String.valueOf(fd.getFieldName()), fm, (int) ws[i] - 20), (int) x + 10, ty + 24);
                            g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 11));
                            fm = g.getFontMetrics();
                            g.setColor(new Color(255, 255, 255, 215));
                            g.drawString(clip(f.format(fd.getAreaSize()), fm, (int) ws[i] - 20), (int) x + 10, ty + 42);
                        }
                        x += ws[i] + GAP;
                    }

                    g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 12));
                    FontMetrics fm = g.getFontMetrics();
                    String info;
                    if (hover >= 0 && hover < shown.size()) {
                        Field fd = shown.get(hover);
                        String pct = total > 0 ? String.format("%.1f%%", fd.getAreaSize() / total * 100) : "0%";
                        info = fd.getFieldId() + "  -  " + fd.getFieldName() + "  -  " + fd.getLocation() + "  -  " + f.format(fd.getAreaSize()) + "  (" + pct + " of total)";
                        g.setColor(Color.WHITE);
                    } else {
                        info = count > MAX_TILES
                                ? "Showing the " + MAX_TILES + " largest plots. Hover for details, click to select."
                                : "Hover a plot for details, click to select it.";
                        g.setColor(new Color(176, 206, 188));
                    }
                    g.drawString(clip(info, fm, x1 - x0), x0, ty + th + 24);
                }
            }
            g.dispose();
        }
    }
}