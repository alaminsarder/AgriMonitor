package com.agrimonitor.gui;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.service.ProfitLossService;
import com.agrimonitor.session.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HomePanel extends JPanel {

    private static final Color FOREST = new Color(31, 61, 51);
    private static final Color FOREST_LIGHT = new Color(55, 101, 75);
    private static final Color GOLD = new Color(190, 151, 73);
    private static final Color PALE_GOLD = new Color(230, 210, 158);
    private static final Color LINE = new Color(232, 236, 232);
    private static final Color MUTED = new Color(111, 120, 108);
    private static final Color GREEN = new Color(52, 130, 98);
    private static final Color RED = new Color(185, 78, 69);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH);

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

    private static final String[][] MODULES = {
            {"Field Management", "Field Management", "Plots, locations and area"},
            {"Crop Management", "Crop Management", "Crops and growth cycles"},
            {"Irrigation Management", "Irrigation", "Water usage records"},
            {"Pesticide Log", "Pesticide Log", "Applications and doses"},
            {"Cost Management", "Cost Management", "Crop expenses and spending"},
            {"Sales Management", "Sales Management", "Produce sales and buyers"},
            {"Profit / Loss", "Profit / Loss", "Earnings and performance"},
            {"Weather", "Weather", "Live conditions and advice"},
            {"Report Generation", "Reports", "View and export farm reports"}
    };

    private final Consumer<String> navigate;

    private final JLabel greeting = new JLabel(" ");
    private final JLabel username = new JLabel(" ");
    private final JLabel dateLabel = new JLabel(" ");
    private final JLabel clock = new JLabel(" ");

    private final RingCard salesRing = new RingCard("Total sales", "Share of money flow", GREEN);
    private final RingCard costRing = new RingCard("Total cost", "Share of money flow", GOLD);
    private final RingCard netRing = new RingCard("Net profit", "Profit margin", GREEN);
    private final MiniCard fieldsMini = new MiniCard("Fields", "field", GREEN);
    private final MiniCard cropsMini = new MiniCard("Crops", "crop", GOLD);

    private final ColumnChart moneyChart = new ColumnChart(
            "Money flow",
            "Revenue, expenses and net result",
            "No sales or cost recorded yet"
    );

    private final AreaChart cropChart = new AreaChart(
            "Crop performance",
            "Top crops ranked by profit",
            "No crop data yet"
    );

    private final MonthCalendar calendar = new MonthCalendar();

    private final Timer clockTimer;

    public HomePanel() {
        this(null);
    }

    public HomePanel(Consumer<String> navigate) {
        this.navigate = navigate;

        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);

        ScrollBody body = new ScrollBody();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(UITheme.BACKGROUND);
        body.setBorder(new EmptyBorder(22, 26, 28, 26));

        JComponent hero = buildHero();
        fixHeight(hero, 132);
        body.add(hero);
        body.add(Box.createVerticalStrut(18));

        JPanel rings = new JPanel(new GridLayout(1, 4, 16, 0));
        rings.setOpaque(false);
        rings.add(salesRing);
        rings.add(costRing);
        rings.add(netRing);
        rings.add(buildMiniStack());
        fixHeight(rings, 170);
        body.add(rings);
        body.add(Box.createVerticalStrut(18));

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.add(moneyChart);
        charts.add(cropChart);
        fixHeight(charts, 310);
        body.add(charts);
        body.add(Box.createVerticalStrut(18));

        JPanel bottom = new JPanel(new BorderLayout(16, 0));
        bottom.setOpaque(false);
        calendar.setPreferredSize(new Dimension(340, 10));
        bottom.add(buildDirectory(), BorderLayout.CENTER);
        bottom.add(calendar, BorderLayout.EAST);
        fixHeight(bottom, 360);
        body.add(bottom);

        JScrollPane scroll = new JScrollPane(
                body,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(UITheme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        add(scroll, BorderLayout.CENTER);

        clockTimer = new Timer(1000, e -> tick());
        tick();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                tick();
                clockTimer.start();
                refresh();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                clockTimer.stop();
            }
        });

        refresh();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (isShowing()) {
            clockTimer.start();
        }
    }

    @Override
    public void removeNotify() {
        clockTimer.stop();
        super.removeNotify();
    }

    private static void fixHeight(JComponent component, int height) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        component.setPreferredSize(new Dimension(10, height));
        component.setMinimumSize(new Dimension(0, height));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }

    private JComponent buildMiniStack() {
        JPanel stack = new JPanel(new GridLayout(2, 1, 0, 14));
        stack.setOpaque(false);
        stack.add(fieldsMini);
        stack.add(cropsMini);
        return stack;
    }

    private JComponent buildHero() {
        HeroPanel hero = new HeroPanel();
        hero.setLayout(new BorderLayout(20, 0));
        hero.setBorder(new EmptyBorder(20, 34, 20, 34));

        greeting.setFont(font(Font.BOLD, 11));
        greeting.setForeground(PALE_GOLD);

        username.setFont(font(Font.BOLD, 28));
        username.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Your farm, thoughtfully managed.");
        subtitle.setFont(font(Font.PLAIN, 13));
        subtitle.setForeground(new Color(207, 223, 210));

        JPanel welcome = new JPanel();
        welcome.setOpaque(false);
        welcome.setLayout(new BoxLayout(welcome, BoxLayout.Y_AXIS));

        greeting.setAlignmentX(Component.LEFT_ALIGNMENT);
        username.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        welcome.add(Box.createVerticalGlue());
        welcome.add(greeting);
        welcome.add(Box.createVerticalStrut(4));
        welcome.add(username);
        welcome.add(Box.createVerticalStrut(4));
        welcome.add(subtitle);
        welcome.add(Box.createVerticalGlue());

        JLabel timeCaption = new JLabel("LOCAL TIME");
        timeCaption.setFont(font(Font.BOLD, 10));
        timeCaption.setForeground(PALE_GOLD);

        clock.setFont(font(Font.BOLD, 24));
        clock.setForeground(Color.WHITE);

        dateLabel.setFont(font(Font.PLAIN, 12));
        dateLabel.setForeground(new Color(207, 223, 210));

        timeCaption.setAlignmentX(Component.RIGHT_ALIGNMENT);
        clock.setAlignmentX(Component.RIGHT_ALIGNMENT);
        dateLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel timeBox = new JPanel();
        timeBox.setOpaque(false);
        timeBox.setLayout(new BoxLayout(timeBox, BoxLayout.Y_AXIS));
        timeBox.add(Box.createVerticalGlue());
        timeBox.add(timeCaption);
        timeBox.add(Box.createVerticalStrut(4));
        timeBox.add(clock);
        timeBox.add(Box.createVerticalStrut(4));
        timeBox.add(dateLabel);
        timeBox.add(Box.createVerticalGlue());

        hero.add(welcome, BorderLayout.CENTER);
        hero.add(timeBox, BorderLayout.EAST);

        return hero;
    }

    private JComponent buildDirectory() {
        Surface card = new Surface();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(20, 24, 22, 24));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("Farm directory");
        title.setFont(font(Font.BOLD, 18));
        title.setForeground(FOREST);

        JLabel caption = new JLabel("Select a section to continue");
        caption.setFont(font(Font.PLAIN, 12));
        caption.setForeground(MUTED);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        caption.setAlignmentX(Component.LEFT_ALIGNMENT);
        titles.add(title);
        titles.add(Box.createVerticalStrut(3));
        titles.add(caption);

        JLabel number = new JLabel("09 SECTIONS");
        number.setFont(font(Font.BOLD, 10));
        number.setForeground(GOLD);

        head.add(titles, BorderLayout.WEST);
        head.add(number, BorderLayout.EAST);

        JPanel columns = new JPanel(new GridLayout(1, 2, 16, 0));
        columns.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(5, 1, 0, 6));
        left.setOpaque(false);

        JPanel right = new JPanel(new GridLayout(5, 1, 0, 6));
        right.setOpaque(false);

        for (int i = 0; i < MODULES.length; i++) {
            DirectoryButton button = new DirectoryButton(
                    UITheme.Icons.keyFor(MODULES[i][1]),
                    MODULES[i][1],
                    MODULES[i][2]
            );

            String key = MODULES[i][0];
            button.addActionListener(e -> go(key));

            if (i < 5) {
                left.add(button);
            } else {
                right.add(button);
            }
        }

        JPanel blank = new JPanel();
        blank.setOpaque(false);
        right.add(blank);

        columns.add(left);
        columns.add(right);

        card.add(head, BorderLayout.NORTH);
        card.add(columns, BorderLayout.CENTER);

        return card;
    }

    private void go(String key) {
        if (navigate != null) {
            navigate.accept(key);
        }
    }

    private void tick() {
        LocalTime now = LocalTime.now();
        clock.setText(now.format(TIME_FORMAT));
        dateLabel.setText(LocalDate.now().format(DATE_FORMAT));

        String user = Session.getUsername();
        if (user == null || user.trim().isEmpty()) {
            user = "Farmer";
        }

        int hour = now.getHour();
        String part = hour < 12
                ? "GOOD MORNING"
                : hour < 17 ? "GOOD AFTERNOON" : "GOOD EVENING";

        greeting.setText(part);
        username.setText(user);
    }

    private void refresh() {
        tick();

        new SwingWorker<Snapshot, Void>() {
            @Override
            protected Snapshot doInBackground() {
                return load();
            }

            @Override
            protected void done() {
                try {
                    apply(get());
                } catch (Exception ex) {
                    apply(new Snapshot());
                }
            }
        }.execute();
    }

    private static Snapshot load() {
        Snapshot snapshot = new Snapshot();

        try {
            List<?> fields = new FieldDAO().getAllFields();
            snapshot.fields = fields == null ? 0 : fields.size();
        } catch (Exception ex) {
            snapshot.fields = -1;
        }

        try {
            List<?> crops = new CropDAO().getAllCrops();
            snapshot.crops = crops == null ? 0 : crops.size();
        } catch (Exception ex) {
            snapshot.crops = -1;
        }

        ProfitLossService service = null;

        try {
            service = new ProfitLossService();
        } catch (Exception ex) {
            service = null;
        }

        if (service != null) {
            try {
                snapshot.sales = service.calculateTotalSales();
                snapshot.cost = service.calculateTotalCost();
                snapshot.margin = service.calculateProfitMarginPercent();
                snapshot.hasMoney = true;
            } catch (Exception ex) {
                snapshot.hasMoney = false;
            }

            try {
                for (Object object : service.getCropWiseProfitLoss()) {
                    if (object instanceof Object[]) {
                        Object[] row = (Object[]) object;
                        if (row.length >= 5) {
                            snapshot.rows.add(new CropRow(
                                    String.valueOf(row[1]),
                                    num(row[4])
                            ));
                        }
                    }
                }

                snapshot.rows.sort((a, b) -> Double.compare(b.profit, a.profit));
            } catch (Exception ex) {
                snapshot.rows.clear();
            }
        }

        return snapshot;
    }

    private void apply(Snapshot snapshot) {
        fieldsMini.setValue(snapshot.fields < 0 ? "-" : String.valueOf(snapshot.fields));
        cropsMini.setValue(snapshot.crops < 0 ? "-" : String.valueOf(snapshot.crops));

        List<Bar> moneyBars = new ArrayList<>();

        if (snapshot.hasMoney) {
            double net = snapshot.sales - snapshot.cost;
            double total = Math.abs(snapshot.sales) + Math.abs(snapshot.cost);
            double salesShare = total > 0 ? Math.abs(snapshot.sales) / total : 0;
            double costShare = total > 0 ? Math.abs(snapshot.cost) / total : 0;

            salesRing.setData(money(snapshot.sales), "Share of money flow", salesShare);
            costRing.setData(money(snapshot.cost), "Share of money flow", costShare);

            boolean badMargin = Double.isNaN(snapshot.margin) || Double.isInfinite(snapshot.margin);
            double marginValue = badMargin ? 0 : snapshot.margin;

            netRing.setTitle(net >= 0 ? "Net profit" : "Net loss");
            netRing.setColor(net >= 0 ? GREEN : RED);
            netRing.setData(
                    money(Math.abs(net)),
                    badMargin ? "Margin n/a" : String.format(Locale.US, "Margin %.1f%%", marginValue),
                    Math.abs(marginValue) / 100d
            );

            if (snapshot.sales != 0 || snapshot.cost != 0) {
                moneyBars.add(new Bar("Total sales", money(snapshot.sales), Math.abs(snapshot.sales), GREEN));
                moneyBars.add(new Bar("Total cost", money(snapshot.cost), Math.abs(snapshot.cost), GOLD));
                moneyBars.add(new Bar(
                        net >= 0 ? "Net profit" : "Net loss",
                        (net < 0 ? "-" : "") + money(Math.abs(net)),
                        Math.abs(net),
                        net >= 0 ? new Color(72, 145, 96) : RED
                ));
            }
        } else {
            salesRing.setData("-", " ", 0);
            costRing.setData("-", " ", 0);
            netRing.setTitle("Net profit");
            netRing.setColor(GREEN);
            netRing.setData("-", " ", 0);
        }

        moneyChart.setBars(moneyBars);

        int count = Math.min(6, snapshot.rows.size());
        cropChart.setRows(new ArrayList<>(snapshot.rows.subList(0, count)));
    }

    private static String money(double value) {
        if (Math.abs(value) >= 1_000_000) {
            return String.format(Locale.US, "Tk %.2fM", value / 1_000_000d);
        }
        return "Tk " + String.format(Locale.US, "%,.0f", value);
    }

    private static String signedMoney(double value) {
        return (value < 0 ? "-" : "") + money(Math.abs(value));
    }

    private static double num(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(
                    String.valueOf(value).replaceAll("[^0-9.\\-]", "")
            );
        } catch (Exception ex) {
            return 0;
        }
    }

    private static Font font(int style, int size) {
        return new Font("Segoe UI", style, size);
    }

    private static String clip(String text, FontMetrics metrics, int maxWidth) {
        if (text == null || maxWidth <= 0) {
            return "";
        }

        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }

        String suffix = "...";
        if (metrics.stringWidth(suffix) > maxWidth) {
            return "";
        }

        int length = text.length();
        while (length > 0
                && metrics.stringWidth(text.substring(0, length) + suffix) > maxWidth) {
            length--;
        }

        return text.substring(0, length) + suffix;
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static void centered(Graphics2D g, String text, double cx, double cy) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(
                text,
                (float) (cx - fm.stringWidth(text) / 2.0),
                (float) (cy + (fm.getAscent() - fm.getDescent()) / 2.0)
        );
    }

    private static void header(Graphics2D g, String title, String subtitle) {
        g.setColor(GOLD);
        g.fillRoundRect(24, 24, 4, 30, 4, 4);

        g.setFont(font(Font.BOLD, 16));
        g.setColor(FOREST);
        g.drawString(title, 38, 36);

        g.setFont(font(Font.PLAIN, 12));
        g.setColor(MUTED);
        g.drawString(subtitle, 38, 52);
    }

    private static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static class Snapshot {
        int fields = -1;
        int crops = -1;
        double sales;
        double cost;
        double margin;
        boolean hasMoney;
        final List<CropRow> rows = new ArrayList<>();
    }

    private static class CropRow {
        final String name;
        final double profit;

        CropRow(String name, double profit) {
            this.name = name;
            this.profit = profit;
        }
    }

    private static class Bar {
        final String label;
        final String value;
        final double magnitude;
        final Color color;

        Bar(String label, String value, double magnitude, Color color) {
            this.label = label;
            this.value = value;
            this.magnitude = magnitude;
            this.color = color;
        }
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

    private static class Surface extends JPanel {
        Surface() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth();
            int h = getHeight();

            for (int i = 0; i < 3; i++) {
                g.setColor(new Color(31, 61, 51, 8));
                g.fillRoundRect(i, i + 2, w - 2 * i, h - 2 * i - 2, 28, 28);
            }

            g.setColor(Color.WHITE);
            g.fillRoundRect(3, 1, w - 6, h - 7, 24, 24);

            g.setColor(LINE);
            g.drawRoundRect(3, 1, w - 7, h - 8, 24, 24);

            g.dispose();
        }
    }

    private static class HeroPanel extends JPanel {
        HeroPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth();
            int h = getHeight();

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 26, 26);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(25, 55, 44),
                    w, h, new Color(57, 102, 72)
            ));
            g.fill(shape);

            g.clip(shape);

            g.setColor(new Color(255, 255, 255, 14));
            g.fillOval(w - 220, -90, 260, 260);
            g.fillOval(w - 330, h - 60, 200, 200);

            int cx = w / 2 + 35;
            int cy = h / 2 + 22;

            g.setColor(new Color(221, 205, 150, 38));
            g.setStroke(new BasicStroke(1.5f));

            Path2D stem = new Path2D.Double();
            stem.moveTo(cx, cy + 40);
            stem.curveTo(cx - 2, cy + 12, cx + 3, cy - 12, cx + 14, cy - 38);
            g.draw(stem);

            for (int i = 0; i < 4; i++) {
                int y = cy + 25 - i * 17;
                int x = cx + i * 3;

                Path2D left = new Path2D.Double();
                left.moveTo(x, y);
                left.quadTo(x - 24, y - 3, x - 32, y - 17);
                left.quadTo(x - 10, y - 18, x, y);
                g.draw(left);

                Path2D right = new Path2D.Double();
                right.moveTo(x + 2, y - 6);
                right.quadTo(x + 26, y - 9, x + 34, y - 24);
                right.quadTo(x + 12, y - 24, x + 2, y - 6);
                g.draw(right);
            }

            g.setColor(GOLD);
            g.fillRect(34, h - 4, Math.max(0, w - 68), 2);

            g.dispose();
        }
    }

    private static class RingCard extends Surface {
        private String title;
        private String value = "-";
        private String sub;
        private double pct;
        private Color color;
        private final Timer animation;
        private float progress = 1f;

        RingCard(String title, String sub, Color color) {
            this.title = title;
            this.sub = sub;
            this.color = color;

            animation = new Timer(16, e -> {
                progress += 0.04f;
                if (progress >= 1f) {
                    progress = 1f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
        }

        void setData(String value, String sub, double pct) {
            this.value = value;
            this.sub = sub;
            this.pct = Math.max(0, Math.min(1, pct));
            progress = 0f;
            animation.restart();
        }

        void setTitle(String title) {
            this.title = title;
            repaint();
        }

        void setColor(Color color) {
            this.color = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth();
            int h = getHeight();

            g.setFont(font(Font.BOLD, 13));
            g.setColor(MUTED);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(clip(title, fm, w - 48), 26, 36);

            int d = Math.max(44, Math.min(h - 78, 92));
            int rx = 26;
            int ry = 52 + Math.max(0, (h - 14 - 52 - d) / 2);
            float stroke = 9f;

            g.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(alpha(color, 38));
            g.drawOval(rx + 5, ry + 5, d - 10, d - 10);

            float eased = 1f - (float) Math.pow(1f - progress, 3);
            double shown = pct * eased;

            if (shown > 0.002) {
                g.setColor(color);
                g.draw(new Arc2D.Double(rx + 5, ry + 5, d - 10, d - 10, 90, -360 * shown, Arc2D.OPEN));
            }

            g.setFont(font(Font.BOLD, 15));
            g.setColor(FOREST);
            centered(g, Math.round(pct * 100 * eased) + "%", rx + d / 2.0, ry + d / 2.0);

            int tx = rx + d + 20;
            int available = Math.max(0, w - tx - 18);

            g.setFont(font(Font.BOLD, 22));
            g.setColor(FOREST);
            fm = g.getFontMetrics();
            g.drawString(clip(value, fm, available), tx, ry + d / 2 + 2);

            g.setFont(font(Font.PLAIN, 12));
            g.setColor(MUTED);
            fm = g.getFontMetrics();
            g.drawString(clip(sub, fm, available), tx, ry + d / 2 + 22);

            g.dispose();
        }
    }

    private static class MiniCard extends Surface {
        private final String title;
        private final String iconKey;
        private final Color color;
        private String value = "-";

        MiniCard(String title, String iconKey, Color color) {
            this.title = title;
            this.iconKey = iconKey;
            this.color = color;
        }

        void setValue(String value) {
            this.value = value;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int h = getHeight() - 6;
            int cs = Math.max(30, Math.min(46, h - 24));
            int cx = 22;
            int cy = (h - cs) / 2 + 1;

            g.setColor(alpha(color, 40));
            g.fillRoundRect(cx, cy, cs, cs, 16, 16);

            UITheme.Icons.paint(g, iconKey, cx + (cs - 22) / 2.0, cy + (cs - 22) / 2.0, 22, color.darker());

            int tx = cx + cs + 16;
            int mid = h / 2 + 1;

            g.setFont(font(Font.PLAIN, 12));
            g.setColor(MUTED);
            g.drawString(title, tx, mid - 5);

            g.setFont(font(Font.BOLD, 22));
            g.setColor(FOREST);
            g.drawString(value, tx, mid + 20);

            g.dispose();
        }
    }

    private static class ColumnChart extends Surface {
        private final String title;
        private final String subtitle;
        private final String emptyText;
        private final Timer animation;

        private List<Bar> rows = new ArrayList<>();
        private boolean ready;
        private float progress = 1f;

        ColumnChart(String title, String subtitle, String emptyText) {
            this.title = title;
            this.subtitle = subtitle;
            this.emptyText = emptyText;

            animation = new Timer(16, e -> {
                progress += 0.045f;
                if (progress >= 1f) {
                    progress = 1f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
        }

        void setBars(List<Bar> rows) {
            this.rows = new ArrayList<>(rows);
            ready = true;
            progress = 0f;
            animation.restart();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth();
            int h = getHeight();

            header(g, title, subtitle);

            if (rows.isEmpty()) {
                String message = ready ? emptyText : "Loading...";
                g.setFont(font(Font.PLAIN, 13));
                g.setColor(MUTED);
                FontMetrics fm = g.getFontMetrics();
                String shown = clip(message, fm, Math.max(0, w - 44));
                g.drawString(shown, Math.max(24, (w - fm.stringWidth(shown)) / 2), h / 2 + 12);
                g.dispose();
                return;
            }

            int left = 28;
            int right = w - 28;
            int top = 86;
            int bottom = h - 50;

            g.setStroke(new BasicStroke(1f));
            for (int k = 0; k <= 4; k++) {
                int y = top + (bottom - top) * k / 4;
                g.setColor(k == 4 ? new Color(214, 222, 216) : new Color(238, 242, 239));
                g.drawLine(left, y, right, y);
            }

            int n = rows.size();
            double slot = (right - left) / (double) n;
            int barW = (int) Math.max(18, Math.min(70, slot * 0.45));
            int fullH = bottom - top - 22;

            double max = 0;
            for (Bar bar : rows) {
                max = Math.max(max, bar.magnitude);
            }

            float eased = 1f - (float) Math.pow(1f - progress, 3);

            for (int i = 0; i < n; i++) {
                Bar bar = rows.get(i);
                double cx = left + slot * i + slot / 2;
                int bx = (int) Math.round(cx - barW / 2.0);

                g.setColor(alpha(bar.color, 28));
                g.fillRoundRect(bx, bottom - fullH, barW, fullH, 16, 16);

                int bh = max > 0 ? (int) Math.round(fullH * (bar.magnitude / max) * eased) : 0;
                if (bar.magnitude > 0) {
                    bh = Math.max(4, bh);
                }

                if (bh > 0) {
                    g.setPaint(new GradientPaint(
                            0, bottom - bh, alpha(bar.color, 170),
                            0, bottom, bar.color
                    ));
                    g.fillRoundRect(bx, bottom - bh, barW, bh, 16, 16);
                }

                g.setFont(font(Font.BOLD, 12));
                g.setColor(bar.color.darker());
                FontMetrics fm = g.getFontMetrics();
                String value = clip(bar.value, fm, (int) slot - 6);
                g.drawString(value, (float) (cx - fm.stringWidth(value) / 2.0), bottom - bh - 8);

                g.setFont(font(Font.PLAIN, 12));
                g.setColor(MUTED);
                fm = g.getFontMetrics();
                String label = clip(bar.label, fm, (int) slot - 8);
                g.drawString(label, (float) (cx - fm.stringWidth(label) / 2.0), bottom + 22);
            }

            g.dispose();
        }
    }

    private static class AreaChart extends Surface {
        private final String title;
        private final String subtitle;
        private final String emptyText;
        private final Timer animation;

        private List<CropRow> rows = new ArrayList<>();
        private boolean ready;
        private float progress = 1f;

        AreaChart(String title, String subtitle, String emptyText) {
            this.title = title;
            this.subtitle = subtitle;
            this.emptyText = emptyText;

            animation = new Timer(16, e -> {
                progress += 0.03f;
                if (progress >= 1f) {
                    progress = 1f;
                    ((Timer) e.getSource()).stop();
                }
                repaint();
            });
        }

        void setRows(List<CropRow> rows) {
            this.rows = new ArrayList<>(rows);
            ready = true;
            progress = 0f;
            animation.restart();
        }

        private static Path2D curve(double[] xs, double[] ys) {
            Path2D p = new Path2D.Double();
            p.moveTo(xs[0], ys[0]);
            int n = xs.length;
            for (int i = 0; i < n - 1; i++) {
                int i0 = Math.max(0, i - 1);
                int i2 = i + 1;
                int i3 = Math.min(n - 1, i + 2);
                double c1x = xs[i] + (xs[i2] - xs[i0]) / 6.0;
                double c1y = ys[i] + (ys[i2] - ys[i0]) / 6.0;
                double c2x = xs[i2] - (xs[i3] - xs[i]) / 6.0;
                double c2y = ys[i2] - (ys[i3] - ys[i]) / 6.0;
                p.curveTo(c1x, c1y, c2x, c2y, xs[i2], ys[i2]);
            }
            return p;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth();
            int h = getHeight();

            header(g, title, subtitle);

            if (rows.isEmpty()) {
                String message = ready ? emptyText : "Loading...";
                g.setFont(font(Font.PLAIN, 13));
                g.setColor(MUTED);
                FontMetrics fm = g.getFontMetrics();
                String shown = clip(message, fm, Math.max(0, w - 44));
                g.drawString(shown, Math.max(24, (w - fm.stringWidth(shown)) / 2), h / 2 + 12);
                g.dispose();
                return;
            }

            int n = rows.size();
            int left = 44;
            int right = w - 44;
            int top = 104;
            int bottom = h - 52;

            double minV = 0;
            double maxV = 0;
            for (CropRow row : rows) {
                minV = Math.min(minV, row.profit);
                maxV = Math.max(maxV, row.profit);
            }
            if (maxV - minV < 1e-9) {
                maxV = minV + 1;
            }

            double[] xs = new double[n];
            double[] ys = new double[n];
            int best = 0;

            for (int i = 0; i < n; i++) {
                xs[i] = n == 1 ? (left + right) / 2.0 : left + (right - left) * (double) i / (n - 1);
                ys[i] = bottom - (rows.get(i).profit - minV) / (maxV - minV) * (bottom - top);
                if (rows.get(i).profit > rows.get(best).profit) {
                    best = i;
                }
            }

            g.setStroke(new BasicStroke(1f));
            for (int k = 0; k <= 4; k++) {
                int y = top + (bottom - top) * k / 4;
                g.setColor(k == 4 ? new Color(214, 222, 216) : new Color(238, 242, 239));
                g.drawLine(left - 14, y, right + 14, y);
            }

            g.setFont(font(Font.PLAIN, 12));
            g.setColor(MUTED);
            FontMetrics fm = g.getFontMetrics();
            for (int i = 0; i < n; i++) {
                String label = clip(rows.get(i).name, fm, 96);
                int lx = (int) Math.round(xs[i] - fm.stringWidth(label) / 2.0);
                lx = Math.max(8, Math.min(w - 8 - fm.stringWidth(label), lx));
                g.drawString(label, lx, bottom + 24);
            }

            float eased = 1f - (float) Math.pow(1f - progress, 3);

            Graphics2D c = (Graphics2D) g.create();
            c.clipRect(0, 0, (int) (w * eased) + 1, h);

            if (n > 1) {
                Path2D line = curve(xs, ys);

                Path2D fill = new Path2D.Double(line);
                fill.lineTo(xs[n - 1], bottom);
                fill.lineTo(xs[0], bottom);
                fill.closePath();

                c.setPaint(new GradientPaint(0, top, alpha(GREEN, 120), 0, bottom, alpha(GREEN, 6)));
                c.fill(fill);

                c.setColor(GREEN);
                c.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                c.draw(line);
            }

            c.setColor(alpha(FOREST, 60));
            c.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{4f, 4f}, 0f));
            c.drawLine((int) xs[best], (int) ys[best], (int) xs[best], bottom);

            for (int i = 0; i < n; i++) {
                int r = i == best ? 7 : 5;
                c.setColor(Color.WHITE);
                c.fillOval((int) (xs[i] - r), (int) (ys[i] - r), r * 2, r * 2);
                c.setColor(rows.get(i).profit >= 0 ? GREEN : RED);
                c.setStroke(new BasicStroke(2.4f));
                c.drawOval((int) (xs[i] - r), (int) (ys[i] - r), r * 2, r * 2);
            }

            String pin = signedMoney(rows.get(best).profit);
            c.setFont(font(Font.BOLD, 12));
            FontMetrics pm = c.getFontMetrics();
            int bw = pm.stringWidth(pin) + 22;
            int bh = 26;
            int bx = (int) Math.round(xs[best] - bw / 2.0);
            bx = Math.max(8, Math.min(w - 8 - bw, bx));
            int by = (int) Math.round(ys[best]) - 8 - 6 - bh;

            c.setColor(FOREST);
            c.fillRoundRect(bx, by, bw, bh, 12, 12);

            Path2D tip = new Path2D.Double();
            tip.moveTo(xs[best] - 6, by + bh - 1);
            tip.lineTo(xs[best] + 6, by + bh - 1);
            tip.lineTo(xs[best], by + bh + 6);
            tip.closePath();
            c.fill(tip);

            c.setColor(Color.WHITE);
            centered(c, pin, bx + bw / 2.0, by + bh / 2.0);

            c.dispose();
            g.dispose();
        }
    }

    private static class MonthCalendar extends Surface {
        private YearMonth month = YearMonth.now();
        private LocalDate selected = LocalDate.now();
        private final Rectangle prev = new Rectangle();
        private final Rectangle next = new Rectangle();
        private final Rectangle[] cells = new Rectangle[42];
        private final LocalDate[] dates = new LocalDate[42];

        MonthCalendar() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (prev.contains(e.getPoint())) {
                        month = month.minusMonths(1);
                        repaint();
                        return;
                    }
                    if (next.contains(e.getPoint())) {
                        month = month.plusMonths(1);
                        repaint();
                        return;
                    }
                    for (int i = 0; i < cells.length; i++) {
                        if (cells[i] != null && cells[i].contains(e.getPoint())) {
                            selected = dates[i];
                            month = YearMonth.from(selected);
                            repaint();
                            return;
                        }
                    }
                }
            });
        }

        private void arrow(Graphics2D g, Rectangle r, boolean left) {
            g.setColor(new Color(240, 246, 242));
            g.fillOval(r.x, r.y, r.width, r.height);
            g.setColor(FOREST);
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = r.x + r.width / 2;
            int cy = r.y + r.height / 2;
            if (left) {
                g.drawLine(cx + 2, cy - 5, cx - 3, cy);
                g.drawLine(cx - 3, cy, cx + 2, cy + 5);
            } else {
                g.drawLine(cx - 2, cy - 5, cx + 3, cy);
                g.drawLine(cx + 3, cy, cx - 2, cy + 5);
            }
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth();
            int h = getHeight();

            g.setColor(GOLD);
            g.fillRoundRect(24, 24, 4, 24, 4, 4);

            g.setFont(font(Font.BOLD, 16));
            g.setColor(FOREST);
            String title = month.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + month.getYear();
            g.drawString(title, 38, 42);

            prev.setBounds(w - 88, 24, 28, 28);
            next.setBounds(w - 54, 24, 28, 28);
            arrow(g, prev, true);
            arrow(g, next, false);

            int gx = 20;
            double cellW = (w - 40) / 7.0;
            int weekY = 80;
            int gridTop = 96;
            double cellH = (h - gridTop - 18) / 6.0;

            String[] names = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
            g.setFont(font(Font.BOLD, 11));
            g.setColor(MUTED);
            for (int i = 0; i < 7; i++) {
                centered(g, names[i], gx + cellW * (i + 0.5), weekY);
            }

            LocalDate first = month.atDay(1);
            LocalDate start = first.minusDays(first.getDayOfWeek().getValue() - 1);
            LocalDate today = LocalDate.now();

            for (int i = 0; i < 42; i++) {
                LocalDate date = start.plusDays(i);
                int row = i / 7;
                int col = i % 7;
                double cx = gx + cellW * (col + 0.5);
                double cy = gridTop + cellH * (row + 0.5);

                dates[i] = date;
                cells[i] = new Rectangle(
                        (int) (cx - cellW / 2), (int) (cy - cellH / 2),
                        (int) cellW, (int) cellH
                );

                double r = Math.min(17, Math.min(cellW, cellH) / 2 - 2);
                boolean inMonth = YearMonth.from(date).equals(month);
                boolean isToday = date.equals(today);
                boolean isSelected = date.equals(selected);

                if (isToday) {
                    g.setColor(GREEN);
                    g.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
                    g.setColor(Color.WHITE);
                    g.setFont(font(Font.BOLD, 12));
                } else if (isSelected) {
                    g.setColor(alpha(GREEN, 36));
                    g.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
                    g.setColor(FOREST);
                    g.setFont(font(Font.BOLD, 12));
                } else {
                    g.setColor(inMonth ? FOREST : new Color(182, 190, 184));
                    g.setFont(font(Font.PLAIN, 12));
                }

                centered(g, String.valueOf(date.getDayOfMonth()), cx, cy);
            }

            g.dispose();
        }
    }

    private static class DirectoryButton extends JButton {
        private final String iconKey;
        private final String title;
        private final String description;
        private boolean hover;

        DirectoryButton(String iconKey, String title, String description) {
            this.iconKey = iconKey;
            this.title = title;
            this.description = description;

            setText(title);
            setToolTipText(title + " - " + description);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth();
            int h = getHeight();

            g.setColor(hover ? new Color(236, 246, 240) : new Color(248, 250, 249));
            g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);

            g.setColor(hover ? new Color(153, 196, 170) : new Color(232, 237, 233));
            g.drawRoundRect(0, 0, w - 1, h - 1, 16, 16);

            int cs = Math.min(34, h - 12);
            int cx = 10;
            int cy = (h - cs) / 2;

            g.setColor(hover ? GREEN : new Color(220, 239, 229));
            g.fillRoundRect(cx, cy, cs, cs, 12, 12);
            UITheme.Icons.paint(g, iconKey, cx + (cs - 18) / 2.0, cy + (cs - 18) / 2.0, 18,
                    hover ? Color.WHITE : FOREST_LIGHT);

            int textX = cx + cs + 14;
            int available = Math.max(0, w - textX - 30);

            g.setFont(font(Font.BOLD, 12));
            g.setColor(FOREST);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(clip(title, fm, available), textX, h / 2 - 2);

            g.setFont(font(Font.PLAIN, 11));
            g.setColor(MUTED);
            fm = g.getFontMetrics();
            g.drawString(clip(description, fm, available), textX, h / 2 + 13);

            int arrowX = w - 19;
            int arrowY = h / 2;

            g.setColor(hover ? FOREST : new Color(166, 176, 164));
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(arrowX - 4, arrowY - 4, arrowX, arrowY);
            g.drawLine(arrowX, arrowY, arrowX - 4, arrowY + 4);

            if (hasFocus()) {
                g.setColor(GOLD);
                g.drawRoundRect(3, 3, w - 7, h - 7, 12, 12);
            }

            g.dispose();
        }
    }
}