package com.agrimonitor.gui;

import com.agrimonitor.dao.CostDAO;
import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Cost;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

public class CostPanel extends JPanel {

    private static final Color W_TOP = new Color(128, 30, 56);
    private static final Color W_BOTTOM = new Color(38, 8, 20);
    private static final Color ROSE = new Color(244, 200, 212);
    private static final Color MUTED = new Color(208, 162, 176);
    private static final Color INK = new Color(70, 24, 38);

    private static final String[] CATS = {
            "Seeds", "Fertilizer", "Pesticide", "Labor", "Irrigation", "Machinery", "Transport", "Other"
    };
    private static final Color[] CAT_COL = {
            new Color(240, 196, 64), new Color(150, 206, 110), new Color(232, 120, 90), new Color(120, 170, 230),
            new Color(90, 200, 200), new Color(190, 140, 230), new Color(230, 150, 190), new Color(190, 190, 170)
    };

    private final CostDAO costDAO = new CostDAO();
    private final CropDAO cropDAO = new CropDAO();
    private final FieldDAO fieldDAO = new FieldDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    private final JTextField tfCrop = UITheme.createTextField();
    private final JTextField tfField = UITheme.createTextField();
    private final JTextField tfNote = UITheme.createTextField();
    private final JTextField tfAmount = UITheme.createTextField();
    private final JTextField tfDate = UITheme.createTextField();
    private final JTextField tfTotal = UITheme.createTextField();
    private final ChipSelector chips = new ChipSelector();

    private final DefaultTableModel model;
    private final JTable table;
    private final ExpenseCard card;
    private double maxAmount = 1;

