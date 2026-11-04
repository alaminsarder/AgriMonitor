package com.agrimonitor.gui;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.SalesDAO;
import com.agrimonitor.model.Sales;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SalesPanel extends JPanel {

    private static final Color P_TOP = new Color(88, 50, 112);
    private static final Color P_BOTTOM = new Color(28, 17, 44);
    private static final Color GOLD = new Color(240, 200, 112);
    private static final Color SOFT = new Color(226, 212, 234);
    private static final Color MUTED = new Color(190, 170, 202);
    private static final Color PLUM_D = new Color(110, 56, 140);
    private static final Color[] PLUMS = {
            new Color(123, 63, 152),
            new Color(46, 134, 171),
            new Color(196, 106, 46),
            new Color(36, 140, 96),
            new Color(181, 69, 110),
            new Color(176, 138, 40)
    };

    private final SalesDAO salesDAO = new SalesDAO();
    private final CropDAO cropDAO = new CropDAO();
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final DecimalFormat qf = new DecimalFormat("#,##0.##");

    private final JTextField cropIdField = UITheme.createTextField();
    private final JTextField cropNameField = UITheme.createTextField();
    private final JTextField quantitySoldField = UITheme.createTextField();
    private final JTextField priceField = UITheme.createTextField();
    private final JTextField dateField = UITheme.createTextField();
    private final JTextField buyerField = UITheme.createTextField();
    private final JTextField searchCropIdField = UITheme.createTextField();

    private final JLabel totalLabel = new JLabel(" ");

    private final DefaultTableModel tableModel = UIKit.model("Crop ID", "Crop Name", "Qty Sold", "Price/Unit", "Total", "Date", "Buyer");
    private final JTable table = new JTable(tableModel);

    private final RevenueCard card = new RevenueCard();
    private final TotalPreview preview = new TotalPreview();
    private double sumTotal;

    public SalesPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(22, 26, 22, 26));

        card.setPreferredSize(new Dimension(10, 166));

        JButton addButton = UITheme.createStyledButton("Add Sales Record", UITheme.SUCCESS);
        JButton searchButton = UITheme.createStyledButton("Get Total", UITheme.INFO);
        JButton refreshButton = UITheme.createStyledButton("View All", UITheme.TEXT_LIGHT);
        JButton clearButton = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        addButton.setPreferredSize(new Dimension(10, 40));
        refreshButton.setPreferredSize(new Dimension(10, 38));
        clearButton.setPreferredSize(new Dimension(10, 38));
        searchButton.setPreferredSize(new Dimension(104, 38));
        searchCropIdField.setPreferredSize(new Dimension(100, 38));

        totalLabel.setFont(new Font(UIKit.FAMILY, Font.BOLD, 13));
        totalLabel.setForeground(UITheme.SUCCESS);

        JPanel pair = new JPanel(new GridLayout(1, 2, 8, 0));
        pair.setOpaque(false);
        pair.add(refreshButton);
        pair.add(clearButton);

        JPanel footer = new JPanel(new GridLayout(2, 1, 0, 8));
        footer.setOpaque(false);
        footer.add(addButton);
        footer.add(pair);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(UIKit.formGrid(3, 2,
                UIKit.field("Crop ID", cropIdField),
                UIKit.field("Crop Name", cropNameField),
                UIKit.field("Quantity Sold", quantitySoldField),
                UIKit.field("Price Per Unit", priceField),
                UIKit.field("Date (YYYY-MM-DD)", dateField),
                UIKit.field("Buyer", buyerField)), BorderLayout.CENTER);
        body.add(preview, BorderLayout.SOUTH);

        JPanel form = UIKit.formCard("Record a Sale", body, footer);

        JLabel lk = new JLabel("SALES BY CROP ID");
        lk.setFont(new Font(UIKit.FAMILY, Font.BOLD, 11));
        lk.setForeground(UITheme.TEXT_LIGHT);

        JPanel lookupRow = new JPanel(new BorderLayout(10, 0));
        lookupRow.setOpaque(false);
        lookupRow.add(searchCropIdField, BorderLayout.CENTER);
        lookupRow.add(searchButton, BorderLayout.EAST);

        SoftCard lookup = new SoftCard(new BorderLayout(0, 10));
        lookup.add(lk, BorderLayout.NORTH);
        lookup.add(lookupRow, BorderLayout.CENTER);
        lookup.add(totalLabel, BorderLayout.SOUTH);

        JPanel lower = new JPanel(new BorderLayout());
        lower.setOpaque(false);
        lower.setBorder(new EmptyBorder(12, 0, 0, 0));
        lower.add(lookup, BorderLayout.NORTH);

        ScrollColumn column = new ScrollColumn();
        column.setLayout(new BorderLayout());
        column.setOpaque(false);
        column.add(form, BorderLayout.NORTH);
        column.add(lower, BorderLayout.CENTER);

        JScrollPane sideScroll = new JScrollPane(column,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setBorder(null);
        sideScroll.setOpaque(false);
        sideScroll.getViewport().setOpaque(false);
        sideScroll.getVerticalScrollBar().setUnitIncrement(16);
        sideScroll.setPreferredSize(new Dimension(350, 10));

        UIKit.styleTable(table);
        table.setRowHeight(40);
        table.getColumnModel().getColumn(0).setCellRenderer(new IdCell());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameCell());
        table.getColumnModel().getColumn(2).setCellRenderer(new QtyCell());
        table.getColumnModel().getColumn(3).setCellRenderer(new PriceCell());
        table.getColumnModel().getColumn(4).setCellRenderer(new TotalCell());
        table.getColumnModel().getColumn(5).setCellRenderer(new DateCell());
        table.getColumnModel().getColumn(6).setCellRenderer(new BuyerCell());
        int[] widths = {80, 110, 70, 84, 130, 104, 120};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JComponent tableCard = UIKit.tableCard("All Sales Records", table, "Search sales...");

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(tableCard, BorderLayout.CENTER);
        center.add(sideScroll, BorderLayout.EAST);

        add(card, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshPreview();
            }
        };
        quantitySoldField.getDocument().addDocumentListener(dl);
        priceField.getDocument().addDocumentListener(dl);

        addButton.addActionListener(e -> addSale());
        refreshButton.addActionListener(e -> loadAllSales());
        searchButton.addActionListener(e -> getTotalSales());
        clearButton.addActionListener(e -> clearForm());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                loadAllSales();
            }
        });

        loadAllSales();
    }

    private void refreshPreview() {
        try {
            double q = Double.parseDouble(quantitySoldField.getText().trim());
            double p = Double.parseDouble(priceField.getText().trim());
            preview.setValue(df.format(q * p) + " BDT");
        } catch (NumberFormatException ex) {
            preview.setValue("-");
        }
    }

    private void addSale() {
        try {
            String cropId = cropIdField.getText().trim();
            String cropName = cropNameField.getText().trim();
            double quantitySold = Double.parseDouble(quantitySoldField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());
            String date = dateField.getText().trim();
            String buyer = buyerField.getText().trim();

            if (cropId.isEmpty() || cropName.isEmpty()) {
                UIKit.warn(this, "Crop ID and Crop Name are required.");
                return;
            }

            if (!cropDAO.cropExists(cropId)) {
                UIKit.warn(this, "Crop ID does not exist.");
                return;
            }

            Sales sale = new Sales(cropId, cropName, quantitySold, price, date, buyer);
            boolean success = salesDAO.addSale(sale);

            if (success) {
                UIKit.info(this, "Sales record added successfully. Total Sales: " + sale.getTotalSales() + " BDT");
                clearForm();
                loadAllSales();
            } else {
                UIKit.warn(this, "Failed to add sales record.");
            }
        } catch (NumberFormatException ex) {
            UIKit.warn(this, "Please enter valid numbers for Quantity and Price.");
        }
    }

    private void loadAllSales() {
        tableModel.setRowCount(0);
        List<Sales> list = salesDAO.getAllSales();
        double sum = 0;
        for (Sales sale : list) {
            tableModel.addRow(new Object[]{
                    sale.getCropId(), sale.getCropName(), sale.getQuantitySold(),
                    sale.getSellingPricePerUnit(), sale.getTotalSales(),
                    sale.getSellingDate(), sale.getBuyerInfo()
            });
            sum += sale.getTotalSales();
        }
        sumTotal = sum;
        card.setData(list);
        table.repaint();
    }

    private void getTotalSales() {
        String cropId = searchCropIdField.getText().trim();
        if (cropId.isEmpty()) {
            UIKit.warn(this, "Please enter a Crop ID.");
            return;
        }
        double total = salesDAO.getTotalSalesByCropId(cropId);
        totalLabel.setText("Total Sales for Crop " + cropId + ": " + df.format(total) + " BDT");
    }

    private void clearForm() {
        cropIdField.setText("");
        cropNameField.setText("");
        quantitySoldField.setText("");
        priceField.setText("");
        dateField.setText("");
        buyerField.setText("");
        searchCropIdField.setText("");
        totalLabel.setText(" ");
        table.clearSelection();
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

    private static String compact(double v) {
        if (v >= 1_000_000) return String.format("%.1fM", v / 1_000_000);
        if (v >= 1_000) return String.format("%.1fK", v / 1_000);
        return String.format("%.0f", v);
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

    private static Color plumFor(Object key) {
        String s = String.valueOf(key);
        return PLUMS[Math.abs(s.hashCode()) % PLUMS.length];
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private static class ScrollColumn extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
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

    private static class SoftCard extends JPanel {
        SoftCard(LayoutManager lm) {
            super(lm);
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, w, h, 18, 18);
            g.setColor(UITheme.BORDER_COLOR);
            g.drawRoundRect(0, 0, w, h, 18, 18);
            g.dispose();
        }
    }

    private static class TotalPreview extends JPanel {
        private String value = "-";

        TotalPreview() {
            setOpaque(false);
            setPreferredSize(new Dimension(10, 62));
        }

        void setValue(String v) {
            value = v;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g.setColor(new Color(246, 239, 250));
            g.fillRoundRect(0, 0, w, h, 16, 16);
            g.setColor(new Color(224, 207, 234));
            g.drawRoundRect(0, 0, w, h, 16, 16);
            g.setColor(PLUM_D);
            g.fillRoundRect(0, 10, 4, h - 20, 4, 4);

            g.setFont(f(Font.BOLD, 11));
            g.setColor(new Color(130, 100, 148));
            g.drawString("ESTIMATED TOTAL", 18, 22);

            int size = 21;
            Font vf = f(Font.BOLD, size);
            while (size > 12 && g.getFontMetrics(vf).stringWidth(value) > w - 36) {
                size--;
                vf = f(Font.BOLD, size);
            }
            g.setFont(vf);
            g.setColor(PLUM_D);
            g.drawString(value, 18, 48);
            g.dispose();
        }
    }

    private static class Pt {
        final String date;
        final String crop;
        final double sale;
        final double cum;

        Pt(String date, String crop, double sale, double cum) {
            this.date = date;
            this.crop = crop;
            this.sale = sale;
            this.cum = cum;
        }
    }

    private static class RevenueCard extends JPanel {
        private static final int MAXP = 24;

        private final List<Pt> pts = new ArrayList<>();
        private double total;
        private double avg;
        private int count;
        private String topCrop = "-";
        private double progress = 1;
        private long startMs;
        private final Timer timer;
        private int hover = -1;
        private double[] px = new double[0];
        private double[] py = new double[0];
        private int startIdx;
        private double plotTop;
        private double plotBottom;

        RevenueCard() {
            setOpaque(false);
            timer = new Timer(16, e -> {
                progress = Math.min(1, (System.currentTimeMillis() - startMs) / 1300.0);
                repaint();
                if (progress >= 1) ((Timer) e.getSource()).stop();
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    int best = -1;
                    double bd = 18;
                    if (e.getY() >= plotTop - 14 && e.getY() <= plotBottom + 14) {
                        for (int i = 0; i < px.length; i++) {
                            double d = Math.abs(e.getX() - px[i]);
                            if (d < bd) {
                                bd = d;
                                best = i;
                            }
                        }
                    }
                    if (best != hover) {
                        hover = best;
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

        void setData(List<Sales> list) {
            pts.clear();
            count = list.size();
            List<Sales> sorted = new ArrayList<>(list);
            sorted.sort(Comparator.comparing((Sales s) -> String.valueOf(s.getSellingDate())));
            Map<String, Double> byCrop = new LinkedHashMap<>();
            double cum = 0;
            for (Sales s : sorted) {
                cum += s.getTotalSales();
                String crop = String.valueOf(s.getCropName());
                pts.add(new Pt(String.valueOf(s.getSellingDate()), crop, s.getTotalSales(), cum));
                byCrop.merge(crop, s.getTotalSales(), Double::sum);
            }
            total = cum;
            avg = count == 0 ? 0 : total / count;
            topCrop = "-";
            double best = -1;
            for (Map.Entry<String, Double> en : byCrop.entrySet()) {
                if (en.getValue() > best) {
                    best = en.getValue();
                    topCrop = en.getKey();
                }
            }
            hover = -1;
            progress = 0;
            startMs = System.currentTimeMillis();
            timer.restart();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            double ease = 1 - Math.pow(1 - progress, 3);

            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 24, 24);
            g.setPaint(new GradientPaint(0, 0, P_TOP, w, h, P_BOTTOM));
            g.fill(shape);
            g.setClip(shape);

            g.setColor(new Color(255, 255, 255, 10));
            g.fillOval(-70, h - 100, 240, 240);
            g.setColor(new Color(255, 255, 255, 8));
            g.fillOval(w - 160, -110, 280, 280);
            g.setColor(UITheme.ACCENT);
            g.fillRect(0, h - 4, w, 4);

            boolean chart = w >= 640;

            g.setFont(f(Font.BOLD, 12));
            g.setColor(GOLD);
            g.drawString("TOTAL REVENUE", 30, 36);

            String num = String.format("%,.0f", total * ease);
            int avail = chart ? 270 : w - 120;
            int size = 40;
            Font bf = f(Font.BOLD, size);
            while (size > 24 && g.getFontMetrics(bf).stringWidth(num) > avail) {
                size -= 2;
                bf = f(Font.BOLD, size);
            }
            g.setFont(bf);
            g.setColor(Color.WHITE);
            g.drawString(num, 30, 84);
            int nw = g.getFontMetrics().stringWidth(num);
            g.setFont(f(Font.BOLD, 13));
            g.setColor(MUTED);
            g.drawString("BDT", 30 + nw + 8, 84);

            g.setStroke(new BasicStroke(1f));
            g.setColor(new Color(255, 255, 255, 36));
            g.drawLine(30, 102, chart ? 340 : w - 30, 102);

            String[] labels = {"SALES", "AVERAGE", "TOP CROP"};
            String[] values = {String.valueOf(count), String.format("%,.0f", avg), topCrop};
            int[] xs = {30, 110, 230};
            for (int i = 0; i < 3; i++) {
                g.setFont(f(Font.BOLD, 11));
                g.setColor(GOLD);
                g.drawString(labels[i], xs[i], 124);
                g.setFont(f(Font.BOLD, 15));
                g.setColor(Color.WHITE);
                g.drawString(fit(values[i], g.getFontMetrics(), i == 2 ? 100 : 78), xs[i], 145);
            }

            px = new double[0];
            py = new double[0];
            if (chart) paintChart(g, w, h, ease);
            g.dispose();
        }

        private void paintChart(Graphics2D g, int w, int h, double ease) {
            double left = 392;
            double right = w - 30;
            plotTop = 48;
            plotBottom = h - 38;
            double cw = right - left;
            if (cw < 120) return;

            g.setFont(f(Font.BOLD, 11));
            g.setColor(GOLD);
            g.drawString("REVENUE TREND", (float) left, 30);

            int n = pts.size();
            if (n == 0) {
                g.setFont(f(Font.PLAIN, 13));
                g.setColor(MUTED);
                centered(g, "No sales recorded yet", (left + right) / 2, (plotTop + plotBottom) / 2 + 4);
                return;
            }

            startIdx = Math.max(0, n - MAXP);
            int m = n - startIdx;
            double maxV = Math.max(1, pts.get(n - 1).cum) * 1.08;

            g.setStroke(new BasicStroke(1f));
            for (int i = 0; i < 3; i++) {
                double y = plotTop + (plotBottom - plotTop) * i / 2.0;
                g.setColor(new Color(255, 255, 255, 28));
                g.draw(new Line2D.Double(left, y, right, y));
            }
            g.setFont(f(Font.PLAIN, 10));
            g.setColor(MUTED);
            g.drawString(compact(maxV), (float) left, (float) (plotTop - 4));
            g.drawString(compact(maxV / 2), (float) left, (float) ((plotTop + plotBottom) / 2 - 4));

            double[] xs = new double[m];
            double[] ys = new double[m];
            for (int i = 0; i < m; i++) {
                Pt p = pts.get(startIdx + i);
                xs[i] = m == 1 ? (left + right) / 2 : left + 8 + i * (cw - 16) / (m - 1);
                ys[i] = plotBottom - (p.cum / maxV) * (plotBottom - plotTop);
            }
            px = xs;
            py = ys;

            Shape prev = g.getClip();
            g.clip(new Rectangle2D.Double(left - 6, 0, (cw + 12) * ease, h));

            if (m > 1) {
                Path2D.Double area = new Path2D.Double();
                area.moveTo(xs[0], plotBottom);
                for (int i = 0; i < m; i++) area.lineTo(xs[i], ys[i]);
                area.lineTo(xs[m - 1], plotBottom);
                area.closePath();
                g.setPaint(new GradientPaint(0, (float) plotTop, new Color(240, 200, 112, 120),
                        0, (float) plotBottom, new Color(240, 200, 112, 0)));
                g.fill(area);

                Path2D.Double line = new Path2D.Double();
                line.moveTo(xs[0], ys[0]);
                for (int i = 1; i < m; i++) line.lineTo(xs[i], ys[i]);
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(GOLD);
                g.draw(line);
            }

            for (int i = 0; i < m; i++) {
                boolean hv = i == hover;
                double r = hv ? 5.5 : 3.4;
                g.setColor(P_BOTTOM);
                g.fill(new Ellipse2D.Double(xs[i] - r, ys[i] - r, r * 2, r * 2));
                g.setStroke(new BasicStroke(hv ? 2.2f : 1.8f));
                g.setColor(hv ? Color.WHITE : GOLD);
                g.draw(new Ellipse2D.Double(xs[i] - r, ys[i] - r, r * 2, r * 2));
            }
            g.setClip(prev);

            g.setFont(f(Font.PLAIN, 10));
            g.setColor(MUTED);
            FontMetrics dm = g.getFontMetrics();
            String first = pts.get(startIdx).date;
            String last = pts.get(n - 1).date;
            g.drawString(first, (float) left, (float) (h - 16));
            if (m > 1) g.drawString(last, (float) (right - dm.stringWidth(last)), (float) (h - 16));

            if (hover >= 0 && hover < m && progress >= 1) {
                Pt p = pts.get(startIdx + hover);
                g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1f, new float[]{3f, 3f}, 0f));
                g.setColor(new Color(255, 255, 255, 80));
                g.draw(new Line2D.Double(xs[hover], plotTop, xs[hover], plotBottom));

                String l1 = p.crop;
                String l2 = p.date;
                String l3 = "Sale  " + String.format("%,.2f", p.sale);
                String l4 = "Total  " + String.format("%,.2f", p.cum);
                g.setFont(f(Font.BOLD, 12));
                int bw = g.getFontMetrics().stringWidth(l1);
                g.setFont(f(Font.PLAIN, 11));
                FontMetrics pm = g.getFontMetrics();
                bw = Math.max(bw, Math.max(pm.stringWidth(l2), Math.max(pm.stringWidth(l3), pm.stringWidth(l4))));
                int boxW = bw + 26;
                int boxH = 80;
                double bx = xs[hover] + 14;
                if (bx + boxW > w - 10) bx = xs[hover] - 14 - boxW;
                double by = Math.max(8, Math.min(h - 12 - boxH, ys[hover] - boxH / 2.0));

                RoundRectangle2D tip = new RoundRectangle2D.Double(bx, by, boxW, boxH, 12, 12);
                g.setColor(new Color(255, 250, 240, 246));
                g.fill(tip);
                g.setStroke(new BasicStroke(1f));
                g.setColor(GOLD);
                g.draw(tip);

                g.setFont(f(Font.BOLD, 12));
                g.setColor(UITheme.TEXT_DARK);
                g.drawString(l1, (float) (bx + 13), (float) (by + 20));
                g.setFont(f(Font.PLAIN, 11));
                g.setColor(new Color(120, 110, 126));
                g.drawString(l2, (float) (bx + 13), (float) (by + 36));
                g.setColor(PLUM_D);
                g.drawString(l3, (float) (bx + 13), (float) (by + 54));
                g.setColor(UITheme.TEXT_DARK);
                g.drawString(l4, (float) (bx + 13), (float) (by + 70));
            }
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
            g.setColor(selected ? new Color(240, 230, 246) : Color.WHITE);
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
            Color c = plumFor(s);
            g.setFont(f(Font.BOLD, 12));
            int cw = g.getFontMetrics().stringWidth(s) + 22;
            int ch = 24;
            int y = (h - ch) / 2;
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 34));
            g.fill(new RoundRectangle2D.Double(10, y, cw, ch, ch, ch));
            g.setColor(c);
            g.drawString(s, 21, y + 16);
        }
    }

    private class NameCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            Object id = tableModel.getValueAt(mrow, 0);
            g.setColor(plumFor(id));
            g.fill(new RoundRectangle2D.Double(10, h / 2.0 - 6, 12, 12, 5, 5));
            g.setFont(f(Font.BOLD, 13));
            g.setColor(UITheme.TEXT_DARK);
            g.drawString(fit(String.valueOf(value), g.getFontMetrics(), w - 38), 30, h / 2 + 5);
        }
    }

    private class QtyCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            String s = qf.format(num(value));
            g.setFont(f(Font.PLAIN, 13));
            g.setColor(UITheme.TEXT_DARK);
            g.drawString(s, w - 12 - g.getFontMetrics().stringWidth(s), h / 2 + 5);
        }
    }

    private class PriceCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            String s = df.format(num(value));
            g.setFont(f(Font.PLAIN, 13));
            g.setColor(new Color(120, 110, 126));
            g.drawString(s, w - 12 - g.getFontMetrics().stringWidth(s), h / 2 + 5);
        }
    }

    private class TotalCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            double v = num(value);
            String txt = df.format(v);
            g.setFont(f(Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            int pw = fm.stringWidth(txt) + 24;
            int ph = 24;
            int px = w - pw - 10;
            int py = (h - ph) / 2;
            g.setColor(new Color(PLUM_D.getRed(), PLUM_D.getGreen(), PLUM_D.getBlue(), 28));
            g.fill(new RoundRectangle2D.Double(px, py, pw, ph, ph, ph));
            g.setColor(PLUM_D);
            g.drawString(txt, px + 12, py + 16);

            if (sumTotal > 0) {
                String pct = String.format("%.0f%%", v / sumTotal * 100);
                g.setFont(f(Font.PLAIN, 11));
                int tw = g.getFontMetrics().stringWidth(pct);
                if (px - 8 - tw > 4) {
                    g.setColor(new Color(140, 132, 146));
                    g.drawString(pct, px - 8 - tw, py + 16);
                }
            }
        }
    }

    private class DateCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            double ix = 10;
            double iy = h / 2.0 - 7;
            g.setStroke(new BasicStroke(1.3f));
            g.setColor(new Color(150, 140, 156));
            g.draw(new RoundRectangle2D.Double(ix, iy + 2, 13, 12, 3, 3));
            g.draw(new Line2D.Double(ix, iy + 6, ix + 13, iy + 6));
            g.draw(new Line2D.Double(ix + 4, iy, ix + 4, iy + 3));
            g.draw(new Line2D.Double(ix + 9, iy, ix + 9, iy + 3));
            g.setFont(f(Font.PLAIN, 12));
            g.setColor(new Color(100, 92, 108));
            g.drawString(fit(String.valueOf(value), g.getFontMetrics(), w - 36), 30, h / 2 + 4);
        }
    }

    private class BuyerCell extends Cell {
        @Override
        void draw(Graphics2D g, int w, int h) {
            String s = value == null ? "" : String.valueOf(value).trim();
            if (s.isEmpty() || s.equals("null")) {
                g.setFont(f(Font.PLAIN, 13));
                g.setColor(new Color(170, 164, 176));
                g.drawString("-", 12, h / 2 + 5);
                return;
            }
            Color c = plumFor(s);
            double d = 24;
            g.setColor(c);
            g.fill(new Ellipse2D.Double(8, (h - d) / 2, d, d));
            g.setFont(f(Font.BOLD, 12));
            g.setColor(Color.WHITE);
            centered(g, s.substring(0, 1).toUpperCase(), 8 + d / 2, h / 2 + 4);
            g.setFont(f(Font.PLAIN, 13));
            g.setColor(UITheme.TEXT_DARK);
            g.drawString(fit(s, g.getFontMetrics(), w - 46), 40, h / 2 + 5);
        }
    }
}