package com.agrimonitor.gui;

import com.agrimonitor.service.ProfitLossService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfitLossPanel extends JPanel {

    private static final String CUR = "Tk ";
    private static final Color INK_TOP = new Color(19, 34, 48);
    private static final Color INK_MID = new Color(36, 62, 84);
    private static final Color INK_BOTTOM = new Color(10, 21, 32);
    private static final Color GOLD = new Color(226, 190, 108);
    private static final Color COST_C = new Color(255, 138, 101);
    private static final Color SALES_C = new Color(52, 224, 166);
    private static final Color PROFIT_C = new Color(255, 205, 90);
    private static final Color LOSS_C = new Color(255, 100, 100);
    private static final Color OTHER_C = new Color(110, 135, 145);
    private static final Color POS_TEXT = new Color(134, 239, 172);
    private static final Color NEG_TEXT = new Color(255, 150, 140);
    private static final Color MUTED = new Color(140, 170, 185);
    private static final Color SOFT = new Color(210, 226, 232);
    private static final Color[] NEON = {
            new Color(52, 224, 166),
            new Color(90, 170, 255),
            new Color(170, 110, 255),
            new Color(255, 205, 90),
            new Color(255, 138, 101),
            new Color(236, 100, 200)
    };
    private static final Color[] DEEP = {
            new Color(31, 122, 92),
            new Color(48, 110, 190),
            new Color(130, 80, 200),
            new Color(190, 140, 36),
            new Color(214, 100, 64),
            new Color(190, 70, 150)
    };

    private final ProfitLossService service = new ProfitLossService();
    private final DefaultTableModel model = UIKit.model("Crop ID", "Crop Name", "Total Cost", "Total Sales", "Profit / Loss");
    private final JTable table = new JTable(model);

    private final BalanceCard balance = new BalanceCard();
    private final InsightStrip insights = new InsightStrip();
    private final BreakdownCard breakdown = new BreakdownCard();

    public ProfitLossPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        ScrollBody body = new ScrollBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(20, 24, 24, 24));

        fixHeight(balance, 214);
        body.add(balance);
        body.add(Box.createVerticalStrut(16));

        fixHeight(insights, 78);
        body.add(insights);
        body.add(Box.createVerticalStrut(16));

        fixHeight(breakdown, 340);
        body.add(breakdown);
        body.add(Box.createVerticalStrut(18));

        UIKit.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setCellRenderer(new IdCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new MoneyCell(new Color(190, 98, 62)));
        table.getColumnModel().getColumn(3).setCellRenderer(new MoneyCell(new Color(24, 132, 98)));
        table.getColumnModel().getColumn(4).setCellRenderer(new ResultCell());
        table.getColumnModel().getColumn(0).setMaxWidth(120);

        JButton refresh = UITheme.createStyledButton("Refresh", UITheme.TEXT_LIGHT);
        refresh.addActionListener(e -> loadData());

        JComponent tableCard = UIKit.tableCard("Crop-wise Profit / Loss", table, "Search crops...", refresh);
        fixHeight(tableCard, 400);
        body.add(tableCard);
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UITheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadData();
            }
        });

        loadData();
    }

    private static void fixHeight(JComponent c, int h) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.setPreferredSize(new Dimension(10, h));
        c.setMinimumSize(new Dimension(0, h));
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
    }

    private void loadData() {
        double cost = service.calculateTotalCost();
        double sales = service.calculateTotalSales();
        double margin = service.calculateProfitMarginPercent();

        model.setRowCount(0);
        List<Object[]> rows = service.getCropWiseProfitLoss();
        List<String> names = new ArrayList<>();
        List<Double> salesList = new ArrayList<>();

        String bestName = "-";
        String worstName = "-";
        double best = Double.NEGATIVE_INFINITY;
        double worst = Double.POSITIVE_INFINITY;

        if (rows != null) {
            for (Object[] r : rows) {
                model.addRow(r);
                String name = r.length > 1 && r[1] != null ? r[1].toString() : String.valueOf(r[0]);
                double s = r.length > 3 ? num(r[3]) : 0;
                double p = r.length > 4 ? num(r[4]) : 0;
                names.add(name);
                salesList.add(s);
                if (p > best) {
                    best = p;
                    bestName = name;
                }
                if (p < worst) {
                    worst = p;
                    worstName = name;
                }
            }
        }

        int count = names.size();
        double[] sArr = new double[count];
        for (int i = 0; i < count; i++) sArr[i] = salesList.get(i);

        balance.set(cost, sales, margin, count);
        breakdown.setData(names, sArr, cost, sales);

        InsightStrip.Tile t1;
        InsightStrip.Tile t2;
        InsightStrip.Tile t3;
        if (count > 0) {
            t1 = new InsightStrip.Tile("BEST PERFORMER", bestName, fmtSigned(best), new Color(30, 150, 110));
            if (worst < 0) {
                t2 = new InsightStrip.Tile("NEEDS ATTENTION", worstName, fmtSigned(worst), new Color(206, 72, 64));
            } else {
                t2 = new InsightStrip.Tile("NEEDS ATTENTION", "None", "Every crop is in profit", new Color(196, 150, 60));
            }
        } else {
            t1 = new InsightStrip.Tile("BEST PERFORMER", "-", "No crop data yet", new Color(30, 150, 110));
            t2 = new InsightStrip.Tile("NEEDS ATTENTION", "-", "No crop data yet", new Color(196, 150, 60));
        }
        if (sales > 0) {
            t3 = new InsightStrip.Tile("COST VS SALES", String.format("%.0f%% of sales spent", cost / sales * 100),
                    "Cost " + fmtMoney(cost) + "  /  Sales " + fmtMoney(sales), new Color(224, 110, 70));
        } else {
            t3 = new InsightStrip.Tile("COST VS SALES", "No sales yet", "Cost " + fmtMoney(cost), new Color(224, 110, 70));
        }
        insights.set(t1, t2, t3);
        table.repaint();
    }

    private static Font f(int style, int size) {
        return new Font(UIKit.FAMILY, style, size);
    }

    private static double num(Object o) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o == null) return 0;
        try {
            return Double.parseDouble(o.toString().replaceAll("[^0-9.\\-]", ""));
        } catch (Exception e) {
            return 0;
        }
    }

    private static String fmtMoney(double v) {
        return CUR + String.format("%,.0f", v);
    }

    private static String fmtSigned(double v) {
        return (v >= 0 ? "+ " : "- ") + fmtMoney(Math.abs(v));
    }

    private static String fit(String s, FontMetrics fm, int maxW) {
        if (fm.stringWidth(s) <= maxW) return s;
        String t = s;
        while (t.length() > 1 && fm.stringWidth(t + "...") > maxW) t = t.substring(0, t.length() - 1);
        return t + "...";
    }

    private static void centered(Graphics2D g, String s, double cx, double y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) y);
    }

    private static Color deepFor(Object key) {
        String s = String.valueOf(key);
        return DEEP[Math.abs(s.hashCode()) % DEEP.length];
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static class ScrollBody extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(40, visibleRect.height - 40);
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

    private static class BalanceCard extends JPanel {
        private double cost;
        private double sales;
        private double margin;
        private int crops;
        private double t = 3;
        private long startMs;
        private final Timer timer;

        BalanceCard() {
            setOpaque(false);
            timer = new Timer(16, e -> {
                t = (System.currentTimeMillis() - startMs) / 1000.0;
                if (t >= 2.6) {
                    t = 2.6;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
        }

        void set(double cost, double sales, double margin, int crops) {
            this.cost = cost;
            this.sales = sales;
            this.margin = margin;
            this.crops = crops;
            t = 0;
            startMs = System.currentTimeMillis();
            timer.restart();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 24, 24);
            g.setPaint(new GradientPaint(0, 0, INK_MID, w, h, INK_BOTTOM));
            g.fill(shape);
            g.setClip(shape);

            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(255, 255, 255, 9));
            for (int y = 28; y < h; y += 28) g.drawLine(0, y, w, y);
            g.setColor(new Color(226, 190, 108, 70));
            g.drawLine(20, 0, 20, h);
            g.setColor(UITheme.ACCENT);
            g.fillRect(0, h - 4, w, 4);

            boolean showScale = w >= 680;
            double net = sales - cost;
            boolean profit = net >= 0;
            double k = Math.min(1, t / 1.1);
            double ease = 1 - Math.pow(1 - k, 3);
            double shown = net * ease;

            g.setFont(f(Font.BOLD, 12));
            g.setColor(GOLD);
            g.drawString(profit ? "NET PROFIT" : "NET LOSS", 42, 40);

            String big = (shown >= 0 ? "+ " : "- ") + fmtMoney(Math.abs(shown));
            int avail = showScale ? (w - 112 - 125 - 42) : (w - 84);
            int size = 44;
            Font bf = f(Font.BOLD, size);
            while (size > 26 && g.getFontMetrics(bf).stringWidth(big) > avail) {
                size -= 2;
                bf = f(Font.BOLD, size);
            }
            g.setFont(bf);
            g.setColor(profit ? POS_TEXT : NEG_TEXT);
            g.drawString(big, 42, 92);

            g.setFont(f(Font.PLAIN, 13));
            g.setColor(MUTED);
            g.drawString("Across " + crops + (crops == 1 ? " crop" : " crops") + "  |  Sales minus cost", 42, 118);

            int lineEnd = showScale ? (w - 112 - 125) : (w - 42);
            g.setColor(new Color(255, 255, 255, 36));
            g.drawLine(42, 136, Math.max(42, lineEnd), 136);

            String[] labels = {"TOTAL COST", "TOTAL SALES", "MARGIN"};
            String[] values = {fmtMoney(cost), fmtMoney(sales), String.format("%.1f%%", margin)};
            int colW = 138;
            for (int i = 0; i < 3; i++) {
                int x = 42 + i * colW;
                g.setFont(f(Font.BOLD, 11));
                g.setColor(GOLD);
                g.drawString(labels[i], x, 160);
                g.setFont(f(Font.BOLD, 17));
                g.setColor(Color.WHITE);
                g.drawString(values[i], x, 183);
            }

            if (showScale) drawScale(g, w, h);
            g.dispose();
        }

        private void drawScale(Graphics2D g, int w, int h) {
            double cx = w - 112;
            double pivotY = 48;
            double half = 66;
            double hang = 58;
            double pan = 26;
            double baseY = h - 20;
            double maxV = Math.max(1, Math.max(cost, sales));
            double ratio = (sales - cost) / maxV;
            double target = Math.toRadians(13) * ratio;
            double a = target * (1 - Math.exp(-3.2 * t) * Math.cos(9 * t));
            double lx = cx - half * Math.cos(a);
            double ly = pivotY - half * Math.sin(a);
            double rx = cx + half * Math.cos(a);
            double ry = pivotY + half * Math.sin(a);

            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(226, 190, 108, 150));
            g.draw(new Line2D.Double(cx, pivotY, cx, baseY));
            g.setColor(new Color(226, 190, 108, 210));
            g.fill(new RoundRectangle2D.Double(cx - 36, baseY - 3, 72, 8, 8, 8));

            drawPan(g, lx, ly, hang, pan, cost, maxV, COST_C, "COST");
            drawPan(g, rx, ry, hang, pan, sales, maxV, SALES_C, "SALES");

            g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(GOLD);
            g.draw(new Line2D.Double(lx, ly, rx, ry));
            g.fill(new Ellipse2D.Double(cx - 7, pivotY - 7, 14, 14));
            g.setColor(INK_BOTTOM);
            g.fill(new Ellipse2D.Double(cx - 3, pivotY - 3, 6, 6));
        }

        private void drawPan(Graphics2D g, double ex, double ey, double hang, double pan,
                             double value, double maxV, Color c, String label) {
            double py = ey + hang;
            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(255, 255, 255, 110));
            g.draw(new Line2D.Double(ex, ey, ex - pan, py));
            g.draw(new Line2D.Double(ex, ey, ex + pan, py));

            g.setColor(new Color(214, 180, 100));
            g.fill(new Arc2D.Double(ex - pan, py - 9, pan * 2, 18, 180, 180, Arc2D.CHORD));

            int n = value <= 0 ? 0 : (int) Math.max(1, Math.round(6 * value / maxV));
            for (int i = 0; i < n; i++) {
                double cyy = py - 3 - 4.5 * i;
                Ellipse2D coin = new Ellipse2D.Double(ex - 14, cyy - 3.5, 28, 7);
                g.setColor(c);
                g.fill(coin);
                g.setColor(c.darker());
                g.draw(coin);
            }

            g.setFont(f(Font.BOLD, 10));
            g.setColor(MUTED);
            centered(g, label, ex, py + 26);
        }
    }

    private static class InsightStrip extends JPanel {
        static class Tile {
            final String kicker;
            final String main;
            final String sub;
            final Color accent;

            Tile(String kicker, String main, String sub, Color accent) {
                this.kicker = kicker;
                this.main = main;
                this.sub = sub;
                this.accent = accent;
            }
        }

        private Tile[] tiles = {
                new Tile("BEST PERFORMER", "-", " ", new Color(30, 150, 110)),
                new Tile("NEEDS ATTENTION", "-", " ", new Color(196, 150, 60)),
                new Tile("COST VS SALES", "-", " ", new Color(224, 110, 70))
        };
        private int hover = -1;

        InsightStrip() {
            setOpaque(false);
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int idx = tileAt(e.getX());
                    if (idx != hover) {
                        hover = idx;
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

        void set(Tile a, Tile b, Tile c) {
            tiles = new Tile[]{a, b, c};
            repaint();
        }

        private int tileAt(int x) {
            double gap = 14;
            double tw = (getWidth() - 2 * gap) / 3.0;
            for (int i = 0; i < 3; i++) {
                double x0 = i * (tw + gap);
                if (x >= x0 && x <= x0 + tw) return i;
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            double gap = 14;
            double tw = (w - 2 * gap) / 3.0;

            for (int i = 0; i < 3; i++) {
                Tile tile = tiles[i];
                double x0 = i * (tw + gap);
                RoundRectangle2D shape = new RoundRectangle2D.Double(x0, 0, tw - 1, h - 1, 16, 16);
                g.setColor(Color.WHITE);
                g.fill(shape);

                Shape oldClip = g.getClip();
                g.setClip(shape);
                g.setColor(tile.accent);
                g.fill(new Rectangle2D.Double(x0, 0, 5, h));
                g.setClip(oldClip);

                if (hover == i) {
                    g.setColor(tile.accent);
                    g.setStroke(new BasicStroke(1.6f));
                } else {
                    g.setColor(new Color(226, 220, 205));
                    g.setStroke(new BasicStroke(1f));
                }
                g.draw(shape);

                int tx = (int) x0 + 20;
                int maxW = (int) tw - 36;

                g.setFont(f(Font.BOLD, 11));
                g.setColor(new Color(120, 128, 122));
                g.drawString(tile.kicker, tx, 22);

                g.setFont(f(Font.BOLD, 17));
                g.setColor(UITheme.TEXT_DARK);
                g.drawString(fit(tile.main, g.getFontMetrics(), maxW), tx, 44);

                g.setFont(f(Font.PLAIN, 12));
                g.setColor(tile.accent.darker());
                g.drawString(fit(tile.sub, g.getFontMetrics(), maxW), tx, 63);
            }
            g.dispose();
        }
    }

    private static class Slice {
        final String label;
        final double value;
        final Color color;

        Slice(String label, double value, Color color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private static class BreakdownCard extends JPanel {
        private static final String[] TABS = {"By Crop", "Cost vs Result"};

        private List<String> names = new ArrayList<>();
        private double[] sales = new double[0];
        private double totalCost;
        private double totalSales;
        private int mode = 0;
        private List<Slice> slices = new ArrayList<>();
        private double total;
        private String centerTitle = "TOTAL SALES";
        private String centerValue = "";
        private int hover = -1;
        private double progress = 1;
        private long startMs;
        private final Timer timer;
        private final Rectangle2D[] tabRects = {new Rectangle2D.Double(), new Rectangle2D.Double()};
        private final List<Rectangle2D> rowRects = new ArrayList<>();
        private double cx;
        private double cy;
        private double rOut;
        private double rIn;

        BreakdownCard() {
            setOpaque(false);
            timer = new Timer(16, e -> {
                progress = Math.min(1, (System.currentTimeMillis() - startMs) / 900.0);
                repaint();
                if (progress >= 1) ((Timer) e.getSource()).stop();
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    updateHover(e.getX(), e.getY());
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    setCursor(Cursor.getDefaultCursor());
                    if (hover != -1) {
                        hover = -1;
                        repaint();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    int tab = tabAt(e.getX(), e.getY());
                    if (tab >= 0 && tab != mode) {
                        mode = tab;
                        hover = -1;
                        buildSlices();
                        startAnim();
                    }
                }
            });
        }

        void setData(List<String> names, double[] sales, double totalCost, double totalSales) {
            this.names = names;
            this.sales = sales;
            this.totalCost = totalCost;
            this.totalSales = totalSales;
            hover = -1;
            buildSlices();
            startAnim();
        }

        private void startAnim() {
            progress = 0;
            startMs = System.currentTimeMillis();
            timer.restart();
        }

        private void buildSlices() {
            slices = new ArrayList<>();
            if (mode == 0) {
                int n = names.size();
                Integer[] idx = new Integer[n];
                for (int i = 0; i < n; i++) idx[i] = i;
                Arrays.sort(idx, (a, b) -> Double.compare(sales[b], sales[a]));
                double others = 0;
                int count = 0;
                for (int k = 0; k < n; k++) {
                    int i = idx[k];
                    if (sales[i] <= 0) continue;
                    if (count < 5) {
                        slices.add(new Slice(names.get(i), sales[i], NEON[count % NEON.length]));
                        count++;
                    } else {
                        others += sales[i];
                    }
                }
                if (others > 0) slices.add(new Slice("Others", others, OTHER_C));
                centerTitle = "TOTAL SALES";
                centerValue = fmtMoney(totalSales);
            } else {
                double net = totalSales - totalCost;
                if (net >= 0) {
                    slices.add(new Slice("Cost", totalCost, COST_C));
                    slices.add(new Slice("Profit", net, PROFIT_C));
                    centerTitle = "TOTAL SALES";
                    centerValue = fmtMoney(totalSales);
                } else {
                    slices.add(new Slice("Sales", totalSales, SALES_C));
                    slices.add(new Slice("Loss", -net, LOSS_C));
                    centerTitle = "TOTAL COST";
                    centerValue = fmtMoney(totalCost);
                }
            }
            total = 0;
            for (Slice s : slices) total += s.value;
        }

        private int tabAt(int x, int y) {
            for (int i = 0; i < tabRects.length; i++) {
                if (tabRects[i].contains(x, y)) return i;
            }
            return -1;
        }

        private void updateHover(int x, int y) {
            int tab = tabAt(x, y);
            int idx = tab >= 0 ? -1 : hitSlice(x, y);
            setCursor(tab >= 0 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            if (idx != hover) {
                hover = idx;
                repaint();
            }
        }

        private int hitSlice(int x, int y) {
            for (int i = 0; i < rowRects.size(); i++) {
                if (rowRects.get(i).contains(x, y)) return i;
            }
            if (rOut <= 0 || total <= 0) return -1;
            double dx = x - cx;
            double dy = y - cy;
            double dist = Math.hypot(dx, dy);
            if (dist < rIn - 2 || dist > rOut + 6) return -1;
            double theta = (90 - Math.toDegrees(Math.atan2(-dy, dx)) + 720) % 360;
            double acc = 0;
            for (int i = 0; i < slices.size(); i++) {
                double sw = 360.0 * slices.get(i).value / total;
                if (theta < acc + sw) return i;
                acc += sw;
            }
            return -1;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            g.setPaint(new GradientPaint(0, 0, INK_TOP, 0, h, INK_BOTTOM));
            g.fill(new RoundRectangle2D.Double(0, 0, w, h, 24, 24));
            g.setColor(new Color(255, 255, 255, 28));
            g.setStroke(new BasicStroke(1f));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1, h - 1, 24, 24));

            g.setFont(f(Font.BOLD, 17));
            g.setColor(Color.WHITE);
            g.drawString("Money Breakdown", 26, 36);
            g.setFont(f(Font.PLAIN, 12));
            g.setColor(MUTED);
            g.drawString(mode == 0 ? "Share of total sales by crop" : "Where the sales money went", 26, 56);

            g.setFont(f(Font.BOLD, 12));
            FontMetrics tfm = g.getFontMetrics();
            double tx = w - 26;
            for (int i = TABS.length - 1; i >= 0; i--) {
                double tw = tfm.stringWidth(TABS[i]) + 28;
                tx -= tw;
                tabRects[i] = new Rectangle2D.Double(tx, 20, tw, 28);
                if (i == mode) {
                    g.setColor(GOLD);
                    g.fill(new RoundRectangle2D.Double(tx, 20, tw, 28, 28, 28));
                    g.setColor(new Color(30, 24, 10));
                } else {
                    g.setColor(new Color(255, 255, 255, 18));
                    g.fill(new RoundRectangle2D.Double(tx, 20, tw, 28, 28, 28));
                    g.setColor(SOFT);
                }
                g.drawString(TABS[i], (float) (tx + 14), (float) 39);
                tx -= 8;
            }

            double bodyTop = 76;
            double bodyBottom = h - 22;
            double bodyH = bodyBottom - bodyTop;
            double zoneW = Math.min(w * 0.45, 300);
            double d = Math.min(Math.min(bodyH, zoneW - 20), 250);
            if (d < 90) d = 90;
            double th = d * 0.18;
            cx = 26 + zoneW / 2;
            cy = bodyTop + bodyH / 2;
            rOut = d / 2;
            rIn = d / 2 - th;
            double rMid = (d - th) / 2;

            rowRects.clear();

            g.setStroke(new BasicStroke((float) th, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g.setColor(new Color(255, 255, 255, 18));
            g.draw(new Ellipse2D.Double(cx - rMid, cy - rMid, rMid * 2, rMid * 2));

            double ri = rIn - 8;
            if (ri > 10) {
                g.setStroke(new BasicStroke(1.2f));
                g.setColor(new Color(255, 255, 255, 40));
                g.draw(new Ellipse2D.Double(cx - ri, cy - ri, ri * 2, ri * 2));
            }

            if (total <= 0) {
                g.setFont(f(Font.PLAIN, 13));
                g.setColor(MUTED);
                centered(g, "No data", cx, cy + 4);
                g.dispose();
                return;
            }

            double ease = 1 - Math.pow(1 - progress, 3);
            double limit = 360 * ease;
            int active = 0;
            for (Slice s : slices) if (s.value > 0) active++;
            double gapDeg = active > 1 ? 3.0 : 0.0;
            double acc = 0;
            int hv = hover < slices.size() ? hover : -1;

            for (int i = 0; i < slices.size(); i++) {
                Slice s = slices.get(i);
                if (s.value <= 0) continue;
                double sweep = 360.0 * s.value / total;
                double vis = Math.max(0, Math.min(sweep, limit - acc));
                if (vis > 0) {
                    double drawSweep = Math.max(vis - gapDeg, 0.6);
                    float sw = (float) (i == hv ? th + 10 : th);
                    Color c = s.color;
                    if (hv >= 0 && i != hv) c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 140);
                    g.setStroke(new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                    g.setColor(c);
                    g.draw(new Arc2D.Double(cx - rMid, cy - rMid, rMid * 2, rMid * 2,
                            90 - acc - gapDeg / 2, -drawSweep, Arc2D.OPEN));
                    if (ease > 0.98 && sweep >= 24) {
                        double mid = Math.toRadians(90 - acc - sweep / 2);
                        g.setFont(f(Font.BOLD, 11));
                        g.setColor(INK_BOTTOM);
                        centered(g, String.format("%.0f%%", 100 * s.value / total),
                                cx + rMid * Math.cos(mid), cy - rMid * Math.sin(mid) + 4);
                    }
                }
                acc += sweep;
            }

            double innerW = Math.max(40, ri * 2 - 16);
            if (hv >= 0) {
                Slice s = slices.get(hv);
                g.setFont(f(Font.BOLD, 12));
                g.setColor(SOFT);
                centered(g, fit(s.label, g.getFontMetrics(), (int) innerW), cx, cy - 12);
                g.setFont(f(Font.BOLD, 24));
                g.setColor(Color.WHITE);
                centered(g, String.format("%.1f%%", 100 * s.value / total), cx, cy + 14);
                g.setFont(f(Font.PLAIN, 11));
                g.setColor(MUTED);
                centered(g, fit(fmtMoney(s.value), g.getFontMetrics(), (int) innerW), cx, cy + 32);
            } else {
                g.setFont(f(Font.BOLD, 10));
                g.setColor(MUTED);
                centered(g, centerTitle, cx, cy - 6);
                int size = 18;
                Font vf = f(Font.BOLD, size);
                while (size > 10 && g.getFontMetrics(vf).stringWidth(centerValue) > innerW) {
                    size--;
                    vf = f(Font.BOLD, size);
                }
                g.setFont(vf);
                g.setColor(Color.WHITE);
                centered(g, centerValue, cx, cy + 16);
            }

            double lx = 26 + zoneW + 34;
            double lw = w - 26 - lx;
            int m = slices.size();
            if (m > 0 && lw > 120) {
                double entryH = Math.min(48, bodyH / m);
                double rowH = entryH - 4;
                double y0 = cy - m * entryH / 2;
                for (int i = 0; i < m; i++) {
                    Slice s = slices.get(i);
                    double y = y0 + i * entryH;
                    rowRects.add(new Rectangle2D.Double(lx - 8, y, lw + 16, rowH));

                    if (hv == i) {
                        g.setColor(new Color(255, 255, 255, 18));
                        g.fill(new RoundRectangle2D.Double(lx - 8, y, lw + 16, rowH, 12, 12));
                    }

                    g.setColor(s.color);
                    g.fill(new RoundRectangle2D.Double(lx + 4, y + rowH / 2 - 5, 10, 10, 4, 4));

                    boolean two = rowH >= 34;
                    double ly = two ? y + rowH / 2 - 2 : y + rowH / 2 + 5;

                    g.setFont(f(Font.BOLD, 13));
                    FontMetrics fm = g.getFontMetrics();
                    String pct = String.format("%.1f%%", 100 * s.value / total);
                    g.setColor(SOFT);
                    g.drawString(pct, (float) (lx + lw - fm.stringWidth(pct)), (float) ly);
                    g.setColor(Color.WHITE);
                    g.drawString(fit(s.label, fm, (int) lw - 24 - 62), (float) (lx + 24), (float) ly);

                    if (two) {
                        g.setFont(f(Font.PLAIN, 11));
                        g.setColor(MUTED);
                        g.drawString(fmtMoney(s.value), (float) (lx + 24), (float) (ly + 16));
                    }
                }
            }
            g.dispose();
        }
    }

    private abstract class Cell extends JComponent implements TableCellRenderer {
        Object value;
        boolean selected;
        int mrow;

        Cell() {
            setOpaque(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            value = v;
            selected = sel;
            mrow = t.convertRowIndexToModel(row);
            return this;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(selected ? new Color(226, 238, 246) : Color.WHITE);
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(238, 234, 224));
            g.drawLine(0, h - 1, w, h - 1);
            draw(g, w, h);
            g.dispose();
        }

        abstract void draw(Graphics2D g, int w, int h);
    }

    private class IdCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            String s = String.valueOf(value);
            Color c = deepFor(s);
            g.setFont(f(Font.BOLD, 12));
            int tw = g.getFontMetrics().stringWidth(s);
            int cw = tw + 22;
            int ch = 24;
            int y = (h - ch) / 2;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 32));
            g.fill(new RoundRectangle2D.Double(12, y, cw, ch, ch, ch));
            g.setColor(c);
            g.drawString(s, 23, y + 16);
        }
    }

    private class NameCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            Object id = model.getValueAt(mrow, 0);
            g.setColor(deepFor(id));
            g.fill(new Ellipse2D.Double(12, h / 2.0 - 6, 12, 12));
            g.setFont(f(Font.BOLD, 13));
            g.setColor(UITheme.TEXT_DARK);
            g.drawString(fit(String.valueOf(value), g.getFontMetrics(), w - 44), 32, h / 2 + 5);
        }
    }

    private class MoneyCell extends Cell {
        private final Color color;

        MoneyCell(Color color) {
            this.color = color;
        }

        @Override
        void draw(Graphics2D g, int w, int h) {
            String s = fmtMoney(num(value));
            g.setFont(f(Font.PLAIN, 13));
            g.setColor(color);
            g.drawString(s, w - 16 - g.getFontMetrics().stringWidth(s), h / 2 + 5);
        }
    }

    private class ResultCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            double v = num(value);
            boolean pos = v >= 0;
            String txt = fmtMoney(Math.abs(v));
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int pw = fm.stringWidth(txt) + 40;
            int ph = 24;
            int px = w - pw - 12;
            int py = (h - ph) / 2;
            Color tc = pos ? new Color(22, 128, 86) : new Color(192, 57, 43);

            g.setColor(pos ? new Color(222, 245, 233) : new Color(253, 228, 224));
            g.fill(new RoundRectangle2D.Double(px, py, pw, ph, ph, ph));

            g.setColor(tc);
            double ax = px + 14;
            double ay = h / 2.0;
            Path2D.Double tri = new Path2D.Double();
            if (pos) {
                tri.moveTo(ax - 4, ay + 3);
                tri.lineTo(ax + 4, ay + 3);
                tri.lineTo(ax, ay - 4);
            } else {
                tri.moveTo(ax - 4, ay - 3);
                tri.lineTo(ax + 4, ay - 3);
                tri.lineTo(ax, ay + 4);
            }
            tri.closePath();
            g.fill(tri);
            g.drawString(txt, px + 26, py + 16);

            double s = num(model.getValueAt(mrow, 3));
            if (s > 0) {
                String pct = String.format("%.0f%%", v / s * 100);
                g.setFont(f(Font.PLAIN, 11));
                g.setColor(new Color(130, 138, 132));
                g.drawString(pct, px - 10 - g.getFontMetrics().stringWidth(pct), py + 16);
            }
        }
    }
}