    public CostPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        card = new ExpenseCard();
        add(card, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Crop ID", "Field ID", "Description", "Amount", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(model);
        UIKit.styleTable(table);
        table.setRowHeight(42);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {60, 60, 210, 150, 90};
        Cell[] cells = {new IdCell(), new FieldCell(), new DescCell(), new AmountCell(), new DateCell()};
        for (int i = 0; i < cells.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setCellRenderer(cells[i]);
        }

        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int r = table.getSelectedRow();
            if (r < 0) return;
            int m = table.convertRowIndexToModel(r);
            tfCrop.setText(str(model.getValueAt(m, 0)));
            tfField.setText(str(model.getValueAt(m, 1)));
        });

        JComponent tableCard = UIKit.tableCard("All Costs", table, "Click a row to reuse its IDs");

        JButton addBtn = UITheme.createStyledButton("Add Cost", UITheme.PRIMARY);
        JButton refBtn = UITheme.createStyledButton("Refresh", UITheme.TEXT_LIGHT);
        JButton clrBtn = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);
        JButton totBtn = UITheme.createStyledButton("Get Crop Total", UITheme.INFO);

        addBtn.addActionListener(e -> addCost());
        refBtn.addActionListener(e -> loadData());
        clrBtn.addActionListener(e -> clearForm());
        totBtn.addActionListener(e -> cropTotal());

        tfDate.setText(LocalDate.now().toString());
        tfTotal.setEditable(false);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridy = 0;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 10, 6);
        body.add(UIKit.field("Crop ID", tfCrop), gc);
        gc.gridx = 1;
        gc.insets = new Insets(0, 6, 10, 0);
        body.add(UIKit.field("Field ID", tfField), gc);
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.insets = new Insets(0, 0, 10, 0);
        gc.gridy = 1;
        body.add(UIKit.field("Category", chips), gc);
        gc.gridy = 2;
        body.add(UIKit.field("Note", tfNote), gc);
        gc.gridy = 3;
        gc.gridwidth = 1;
        gc.gridx = 0;
        gc.insets = new Insets(0, 0, 0, 6);
        body.add(UIKit.field("Amount (BDT)", tfAmount), gc);
        gc.gridx = 1;
        gc.insets = new Insets(0, 6, 0, 0);
        body.add(UIKit.field("Date (yyyy-MM-dd)", tfDate), gc);

        JPanel footer = new JPanel(new GridLayout(1, 3, 8, 8));
        footer.setOpaque(false);
        footer.add(addBtn);
        footer.add(refBtn);
        footer.add(clrBtn);

        JComponent form = UIKit.formCard("Add Expense", body, footer);

        JPanel totalBody = new JPanel(new BorderLayout());
        totalBody.setOpaque(false);
        totalBody.add(UIKit.field("Total cost for the Crop ID above", tfTotal), BorderLayout.CENTER);
        JPanel totalFooter = new JPanel(new GridLayout(1, 1));
        totalFooter.setOpaque(false);
        totalFooter.add(totBtn);
        JComponent totalCard = UIKit.formCard("Crop Total", totalBody, totalFooter);

        JPanel stack = new JPanel(new GridLayout(0, 1, 0, 14));
        stack.setOpaque(false);
        stack.add(form);
        stack.add(totalCard);

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

    private static int catIndex(String desc) {
        if (desc == null) return CATS.length - 1;
        for (int i = 0; i < CATS.length; i++) {
            if (desc.equals(CATS[i]) || desc.startsWith(CATS[i] + " - ")) return i;
        }
        return CATS.length - 1;
    }

    private static boolean hasCategory(String desc) {
        if (desc == null) return false;
        for (String c : CATS) {
            if (desc.equals(c) || desc.startsWith(c + " - ")) return true;
        }
        return false;
    }

    private static String noteOf(String desc) {
        if (desc == null) return "";
        int i = catIndex(desc);
        if (!hasCategory(desc)) return desc;
        if (desc.equals(CATS[i])) return "";
        return desc.substring(CATS[i].length() + 3);
    }

    private static Color darker(Color c) {
        return new Color((int) (c.getRed() * 0.62), (int) (c.getGreen() * 0.62), (int) (c.getBlue() * 0.62));
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "AgriMonitor", JOptionPane.WARNING_MESSAGE);
    }

    private void info(String msg) {
        JOptionPane.showMessageDialog(this, msg, "AgriMonitor", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearForm() {
        tfCrop.setText("");
        tfField.setText("");
        tfNote.setText("");
        tfAmount.setText("");
        tfDate.setText(LocalDate.now().toString());
        tfTotal.setText("");
        chips.clear();
        table.clearSelection();
    }

    private void loadData() {
        try {
            List<Cost> list = costDAO.getAllCosts();
            model.setRowCount(0);
            double max = 1;
            for (Cost c : list) {
                model.addRow(new Object[]{c.getCropId(), c.getFieldId(), c.getDescription(),
                        c.getAmount(), c.getDate()});
                max = Math.max(max, c.getAmount());
            }
            maxAmount = max;
            card.setData(list, df);
            table.repaint();
        } catch (Exception ex) {
            warn("Could not load costs: " + ex.getMessage());
        }
    }

    private void addCost() {
        String cropId = tfCrop.getText().trim();
        String fieldId = tfField.getText().trim();
        String note = tfNote.getText().trim();
        String amountText = tfAmount.getText().trim();
        String date = tfDate.getText().trim();
        String category = chips.getSelected();

        if (cropId.isEmpty() || fieldId.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
            warn("Crop ID, Field ID, Amount and Date are required.");
            return;
        }
        if (category.isEmpty() && note.isEmpty()) {
            warn("Choose a category or write a note.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException ex) {
            warn("Amount must be a number.");
            return;
        }
        if (amount <= 0) {
            warn("Amount must be greater than zero.");
            return;
        }
        try {
            LocalDate.parse(date);
        } catch (Exception ex) {
            warn("Date must be in yyyy-MM-dd format.");
            return;
        }
        try {
            if (!cropDAO.cropExists(cropId)) {
                warn("Crop ID does not exist.");
                return;
            }
            if (!fieldDAO.fieldExists(fieldId)) {
                warn("Field ID does not exist.");
                return;
            }
            String description;
            if (category.isEmpty()) description = note;
            else if (note.isEmpty()) description = category;
            else description = category + " - " + note;

            costDAO.addCost(new Cost(cropId, fieldId, description, amount, date));
            info("Cost added successfully.");
            clearForm();
            loadData();
        } catch (Exception ex) {
            warn("Could not add cost: " + ex.getMessage());
        }
    }

    private void cropTotal() {
        String cropId = tfCrop.getText().trim();
        if (cropId.isEmpty()) {
            warn("Enter a Crop ID first.");
            return;
        }
        try {
            if (!cropDAO.cropExists(cropId)) {
                warn("Crop ID does not exist.");
                return;
            }
            double total = costDAO.getTotalCostByCropId(cropId);
            tfTotal.setText("BDT " + df.format(total));
        } catch (Exception ex) {
            warn("Could not get total: " + ex.getMessage());
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

    private static class ChipSelector extends JComponent {
        private int selected = -1;
        private int hover = -1;
        private final Rectangle[] rects = new Rectangle[CATS.length];

        ChipSelector() {
            setOpaque(false);
            for (int i = 0; i < rects.length; i++) rects[i] = new Rectangle();

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = indexAt(e.getPoint());
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
                    int idx = indexAt(e.getPoint());
                    if (idx < 0) return;
                    selected = selected == idx ? -1 : idx;
                    repaint();
                }
            });
        }

        String getSelected() {
            return selected < 0 ? "" : CATS[selected];
        }

        void clear() {
            selected = -1;
            repaint();
        }

        private int indexAt(Point p) {
            for (int i = 0; i < rects.length; i++) {
                if (rects[i].contains(p)) return i;
            }
            return -1;
        }

        private int layoutChips(int width, FontMetrics fm) {
            int x = 0;
            int y = 0;
            int ch = 28;
            for (int i = 0; i < CATS.length; i++) {
                int cw = fm.stringWidth(CATS[i]) + 26;
                if (x > 0 && x + cw > width) {
                    x = 0;
                    y += ch + 6;
                }
                rects[i].setBounds(x, y, cw, ch);
                x += cw + 6;
            }
            return y + ch + 2;
        }

        @Override
        public Dimension getPreferredSize() {
            int w = getWidth() > 0 ? getWidth() : 250;
            int h = layoutChips(w, getFontMetrics(f(Font.BOLD, 12)));
            return new Dimension(w, h);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int needed = layoutChips(getWidth(), fm);
            if (needed != getHeight()) {
                SwingUtilities.invokeLater(this::revalidate);
            }
            for (int i = 0; i < CATS.length; i++) {
                Rectangle r = rects[i];
                Color base = CAT_COL[i];
                boolean sel = i == selected;
                boolean hv = i == hover;
                if (sel) {
                    g.setColor(darker(base));
                    g.fillRoundRect(r.x, r.y, r.width, r.height, r.height, r.height);
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(hv ? new Color(base.getRed(), base.getGreen(), base.getBlue(), 60)
                            : new Color(base.getRed(), base.getGreen(), base.getBlue(), 30));
                    g.fillRoundRect(r.x, r.y, r.width, r.height, r.height, r.height);
                    g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 150));
                    g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);
                    g.setColor(darker(base));
                }
                int tx = r.x + (r.width - fm.stringWidth(CATS[i])) / 2;
                int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;
                g.drawString(CATS[i], tx, ty);
            }
            g.dispose();
        }
    }

    private static class ExpenseCard extends JPanel {
        private final javax.swing.Timer timer;
        private double progress = 1;
        private double total;
        private int count;
        private double avg;
        private double max;
        private final double[] cat = new double[CATS.length];
        private final Rectangle[] segs = new Rectangle[CATS.length];
        private int hover = -1;
        private final DecimalFormat whole = new DecimalFormat("#,##0");

        ExpenseCard() {
            setOpaque(false);
            setPreferredSize(new Dimension(100, 168));
            setMinimumSize(new Dimension(100, 168));

            timer = new javax.swing.Timer(16, e -> {
                progress += 0.032;
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
                    for (int i = 0; i < segs.length; i++) {
                        if (segs[i] != null && segs[i].contains(e.getPoint())) {
                            idx = i;
                            break;
                        }
                    }
                    if (idx != hover) {
                        hover = idx;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = -1;
                    repaint();
                }
            });
        }

        void setData(List<Cost> list, DecimalFormat df) {
            count = list.size();
            total = 0;
            max = 0;
            for (int i = 0; i < cat.length; i++) cat[i] = 0;
            for (Cost c : list) {
                double a = c.getAmount();
                total += a;
                max = Math.max(max, a);
                cat[catIndex(c.getDescription())] += a;
            }
            avg = count == 0 ? 0 : total / count;
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
            g.setPaint(new GradientPaint(0, 0, W_TOP, w, h, W_BOTTOM));
            g.fill(shape);

            g.setClip(shape);
            g.setColor(new Color(255, 255, 255, 12));
            g.fillOval(w - 190, -90, 280, 280);
            g.setColor(new Color(255, 255, 255, 9));
            g.fillOval(-60, h - 70, 190, 190);
            g.setClip(null);

            boolean wide = w >= 700;
            int rx = (int) (w * 0.46);
            int leftW = wide ? rx - 44 : w - 52;
            double e = ease(progress);

            g.setFont(f(Font.BOLD, 11));
            g.setColor(MUTED);
            g.drawString("TOTAL EXPENSES", 26, 34);

            g.setFont(f(Font.BOLD, 14));
            g.setColor(MUTED);
            g.drawString("BDT", 26, 80);
            int bdtW = g.getFontMetrics().stringWidth("BDT") + 8;
            g.setFont(f(Font.BOLD, 38));
            g.setColor(Color.WHITE);
            g.drawString(whole.format(total * e), 26 + bdtW, 82);

            int col = Math.max(70, leftW / 3);
            String[] labels = {"RECORDS", "AVERAGE", "HIGHEST"};
            String[] values = {String.valueOf(count), whole.format(avg), whole.format(max)};
            for (int i = 0; i < 3; i++) {
                int x = 26 + i * col;
                g.setColor(new Color(255, 255, 255, 18));
                g.fillRoundRect(x - 8, 102, col - 10, 50, 14, 14);
                g.setFont(f(Font.BOLD, 10));
                g.setColor(MUTED);
                g.drawString(labels[i], x, 120);
                g.setFont(f(Font.BOLD, 15));
                g.setColor(ROSE);
                g.drawString(fit(values[i], g.getFontMetrics(), col - 30), x, 142);
            }

            for (int i = 0; i < segs.length; i++) segs[i] = null;

            if (!wide || total <= 0) {
                g.dispose();
                return;
            }

            int rw = w - rx - 26;
            g.setColor(new Color(255, 255, 255, 34));
            g.drawLine(rx - 20, 22, rx - 20, h - 22);

            g.setFont(f(Font.BOLD, 11));
            g.setColor(MUTED);
            String head = "WHERE THE MONEY WENT";
            if (hover >= 0) {
                double pct = cat[hover] / total * 100.0;
                head = CATS[hover] + "  -  BDT " + whole.format(cat[hover]) + "  -  " + Math.round(pct) + "%";
                g.setColor(Color.WHITE);
            }
            g.drawString(head, rx, 32);

            int by = 46;
            int bh = 20;
            Shape barShape = new RoundRectangle2D.Double(rx, by, rw, bh, bh, bh);
            g.setColor(new Color(255, 255, 255, 26));
            g.fill(barShape);

            Shape oldClip = g.getClip();
            g.clip(new RoundRectangle2D.Double(rx, by, rw * e, bh, bh, bh));
            g.clip(barShape);
            double x = rx;
            for (int i = 0; i < CATS.length; i++) {
                if (cat[i] <= 0) continue;
                double sw = rw * (cat[i] / total);
                Color c = CAT_COL[i];
                g.setColor(i == hover ? Color.WHITE : c);
                g.fill(new Rectangle2D.Double(x, by, Math.max(2, sw - 2), bh));
                segs[i] = new Rectangle((int) x, by - 4, (int) Math.max(2, sw), bh + 8);
                x += sw;
            }
            g.setClip(oldClip);

            int colW = rw / 4;
            for (int i = 0; i < CATS.length; i++) {
                int cx = rx + (i % 4) * colW;
                int cy = 96 + (i / 4) * 34;
                boolean hv = i == hover;
                g.setColor(CAT_COL[i]);
                g.fillOval(cx, cy - 8, 9, 9);
                g.setFont(f(hv ? Font.BOLD : Font.PLAIN, 12));
                g.setColor(hv ? Color.WHITE : new Color(240, 220, 226));
                g.drawString(fit(CATS[i], g.getFontMetrics(), colW - 20), cx + 15, cy);
                g.setFont(f(Font.BOLD, 11));
                g.setColor(MUTED);
                g.drawString(fit(whole.format(cat[i] * e), g.getFontMetrics(), colW - 20), cx + 15, cy + 15);
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
            g.setColor(sel ? new Color(244, 222, 228) : (row % 2 == 0 ? Color.WHITE : new Color(253, 250, 251)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(240, 232, 235));
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
            g.setColor(W_TOP);
            g.drawString(fit(text(), g.getFontMetrics(), w - 20), 14, baseline(g, h));
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
            g.setColor(new Color(246, 238, 241));
            g.fillRoundRect(10, py, pw, ph, 8, 8);
            g.setColor(new Color(226, 208, 214));
            g.drawRoundRect(10, py, pw, ph, 8, 8);
            g.setColor(new Color(110, 52, 70));
            g.drawString(t, 20, baseline(g, h));
        }
    }

    private static class DescCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            String desc = text();
            String note = noteOf(desc);
            int x = 12;
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            if (hasCategory(desc)) {
                int idx = catIndex(desc);
                Color base = CAT_COL[idx];
                String name = CATS[idx];
                int pw = fm.stringWidth(name) + 22;
                int ph = 24;
                int py = (h - ph) / 2;
                g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 48));
                g.fillRoundRect(x, py, pw, ph, ph, ph);
                g.setColor(darker(base));
                g.drawString(name, x + 11, baseline(g, h));
                x += pw + 10;
            }
            if (!note.isEmpty()) {
                g.setFont(f(Font.PLAIN, 12));
                g.setColor(new Color(96, 70, 78));
                g.drawString(fit(note, g.getFontMetrics(), w - x - 8), x, baseline(g, h));
            }
        }
    }

    private class AmountCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            double amount = 0;
            try {
                amount = Double.parseDouble(text().trim());
            } catch (NumberFormatException ignored) {
            }
            DecimalFormat fmt = new DecimalFormat("#,##0.##");
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            String label = fmt.format(amount);
            int tw = Math.max(fm.stringWidth(label), fm.stringWidth("00,000"));
            int bw = Math.max(16, w - 28 - tw - 10);
            int by = (h - 8) / 2;
            g.setColor(new Color(240, 230, 234));
            g.fillRoundRect(14, by, bw, 8, 8, 8);
            double ratio = maxAmount <= 0 ? 0 : amount / maxAmount;
            int len = Math.max(6, (int) (bw * ratio));
            g.setPaint(new GradientPaint(14, 0, new Color(150, 44, 74), 14 + len, 0, new Color(224, 120, 100)));
            g.fillRoundRect(14, by, len, 8, 8, 8);
            g.setColor(INK);
            g.drawString(label, 14 + bw + 10, baseline(g, h));
        }
    }

    private static class DateCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            int x = 12;
            int y = (h - 12) / 2;
            g.setColor(new Color(150, 110, 122));
            g.setStroke(new BasicStroke(1.3f));
            g.drawRoundRect(x, y, 12, 12, 3, 3);
            g.drawLine(x, y + 4, x + 12, y + 4);
            g.drawLine(x + 3, y - 2, x + 3, y + 1);
            g.drawLine(x + 9, y - 2, x + 9, y + 1);

            g.setFont(f(Font.PLAIN, 12));
            g.setColor(new Color(96, 70, 78));
            g.drawString(fit(text(), g.getFontMetrics(), w - 40), x + 20, baseline(g, h));
        }
    }
}