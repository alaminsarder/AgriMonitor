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
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HomePanel extends JPanel {

    private static final Color FOREST = new Color(31, 61, 51);
    private static final Color FOREST_LIGHT = new Color(55, 101, 75);
    private static final Color GOLD = new Color(190, 151, 73);
    private static final Color PALE_GOLD = new Color(230, 210, 158);
    private static final Color PAPER = new Color(250, 248, 242);
    private static final Color LINE = new Color(226, 224, 212);
    private static final Color MUTED = new Color(111, 120, 108);

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

    private final JLabel fieldsValue = new JLabel("-");
    private final JLabel fieldsSub = new JLabel("Registered plots");

    private final JLabel cropsValue = new JLabel("-");
    private final JLabel cropsSub = new JLabel("Under tracking");

    private final JLabel salesValue = new JLabel("-");
    private final JLabel salesSub = new JLabel("Total revenue");

    private final JLabel netTitle = new JLabel("Net profit");
    private final JLabel netValue = new JLabel("-");
    private final JLabel netSub = new JLabel(" ");

    private final BarsCard moneyCard = new BarsCard(
            "Money flow",
            "Revenue, expenses and net result",
            "No sales or cost recorded yet"
    );

    private final BarsCard cropCard = new BarsCard(
            "Crop performance",
            "Top crops ranked by profit",
            "No crop data yet"
    );

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
        fixHeight(hero, 166);
        body.add(hero);
        body.add(Box.createVerticalStrut(18));

        JComponent summary = buildSummary();
        fixHeight(summary, 134);
        body.add(summary);
        body.add(Box.createVerticalStrut(20));

        JComponent section = sectionHeading(
                "Farm overview",
                "A closer look at your farm's performance"
        );
        fixHeight(section, 48);
        body.add(section);
        body.add(Box.createVerticalStrut(8));

        JPanel charts = new JPanel(new GridLayout(1, 2, 16, 0));
        charts.setOpaque(false);
        charts.add(moneyCard);
        charts.add(cropCard);
        fixHeight(charts, 276);
        body.add(charts);
        body.add(Box.createVerticalStrut(20));

        JComponent directory = buildDirectory();
        fixHeight(directory, 364);
        body.add(directory);

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

    private JComponent buildHero() {
        HeroPanel hero = new HeroPanel();
        hero.setLayout(new BorderLayout(20, 0));
        hero.setBorder(new EmptyBorder(24, 32, 24, 32));

        greeting.setFont(font(Font.BOLD, 11));
        greeting.setForeground(PALE_GOLD);

        username.setFont(font(Font.BOLD, 30));
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
        welcome.add(Box.createVerticalStrut(6));
        welcome.add(username);
        welcome.add(Box.createVerticalStrut(6));
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
        timeBox.add(Box.createVerticalStrut(6));
        timeBox.add(clock);
        timeBox.add(Box.createVerticalStrut(5));
        timeBox.add(dateLabel);
        timeBox.add(Box.createVerticalGlue());

        hero.add(welcome, BorderLayout.CENTER);
        hero.add(timeBox, BorderLayout.EAST);

        return hero;
    }

    private JComponent buildSummary() {
        Surface surface = new Surface(PAPER);
        surface.setLayout(new BorderLayout());
        surface.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel heading = new JLabel("FARM AT A GLANCE");
        heading.setFont(font(Font.BOLD, 10));
        heading.setForeground(GOLD);

        JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
        row.setOpaque(false);
        row.add(metric("Fields", fieldsValue, fieldsSub, false));
        row.add(metric("Crops", cropsValue, cropsSub, true));
        row.add(metric("Total sales", salesValue, salesSub, true));
        row.add(metric(netTitle, netValue, netSub, true));

        surface.add(heading, BorderLayout.NORTH);
        surface.add(row, BorderLayout.CENTER);

        return surface;
    }

    private JComponent metric(String title, JLabel value, JLabel sub, boolean divider) {
        return metric(new JLabel(title), value, sub, divider);
    }

    private JComponent metric(JLabel title, JLabel value, JLabel sub, boolean divider) {
        MetricPanel panel = new MetricPanel(divider);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(10, divider ? 20 : 0, 0, 8));

        title.setFont(font(Font.PLAIN, 12));
        title.setForeground(MUTED);

        value.setFont(font(Font.BOLD, 23));
        value.setForeground(FOREST);

        sub.setFont(font(Font.PLAIN, 11));
        sub.setForeground(MUTED);

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(value);
        panel.add(Box.createVerticalStrut(3));
        panel.add(sub);

        return panel;
    }

    private JComponent sectionHeading(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel heading = new JLabel(title);
        heading.setFont(font(Font.BOLD, 20));
        heading.setForeground(FOREST);

        JLabel description = new JLabel(subtitle);
        description.setFont(font(Font.PLAIN, 12));
        description.setForeground(MUTED);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(heading);
        text.add(Box.createVerticalStrut(2));
        text.add(description);

        panel.add(text, BorderLayout.WEST);
        return panel;
    }

    private JComponent buildDirectory() {
        Surface card = new Surface(PAPER);
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(new EmptyBorder(20, 24, 22, 24));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);

        JLabel title = new JLabel("Farm directory");
        title.setFont(font(Font.BOLD, 20));
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

        JPanel columns = new JPanel(new GridLayout(1, 2, 24, 0));
        columns.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(5, 1, 0, 5));
        left.setOpaque(false);

        JPanel right = new JPanel(new GridLayout(5, 1, 0, 5));
        right.setOpaque(false);

        for (int i = 0; i < MODULES.length; i++) {
            DirectoryButton button = new DirectoryButton(
                    i + 1,
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
        fieldsValue.setText(snapshot.fields < 0 ? "-" : String.valueOf(snapshot.fields));
        cropsValue.setText(snapshot.crops < 0 ? "-" : String.valueOf(snapshot.crops));

        fieldsSub.setText("Registered plots");
        cropsSub.setText("Under tracking");

        List<Bar> moneyBars = new ArrayList<>();

        if (snapshot.hasMoney) {
            double net = snapshot.sales - snapshot.cost;

            salesValue.setText(money(snapshot.sales));
            salesSub.setText("Total revenue");

            netTitle.setText(net >= 0 ? "Net profit" : "Net loss");
            netValue.setText(money(Math.abs(net)));
            netValue.setForeground(
                    net >= 0 ? new Color(40, 125, 80) : new Color(178, 63, 58)
            );

            if (Double.isNaN(snapshot.margin) || Double.isInfinite(snapshot.margin)) {
                netSub.setText("Margin n/a");
            } else {
                netSub.setText(String.format(Locale.US, "Margin %.1f%%", snapshot.margin));
            }

            if (snapshot.sales != 0 || snapshot.cost != 0) {
                moneyBars.add(new Bar(
                        "Total sales",
                        money(snapshot.sales),
                        Math.abs(snapshot.sales),
                        new Color(52, 130, 98)
                ));

                moneyBars.add(new Bar(
                        "Total cost",
                        money(snapshot.cost),
                        Math.abs(snapshot.cost),
                        GOLD
                ));

                moneyBars.add(new Bar(
                        net >= 0 ? "Net profit" : "Net loss",
                        (net < 0 ? "-" : "") + money(Math.abs(net)),
                        Math.abs(net),
                        net >= 0 ? new Color(72, 145, 96) : new Color(185, 78, 69)
                ));
            }
        } else {
            salesValue.setText("-");
            salesSub.setText(" ");
            netTitle.setText("Net profit");
            netValue.setText("-");
            netValue.setForeground(FOREST);
            netSub.setText(" ");
        }

        moneyCard.setRows(moneyBars);

        List<Bar> cropBars = new ArrayList<>();
        int count = Math.min(4, snapshot.rows.size());

        for (int i = 0; i < count; i++) {
            CropRow row = snapshot.rows.get(i);

            cropBars.add(new Bar(
                    row.name,
                    (row.profit < 0 ? "-" : "") + money(Math.abs(row.profit)),
                    Math.abs(row.profit),
                    row.profit >= 0
                            ? new Color(72, 145, 96)
                            : new Color(185, 78, 69)
            ));
        }

        cropCard.setRows(cropBars);
    }

    private static String money(double value) {
        if (Math.abs(value) >= 1_000_000) {
            return String.format(Locale.US, "Tk %.2fM", value / 1_000_000d);
        }
        return "Tk " + String.format(Locale.US, "%,.0f", value);
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
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
        g.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
        g.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE
        );
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
        public int getScrollableUnitIncrement(
                Rectangle visibleRect,
                int orientation,
                int direction
        ) {
            return 18;
        }

        @Override
        public int getScrollableBlockIncrement(
                Rectangle visibleRect,
                int orientation,
                int direction
        ) {
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
        private final Color fill;

        Surface(Color fill) {
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);

            int w = getWidth() - 1;
            int h = getHeight() - 3;

            g.setColor(new Color(31, 61, 51, 12));
            g.fillRoundRect(1, 3, w - 1, h, 16, 16);

            g.setColor(fill);
            g.fillRoundRect(0, 0, w, h, 16, 16);

            g.setColor(LINE);
            g.drawRoundRect(0, 0, w, h, 16, 16);

            g.dispose();
        }
    }

    private static class MetricPanel extends JPanel {
        private final boolean divider;

        MetricPanel(boolean divider) {
            this.divider = divider;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);

            if (divider) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setColor(LINE);
                g.drawLine(0, 12, 0, getHeight() - 4);
                g.dispose();
            }
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

            RoundRectangle2D shape =
                    new RoundRectangle2D.Double(0, 0, w, h, 18, 18);

            g.setPaint(new GradientPaint(
                    0, 0, new Color(25, 55, 44),
                    w, h, new Color(57, 102, 72)
            ));
            g.fill(shape);

            g.clip(shape);

            g.setColor(new Color(230, 210, 158, 45));
            g.setStroke(new BasicStroke(1f));
            g.drawRoundRect(8, 8, w - 17, h - 17, 13, 13);

            int cx = w / 2 + 35;
            int cy = h / 2 + 28;

            g.setColor(new Color(221, 205, 150, 35));
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
            g.fillRect(30, h - 4, Math.max(0, w - 60), 2);

            g.dispose();
        }
    }

    private static class BarsCard extends Surface {
        private final String title;
        private final String subtitle;
        private final String emptyText;
        private final Timer animation;

        private List<Bar> rows = new ArrayList<>();
        private boolean ready;
        private float progress = 1f;

        BarsCard(String title, String subtitle, String emptyText) {
            super(PAPER);
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

        void setRows(List<Bar> rows) {
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
            int left = 23;
            int right = w - 23;

            g.setColor(GOLD);
            g.fillRoundRect(left, 23, 4, 29, 4, 4);

            g.setFont(font(Font.BOLD, 16));
            g.setColor(FOREST);
            g.drawString(title, left + 14, 33);

            g.setFont(font(Font.PLAIN, 12));
            g.setColor(MUTED);
            g.drawString(subtitle, left + 14, 51);

            g.setColor(LINE);
            g.drawLine(left, 65, right, 65);

            if (rows.isEmpty()) {
                String message = ready ? emptyText : "Loading...";
                g.setFont(font(Font.PLAIN, 13));
                g.setColor(MUTED);

                FontMetrics fm = g.getFontMetrics();
                String shown = clip(message, fm, Math.max(0, w - 44));
                g.drawString(
                        shown,
                        Math.max(left, (w - fm.stringWidth(shown)) / 2),
                        65 + (h - 65) / 2
                );

                g.dispose();
                return;
            }

            double max = 0;
            for (Bar bar : rows) {
                max = Math.max(max, bar.magnitude);
            }

            float eased = 1f - (float) Math.pow(1f - progress, 3);
            int y = 78;

            for (Bar bar : rows) {
                g.setFont(font(Font.BOLD, 12));
                FontMetrics fm = g.getFontMetrics();

                int valueWidth = fm.stringWidth(bar.value);
                int available = Math.max(0, right - left - valueWidth - 16);

                g.setColor(FOREST);
                g.drawString(clip(bar.label, fm, available), left, y + 13);

                g.setColor(bar.color.darker());
                g.drawString(bar.value, right - valueWidth, y + 13);

                int barY = y + 23;
                int barWidth = Math.max(0, right - left);

                g.setColor(new Color(232, 234, 223));
                g.fillRoundRect(left, barY, barWidth, 8, 8, 8);

                if (max > 0 && bar.magnitude > 0 && barWidth > 0) {
                    int filled = (int) Math.round(
                            barWidth * (bar.magnitude / max) * eased
                    );
                    filled = Math.max(1, Math.min(barWidth, filled));

                    g.setPaint(new GradientPaint(
                            left, 0,
                            new Color(
                                    bar.color.getRed(),
                                    bar.color.getGreen(),
                                    bar.color.getBlue(),
                                    150
                            ),
                            left + filled, 0,
                            bar.color
                    ));
                    g.fillRoundRect(left, barY, filled, 8, 8, 8);
                }

                y += 46;
            }

            g.dispose();
        }
    }

    private static class DirectoryButton extends JButton {
        private final String number;
        private final String title;
        private final String description;
        private boolean hover;

        DirectoryButton(int index, String title, String description) {
            this.number = String.format(Locale.US, "%02d", index);
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

            g.setColor(hover ? new Color(233, 241, 230) : Color.WHITE);
            g.fillRoundRect(0, 0, w - 1, h - 1, 10, 10);

            g.setColor(hover ? new Color(153, 183, 156) : LINE);
            g.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

            g.setFont(font(Font.BOLD, 11));
            g.setColor(GOLD);
            g.drawString(number, 13, h / 2 + 4);

            int textX = 42;
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
            g.setStroke(new BasicStroke(
                    1.6f,
                    BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND
            ));
            g.drawLine(arrowX - 4, arrowY - 4, arrowX, arrowY);
            g.drawLine(arrowX, arrowY, arrowX - 4, arrowY + 4);

            if (hasFocus()) {
                g.setColor(GOLD);
                g.drawRoundRect(3, 3, w - 7, h - 7, 8, 8);
            }

            g.dispose();
        }
    }
}