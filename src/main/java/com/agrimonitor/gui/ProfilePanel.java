package com.agrimonitor.gui;

import com.agrimonitor.session.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfilePanel extends JPanel {

    private final JLabel nameLabel = new JLabel(" ");
    private final JLabel userValue = new JLabel(" ");
    private final Avatar avatar = new Avatar();
    private final String openedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

    public ProfilePanel() {
        setLayout(new BorderLayout(0, 18));
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        add(buildHero(), BorderLayout.NORTH);
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
        hero.setLayout(new BorderLayout(24, 0));
        hero.setBorder(new EmptyBorder(26, 32, 26, 32));
        hero.setPreferredSize(new Dimension(10, 190));

        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        nameLabel.setForeground(Color.WHITE);

        JLabel role = new JLabel("AgriMonitor Account");
        role.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        role.setForeground(new Color(200, 220, 208));

        JLabel badge = new JLabel("  ACTIVE SESSION  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(new Color(223, 196, 120));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        role.setAlignmentX(Component.LEFT_ALIGNMENT);
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(Box.createVerticalGlue());
        text.add(nameLabel);
        text.add(Box.createVerticalStrut(4));
        text.add(role);
        text.add(Box.createVerticalStrut(10));
        text.add(badge);
        text.add(Box.createVerticalGlue());

        JPanel avatarWrap = new JPanel(new GridBagLayout());
        avatarWrap.setOpaque(false);
        avatarWrap.add(avatar);

        hero.add(avatarWrap, BorderLayout.WEST);
        hero.add(text, BorderLayout.CENTER);
        return hero;
    }

    private JComponent buildDetails() {
        Card card = new Card();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(20, 26, 20, 26));

        JLabel title = new JLabel("Account Details");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UITheme.PRIMARY_DARK);

        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.add(row("Username", userValue));
        rows.add(row("Status", new JLabel("Active")));
        rows.add(row("Application", new JLabel("AgriMonitor")));
        rows.add(row("System", new JLabel("Smart Agriculture Management System")));
        rows.add(row("Session Opened", new JLabel(openedAt)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(rows, BorderLayout.NORTH);

        card.add(title, BorderLayout.NORTH);
        card.add(top, BorderLayout.CENTER);
        return card;
    }

    private JComponent row(String label, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(14, 2, 14, 2)));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

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
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private static class Avatar extends JComponent {
        private String letter = "U";

        Avatar() {
            setPreferredSize(new Dimension(112, 112));
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
            g.setColor(new Color(255, 255, 255, 30));
            g.fill(new Ellipse2D.Double(0, 0, w, h));
            g.setPaint(new GradientPaint(0, 0, new Color(236, 206, 120), 0, h, UITheme.ACCENT));
            g.fill(new Ellipse2D.Double(6, 6, w - 12, h - 12));
            g.setFont(new Font("Segoe UI", Font.BOLD, 46));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(UITheme.PRIMARY_DARK);
            g.drawString(letter, (w - fm.stringWidth(letter)) / 2f, (h - fm.getHeight()) / 2f + fm.getAscent());
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
            int w = getWidth() - 1;
            int h = getHeight() - 1;
            g.setColor(Color.WHITE);
            g.fillRoundRect(0, 0, w, h, 18, 18);
            g.setColor(UITheme.BORDER_COLOR);
            g.drawRoundRect(0, 0, w, h, 18, 18);
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
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w, h, 24, 24);
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
