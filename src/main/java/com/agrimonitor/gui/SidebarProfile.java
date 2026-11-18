package com.agrimonitor.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

public class SidebarProfile extends JPanel {

    private final String name;
    private final Runnable onClick;
    private boolean hover = false;

    public SidebarProfile(String name) {
        this(name, null);
    }

    public SidebarProfile(String name, Runnable onClick) {
        this.name = (name == null || name.trim().isEmpty()) ? "User" : name.trim();
        this.onClick = onClick;
        setOpaque(false);
        setPreferredSize(new Dimension(210, 92));
        setMinimumSize(new Dimension(180, 92));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        setAlignmentX(Component.LEFT_ALIGNMENT);

        if (onClick != null) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("Open profile");
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

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        SidebarProfile.this.onClick.run();
                    }
                }
            });
        }
    }

    private String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String dots = "...";
        int end = text.length();
        while (end > 1 && fm.stringWidth(text.substring(0, end) + dots) > maxWidth) {
            end--;
        }
        return text.substring(0, end) + dots;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int w = getWidth();
        int h = getHeight();

        Shape card = new RoundRectangle2D.Float(0, 0, w, h, 20, 20);
        g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY_DARK, w, h, UITheme.PRIMARY));
        g2.fill(card);

        if (hover) {
            g2.setColor(new Color(255, 255, 255, 22));
            g2.fill(card);
        }

        Shape oldClip = g2.getClip();
        g2.setClip(card);
        g2.setColor(UITheme.ACCENT);
        g2.fillRect(0, h - 3, w, 3);
        g2.setClip(oldClip);

        int ad = 50;
        int ax = 16;
        int ay = (h - 3 - ad) / 2;

        g2.setColor(new Color(255, 255, 255, 38));
        g2.fill(new Ellipse2D.Float(ax - 5, ay - 5, ad + 10, ad + 10));

        g2.setPaint(new GradientPaint(0, ay, new Color(240, 200, 110), 0, ay + ad, UITheme.ACCENT));
        g2.fill(new Ellipse2D.Float(ax, ay, ad, ad));

        String initial = name.substring(0, 1).toUpperCase();
        g2.setFont(new Font("Segoe UI", Font.BOLD, 24));
        FontMetrics ifm = g2.getFontMetrics();
        g2.setColor(UITheme.PRIMARY_DARK);
        g2.drawString(initial,
                ax + (ad - ifm.stringWidth(initial)) / 2,
                ay + (ad - ifm.getHeight()) / 2 + ifm.getAscent());

        int tx = ax + ad + 16;
        int arrowSpace = onClick != null ? 22 : 0;
        int maxText = w - tx - 10 - arrowSpace;

        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        FontMetrics nfm = g2.getFontMetrics();
        g2.setColor(Color.WHITE);
        g2.drawString(fit(name, nfm, maxText), tx, 42);

        g2.setColor(new Color(110, 231, 183));
        g2.fill(new Ellipse2D.Float(tx, 54, 7, 7));

        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        g2.setColor(new Color(252, 211, 120));
        g2.drawString("ACTIVE SESSION", tx + 13, 62);

        if (onClick != null) {
            int cx = w - 18;
            int cy = (h - 3) / 2;
            Path2D arrow = new Path2D.Float();
            arrow.moveTo(cx - 3, cy - 6);
            arrow.lineTo(cx + 3, cy);
            arrow.lineTo(cx - 3, cy + 6);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 255, 255, hover ? 230 : 150));
            g2.draw(arrow);
        }

        g2.dispose();
    }
}