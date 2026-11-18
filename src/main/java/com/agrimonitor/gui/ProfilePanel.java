package com.agrimonitor.gui;

import com.agrimonitor.db.MongoDBConnection;
import com.agrimonitor.session.Session;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfilePanel extends JPanel {

    private static final String ROLE_TEXT = "AgriMonitor Account";

    private final JLabel nameLabel = new JLabel(" ");
    private final JLabel roleLabel = new JLabel(ROLE_TEXT);
    private final JLabel userValue = new JLabel(" ");
    private final JLabel emailValue = new JLabel(" ");
    private final Avatar avatar = new Avatar();
    private final String openedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

    public ProfilePanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        JPanel top = new JPanel(new BorderLayout(0, 16));
        top.setOpaque(false);
        top.add(buildHero(), BorderLayout.NORTH);
        top.add(buildTiles(), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(buildDetails(), BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refresh();
            }
        });

        refresh();
    }

    private JComponent buildHero() {
        HeroPanel hero = new HeroPanel();
        hero.setLayout(new BorderLayout(26, 0));
        hero.setBorder(new EmptyBorder(24, 34, 28, 32));
        hero.setPreferredSize(new Dimension(10, 180));

        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        nameLabel.setForeground(Color.WHITE);

        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roleLabel.setForeground(new Color(200, 225, 212));

        Pill pill = new Pill("ACTIVE SESSION");

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pill.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(Box.createVerticalGlue());
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(roleLabel);
        text.add(Box.createVerticalStrut(12));
        text.add(pill);
        text.add(Box.createVerticalGlue());

        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.add(avatar);

        hero.add(avatarWrap, BorderLayout.WEST);
        hero.add(text, BorderLayout.CENTER);
        return hero;
    }

    private JComponent buildTiles() {
        JPanel tiles = new JPanel(new GridLayout(1, 3, 16, 0));
        tiles.setOpaque(false);
        tiles.setPreferredSize(new Dimension(10, 92));
        tiles.add(new InfoTile("check", "STATUS", "Active"));
        tiles.add(new InfoTile("home", "APPLICATION", "AgriMonitor"));
        tiles.add(new InfoTile("clock", "SESSION OPENED", openedAt));
        return tiles;
    }

    private JComponent buildDetails() {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(22, 28, 24, 28));

        Heading title = new Heading("Account Details");

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.add(row("profile", "Username", userValue));
        rows.add(row("mail", "Email", emailValue));
        rows.add(row("check", "Status", new JLabel("Active")));
        rows.add(row("home", "Application", new JLabel("AgriMonitor")));
        rows.add(row("field", "System", new JLabel("Smart Agriculture Management System")));
        rows.add(row("clock", "Session Opened", new JLabel(openedAt)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(rows, BorderLayout.NORTH);

        card.add(title, BorderLayout.NORTH);
        card.add(top, BorderLayout.CENTER);
        return card;
    }

    private JComponent row(String iconKey, String label, JLabel value) {
        IconRow p = new IconRow(iconKey);
        p.setLayout(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(14, 56, 14, 4)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(UITheme.TEXT_LIGHT);

        value.setFont(new Font("Segoe UI", Font.BOLD, 14));
        value.setForeground(UITheme.TEXT_DARK);
        value.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(l, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        return p;
    }

    private void refresh() {
        String user = Session.getUsername();
        if (user == null || user.trim().isEmpty()) user = "User";
        nameLabel.setText(user);
        userValue.setText(user);
        avatar.setLetter(user.substring(0, 1).toUpperCase());

        roleLabel.setText(ROLE_TEXT);
        emailValue.setText("Loading...");
        loadEmail(Session.getUserId());
    }

    private void loadEmail(final String userId) {
        if (userId == null) {
            applyEmail(null);
            return;
        }
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    Document d = MongoDBConnection.getDatabase()
                            .getCollection("users")
                            .find(Filters.eq("_id", new ObjectId(userId)))
                            .first();
                    return d == null ? null : d.getString("email");
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void done() {
                try {
                    applyEmail(get());
                } catch (Exception e) {
                    applyEmail(null);
                }
            }
        }.execute();
    }

    private void applyEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            emailValue.setText("Not set");
            roleLabel.setText(ROLE_TEXT);
        } else {
            emailValue.setText(email.trim());
            roleLabel.setText(email.trim());
        }
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static String clip(String s, FontMetrics fm, int max) {
        if (s == null || max <= 0) return "";
        if (fm.stringWidth(s) <= max) return s;
        String e = "...";
        int n = s.length();
        while (n > 0 && fm.stringWidth(s.substring(0, n) + e) > max) n--;
        return s.substring(0, n) + e;
    }

    private static void drawIcon(Graphics2D g, String key, int x, int y, int size, Color c) {
        if ("check".equals(key)) {
            Path2D p = new Path2D.Float();
            p.moveTo(x + size * 0.20, y + size * 0.55);
            p.lineTo(x + size * 0.42, y + size * 0.76);
            p.lineTo(x + size * 0.80, y + size * 0.28);
            g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c);
            g.draw(p);
        } else if ("clock".equals(key)) {
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c);
            g.draw(new Ellipse2D.Double(x + 2, y + 2, size - 4, size - 4));
            double cx = x + size / 2.0;
            double cy = y + size / 2.0;
            g.draw(new java.awt.geom.Line2D.Double(cx, cy, cx, y + size * 0.27));
            g.draw(new java.awt.geom.Line2D.Double(cx, cy, x + size * 0.70, cy));
        } else if ("mail".equals(key)) {
            g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c);
            g.draw(new RoundRectangle2D.Double(x + 2, y + 4, size - 4, size - 8, 4, 4));
            Path2D p = new Path2D.Float();
            p.moveTo(x + 3.5, y + 6.5);
            p.lineTo(x + size / 2.0, y + size * 0.58);
            p.lineTo(x + size - 3.5, y + 6.5);
            g.draw(p);
        } else {
            UITheme.Icons.paint(g, key, x, y, size, c);
        }
    }

    private static class Avatar extends JComponent {
        private String letter = "U";

        Avatar() {
            setPreferredSize(new Dimension(116, 116));
        }

        void setLetter(String l) {
            letter = l;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(new Color(255, 255, 255, 34));
            g.fill(new Ellipse2D.Double(0, 0, w, h));
            g.setPaint(new GradientPaint(0, 0, new Color(240, 204, 120), 0, h, UITheme.ACCENT));
            g.fill(new Ellipse2D.Double(7, 7, w - 14, h - 14));
            g.setFont(new Font("Segoe UI", Font.BOLD, 46));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(UITheme.PRIMARY_DARK);
            g.drawString(letter, (w - fm.stringWidth(letter)) / 2f, (h - fm.getHeight()) / 2f + fm.getAscent());
            g.dispose();
        }
    }

    private static class Pill extends JComponent {
        private final String text;

        Pill(String text) {
            this.text = text;
            Dimension d = new Dimension(156, 26);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(new Color(255, 255, 255, 30));
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
            g.setColor(new Color(255, 255, 255, 60));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, h, h));
            g.setColor(new Color(110, 231, 183));
            g.fill(new Ellipse2D.Double(12, (h - 8) / 2.0, 8, 8));
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(252, 211, 120));
            g.drawString(text, 28, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    private static class Heading extends JLabel {
        Heading(String text) {
            super(text);
            setFont(new Font("Segoe UI", Font.BOLD, 17));
            setForeground(UITheme.PRIMARY_DARK);
            setBorder(new EmptyBorder(2, 16, 2, 0));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int h = getHeight();
            g.setPaint(new GradientPaint(0, 4, UITheme.PRIMARY, 0, h - 4, UITheme.PRIMARY_DARK));
            g.fill(new RoundRectangle2D.Double(0, 4, 6, h - 8, 6, 6));
            g.dispose();
            super.paintComponent(g0);
        }
    }

    private static class IconRow extends JPanel {
        private final String key;

        IconRow(String key) {
            this.key = key;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int d = 36;
            int y = (getHeight() - 1 - d) / 2;
            g.setColor(alpha(UITheme.PRIMARY, 30));
            g.fill(new Ellipse2D.Double(2, y, d, d));
            drawIcon(g, key, 2 + (d - 18) / 2, y + (d - 18) / 2, 18, UITheme.PRIMARY_DARK);
            g.dispose();
        }
    }

    private static class InfoTile extends JComponent {
        private final String key;
        private final String label;
        private final String value;

        InfoTile(String key, String label, String value) {
            this.key = key;
            this.label = label;
            this.value = value;
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();

            g.setColor(new Color(0, 0, 0, 10));
            g.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 4, 22, 22));
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 3, 22, 22));
            g.setColor(UITheme.BORDER_COLOR);
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 4, 22, 22));

            int d = 46;
            int bx = 18;
            int by = (h - 3 - d) / 2;
            g.setPaint(new GradientPaint(0, by, new Color(240, 204, 120), 0, by + d, UITheme.ACCENT));
            g.fill(new Ellipse2D.Double(bx, by, d, d));
            drawIcon(g, key, bx + (d - 22) / 2, by + (d - 22) / 2, 22, UITheme.PRIMARY_DARK);

            int tx = bx + d + 16;
            int max = w - tx - 14;
            g.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g.setColor(UITheme.TEXT_LIGHT);
            g.drawString(clip(label, g.getFontMetrics(), max), tx, by + 16);

            g.setFont(new Font("Segoe UI", Font.BOLD, 16));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(UITheme.PRIMARY_DARK);
            g.drawString(clip(value, fm, max), tx, by + 38);
            g.dispose();
        }
    }

    private static class Card extends JPanel {
        Card() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(new Color(0, 0, 0, 10));
            g.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 4, 22, 22));
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 3, 22, 22));
            g.setColor(UITheme.BORDER_COLOR);
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 4, 22, 22));
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
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 22, 22);
            g.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY_DARK, w, h, UITheme.PRIMARY));
            g.fill(shape);
            g.setClip(shape);
            g.setColor(new Color(255, 255, 255, 14));
            g.fillOval(w - 220, -90, 300, 300);
            g.setColor(new Color(255, 255, 255, 10));
            g.fillOval(w - 330, h - 100, 220, 220);
            g.setColor(UITheme.ACCENT);
            g.fillRect(0, h - 4, w, 4);
            g.dispose();
        }
    }
}