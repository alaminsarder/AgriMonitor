package com.agrimonitor.gui;

import com.agrimonitor.dao.ChatHistoryDAO;
import com.agrimonitor.service.AIChatService;
import com.agrimonitor.service.ChatMessage;
import com.agrimonitor.service.FarmContextBuilder;
import com.agrimonitor.service.FarmAssistantPrompt;
import com.agrimonitor.session.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ChatPanel extends JPanel {

    private static final Color ERROR_BG = new Color(254, 226, 226);
    private static final Color ERROR_FG = new Color(153, 27, 27);
    private static final Color SURFACE = new Color(247, 250, 248);
    private static final int MAX_HISTORY_MESSAGES = 12;
    private static final int MAX_SHOWN_ON_OPEN = 40;

    private final AIChatService ai = new AIChatService();
    private final List<ChatMessage> history = new ArrayList<>();

    private final MessagesPanel messages = new MessagesPanel();
    private final JScrollPane scroll = new JScrollPane(messages);
    private final JTextArea input = new JTextArea(2, 20);
    private final SendButton sendButton = new SendButton();
    private final JPanel chipsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final InputBox inputBox = new InputBox();

    private ChatHistoryDAO historyDao;
    private Row typingRow;
    private TypingDots dots;
    private Timer typingTimer;
    private boolean busy = false;

    public ChatPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BACKGROUND);
        setBorder(new EmptyBorder(14, 18, 14, 18));

        UITheme.RoundedPanel card = new UITheme.RoundedPanel(22);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(new EmptyBorder(20, 24, 22, 24));

        card.add(buildHeader(), BorderLayout.NORTH);

        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(SURFACE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setUI(new ThinScrollUI());
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Surface surface = new Surface();
        surface.setLayout(new BorderLayout());
        surface.setBorder(new EmptyBorder(8, 8, 8, 8));
        surface.add(scroll, BorderLayout.CENTER);
        card.add(surface, BorderLayout.CENTER);

        card.add(buildBottom(), BorderLayout.SOUTH);
        add(card, BorderLayout.CENTER);

        messages.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                messages.revalidate();
            }
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                FarmContextBuilder.invalidate();
            }
        });

        showWelcome();
        loadSavedHistory();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.createCardTitle("AI Farming Assistant"), BorderLayout.CENTER);

        StatusPill status = new StatusPill(
                ai.isConfigured() ? "Online - " + ai.getModel() : "AI key not set",
                ai.isConfigured());

        NewChatButton clear = new NewChatButton("New Chat");
        clear.addActionListener(e -> clearChat());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(status);
        right.add(clear);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        chipsRow.setOpaque(false);
        String[] samples = {
                "My crop leaves are turning yellow",
                "How often should I irrigate my crop?",
                "Insects are attacking my field",
                "How can I increase my profit?"
        };
        for (String s : samples) chipsRow.add(chip(s));
        bottom.add(chipsRow, BorderLayout.NORTH);

        input.setFont(UITheme.FONT_BODY);
        input.setLineWrap(true);
        input.setWrapStyleWord(true);
        input.setOpaque(false);
        input.setBorder(null);
        input.setCaretColor(UITheme.PRIMARY);
        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "newline");
        input.getActionMap().put("send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFromInput();
            }
        });
        input.getActionMap().put("newline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                input.append("\n");
            }
        });
        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                inputBox.setFocused(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                inputBox.setFocused(false);
            }
        });

        JScrollPane inputScroll = new JScrollPane(input);
        inputScroll.setBorder(null);
        inputScroll.setOpaque(false);
        inputScroll.getViewport().setOpaque(false);
        inputScroll.getVerticalScrollBar().setUI(new ThinScrollUI());
        inputScroll.getVerticalScrollBar().setPreferredSize(new Dimension(10, 0));
        inputScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        inputBox.setLayout(new BorderLayout());
        inputBox.setBorder(new EmptyBorder(8, 16, 8, 10));
        inputBox.add(inputScroll, BorderLayout.CENTER);
        inputBox.setPreferredSize(new Dimension(100, 64));

        sendButton.addActionListener(e -> sendFromInput());

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.add(inputBox, BorderLayout.CENTER);
        row.add(sendButton, BorderLayout.EAST);
        bottom.add(row, BorderLayout.CENTER);

        JLabel hint = new JLabel("Enter = send, Shift+Enter = new line. AI advice can be wrong - for serious problems ask a local agriculture officer.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_LIGHT);
        bottom.add(hint, BorderLayout.SOUTH);
        return bottom;
    }

    private JButton chip(String text) {
        ChipButton b = new ChipButton(text);
        b.addActionListener(e -> send(text));
        return b;
    }

    private void showWelcome() {
        String name = Session.getUsername() == null ? "" : " " + Session.getUsername();
        addBubble("Hello" + name + "! I am your farming assistant.\n"
                + "Tell me about your problem - a crop disease, insects, irrigation, fertilizer, weather or profit - "
                + "and we will discuss it together. You can write in Bangla or English.", false, false);
        if (!ai.isConfigured()) {
            addBubble("The AI key is not set yet. Add AI_API_KEY to the .env file in the project folder "
                    + "(see .env.example), then restart the app.", false, true);
        }
    }

    private void clearChat() {
        if (busy) return;
        history.clear();
        new Thread(() -> {
            ChatHistoryDAO dao = dao();
            if (dao != null) dao.clear();
        }, "chat-clear").start();
        messages.removeAll();
        chipsRow.setVisible(true);
        showWelcome();
        messages.revalidate();
        messages.repaint();
    }

    private void sendFromInput() {
        send(input.getText());
    }

    private void send(String raw) {
        if (busy) return;
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) return;

        input.setText("");
        chipsRow.setVisible(false);
        addBubble(text, true, false);
        history.add(new ChatMessage(ChatMessage.USER, text));
        setBusy(true);
        showTyping();

        final List<ChatMessage> toSend = recentHistory();
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return ai.chat(buildSystemPrompt(), toSend);
            }

            @Override
            protected void done() {
                hideTyping();
                try {
                    String reply = get();
                    history.add(new ChatMessage(ChatMessage.ASSISTANT, reply));
                    addBubble(cleanMarkdown(reply), false, false);
                    saveInBackground(text, reply);
                } catch (Exception ex) {
                    Throwable t = ex.getCause() != null ? ex.getCause() : ex;
                    if (!history.isEmpty()) history.remove(history.size() - 1);
                    input.setText(text);
                    addBubble(t.getMessage() == null ? "Something went wrong. Please try again." : t.getMessage(), false, true);
                }
                setBusy(false);
                input.requestFocusInWindow();
            }
        }.execute();
    }

    protected String buildSystemPrompt() {
        return FarmAssistantPrompt.build(FarmContextBuilder.build());
    }

    private synchronized ChatHistoryDAO dao() {
        if (historyDao == null) {
            try {
                historyDao = new ChatHistoryDAO();
            } catch (Throwable t) {
                historyDao = null;
            }
        }
        return historyDao;
    }

    private void loadSavedHistory() {
        setBusy(true);
        new SwingWorker<List<ChatMessage>, Void>() {
            @Override
            protected List<ChatMessage> doInBackground() {
                ChatHistoryDAO dao = dao();
                return dao == null ? new ArrayList<ChatMessage>() : dao.loadRecent(MAX_SHOWN_ON_OPEN);
            }

            @Override
            protected void done() {
                try {
                    List<ChatMessage> saved = get();
                    if (!saved.isEmpty() && history.isEmpty()) {
                        history.addAll(saved);
                        chipsRow.setVisible(false);
                        for (ChatMessage m : saved) {
                            addBubble(m.isUser() ? m.getText() : cleanMarkdown(m.getText()), m.isUser(), false);
                        }
                    }
                } catch (Exception ignored) {
                }
                setBusy(false);
            }
        }.execute();
    }

    private void saveInBackground(final String question, final String answer) {
        new Thread(() -> {
            ChatHistoryDAO dao = dao();
            if (dao != null) dao.saveExchange(question, answer);
        }, "chat-save").start();
    }

    private List<ChatMessage> recentHistory() {
        int from = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        List<ChatMessage> list = new ArrayList<>(history.subList(from, history.size()));
        while (!list.isEmpty() && !list.get(0).isUser()) list.remove(0);
        return list;
    }

    private void setBusy(boolean b) {
        busy = b;
        sendButton.setEnabled(!b);
        input.setEnabled(!b);
    }

    private void addBubble(String text, boolean user, boolean error) {
        Bubble bubble = new Bubble(text, user, error);
        messages.add(new Row(bubble, user));
        messages.revalidate();
        messages.repaint();
        scrollToBottom();
    }

    private void showTyping() {
        dots = new TypingDots();
        typingRow = new Row(dots, false);
        messages.add(typingRow);
        messages.revalidate();
        scrollToBottom();
        typingTimer = new Timer(260, e -> {
            if (dots != null) dots.tick();
        });
        typingTimer.start();
    }

    private void hideTyping() {
        if (typingTimer != null) typingTimer.stop();
        if (typingRow != null) {
            messages.remove(typingRow);
            typingRow = null;
            dots = null;
            messages.revalidate();
            messages.repaint();
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    static String cleanMarkdown(String s) {
        if (s == null) return "";
        s = s.replace("\r", "");
        s = s.replaceAll("(?m)^\\s*#{1,6}\\s*", "");
        s = s.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        s = s.replaceAll("(?m)^\\s*[*\\-]\\s+", "\u2022 ");
        s = s.replaceAll("(?<![\\w*])\\*(\\S[^*\\n]*?)\\*(?![\\w*])", "$1");
        s = s.replaceAll("`([^`\\n]*)`", "$1");
        return s.trim();
    }

    private static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private static void softCard(Graphics2D g, int w, int h, Color fill, Color border) {
        g.setColor(new Color(0, 0, 0, 10));
        g.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 3, 22, 22));
        g.setColor(fill);
        g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 3, 22, 22));
        g.setColor(border);
        g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 4, 22, 22));
    }

    private static class Surface extends JPanel {
        Surface() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            g.setColor(SURFACE);
            g.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 20, 20));
            g.setColor(UITheme.BORDER_COLOR);
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 2, getHeight() - 2, 20, 20));
            g.dispose();
        }
    }

    private static class ThinScrollUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(190, 205, 197);
            trackColor = new Color(0, 0, 0, 0);
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return empty();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return empty();
        }

        private JButton empty() {
            JButton b = new JButton();
            Dimension d = new Dimension(0, 0);
            b.setPreferredSize(d);
            b.setMinimumSize(d);
            b.setMaximumSize(d);
            return b;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x + 3, r.y + 2, r.width - 6, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    private static class StatusPill extends JComponent {
        private final String text;
        private final boolean ok;

        StatusPill(String text, boolean ok) {
            this.text = text;
            this.ok = ok;
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(new Font(UIKit.FAMILY, Font.BOLD, 12));
            return new Dimension(fm.stringWidth(text) + 44, 30);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            Color c = ok ? UITheme.SUCCESS : UITheme.DANGER;
            g.setColor(alpha(c, 26));
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
            g.setColor(alpha(c, 90));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, h, h));
            g.setColor(c);
            g.fill(new Ellipse2D.Double(13, (h - 8) / 2.0, 8, 8));
            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 12));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(ok ? UITheme.PRIMARY_DARK : UITheme.DANGER);
            g.drawString(text, 29, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    private static class NewChatButton extends JButton {
        private boolean hover = false;

        NewChatButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(126, 38));
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
            Color fg = hover ? Color.WHITE : UITheme.PRIMARY_DARK;
            g.setColor(hover ? UITheme.PRIMARY : Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
            g.setStroke(new BasicStroke(1.6f));
            g.setColor(UITheme.PRIMARY);
            g.draw(new RoundRectangle2D.Double(0.8, 0.8, w - 2.6, h - 2.6, h, h));

            int cx = 22;
            int cy = h / 2;
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(fg);
            g.drawLine(cx - 5, cy, cx + 5, cy);
            g.drawLine(cx, cy - 5, cx, cy + 5);

            g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(getText(), 38, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    private static class ChipButton extends JButton {
        private boolean hover = false;

        ChipButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(new Font(UIKit.FAMILY, Font.PLAIN, 12));
            return new Dimension(fm.stringWidth(getText()) + 34, 34);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(hover ? UITheme.PRIMARY : new Color(236, 247, 242));
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
            g.setColor(alpha(UITheme.PRIMARY, hover ? 255 : 90));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, h, h));
            g.setFont(new Font(UIKit.FAMILY, Font.PLAIN, 12));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(hover ? Color.WHITE : UITheme.PRIMARY_DARK);
            g.drawString(getText(), 17, (h - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }

    private static class InputBox extends JPanel {
        private boolean focused = false;

        InputBox() {
            setOpaque(false);
        }

        void setFocused(boolean f) {
            focused = f;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            g.setColor(Color.WHITE);
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 22, 22));
            g.setStroke(new BasicStroke(focused ? 2f : 1.2f));
            g.setColor(focused ? UITheme.PRIMARY : UITheme.BORDER_COLOR);
            g.draw(new RoundRectangle2D.Double(1, 1, w - 3, h - 3, 22, 22));
            g.dispose();
        }
    }

    private static class SendButton extends JButton {
        private boolean hover = false;

        SendButton() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(64, 64));
            setToolTipText("Send");
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
            RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 22, 22);
            if (isEnabled()) {
                Color top = hover ? UITheme.PRIMARY.brighter() : UITheme.PRIMARY;
                g.setPaint(new GradientPaint(0, 0, top, w, h, UITheme.PRIMARY_DARK));
            } else {
                g.setColor(new Color(190, 205, 197));
            }
            g.fill(shape);

            Path2D plane = new Path2D.Double();
            plane.moveTo(w * 0.28, h * 0.50);
            plane.lineTo(w * 0.74, h * 0.28);
            plane.lineTo(w * 0.60, h * 0.72);
            plane.lineTo(w * 0.50, h * 0.55);
            plane.closePath();
            g.setColor(Color.WHITE);
            g.fill(plane);
            g.dispose();
        }
    }

    private static class MessagesPanel extends JPanel implements Scrollable {
        MessagesPanel() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(SURFACE);
            setBorder(new EmptyBorder(10, 6, 10, 6));
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
            return 80;
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

    private static class AvatarDot extends JComponent {
        private final boolean user;

        AvatarDot(boolean user) {
            this.user = user;
            setPreferredSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            if (user) {
                g.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, 0, h, UITheme.PRIMARY_DARK));
                g.fill(new Ellipse2D.Double(0, 0, w - 1, h - 1));
                String name = Session.getUsername();
                String letter = (name == null || name.trim().isEmpty()) ? "U" : name.trim().substring(0, 1).toUpperCase();
                g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 15));
                FontMetrics fm = g.getFontMetrics();
                g.setColor(Color.WHITE);
                g.drawString(letter, (w - fm.stringWidth(letter)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(240, 204, 120), 0, h, UITheme.ACCENT));
                g.fill(new Ellipse2D.Double(0, 0, w - 1, h - 1));
                UITheme.Icons.paint(g, "crop", (w - 20) / 2, (h - 20) / 2, 20, UITheme.PRIMARY_DARK);
            }
            g.dispose();
        }
    }

    private static class Row extends JPanel {
        Row(JComponent bubble, boolean user) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(6, 8, 6, 8));

            JPanel avatarWrap = new JPanel(new BorderLayout());
            avatarWrap.setOpaque(false);
            avatarWrap.add(new AvatarDot(user), BorderLayout.NORTH);

            JPanel holder = new JPanel(new BorderLayout(10, 0));
            holder.setOpaque(false);
            holder.add(bubble, BorderLayout.CENTER);
            if (user) {
                holder.add(avatarWrap, BorderLayout.EAST);
                add(holder, BorderLayout.EAST);
            } else {
                holder.add(avatarWrap, BorderLayout.WEST);
                add(holder, BorderLayout.WEST);
            }
        }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
        }
    }

    private static class TypingDots extends JComponent {
        private int phase = 0;

        TypingDots() {
            setPreferredSize(new Dimension(78, 46));
        }

        void tick() {
            phase = (phase + 1) % 3;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            smooth(g);
            int w = getWidth();
            int h = getHeight();
            softCard(g, w, h, Color.WHITE, UITheme.BORDER_COLOR);
            int cy = (h - 3) / 2;
            for (int i = 0; i < 3; i++) {
                boolean on = i == phase;
                double d = on ? 10 : 7;
                int cx = 24 + i * 15;
                g.setColor(on ? UITheme.PRIMARY : new Color(190, 205, 197));
                g.fill(new Ellipse2D.Double(cx - d / 2, cy - d / 2, d, d));
            }
            g.dispose();
        }
    }

    private class Bubble extends JPanel {
        private final JTextArea area = new JTextArea();
        private final boolean user;
        private final boolean error;

        Bubble(String text, boolean user, boolean error) {
            super(new BorderLayout());
            this.user = user;
            this.error = error;
            setOpaque(false);
            area.setText(text);
            area.setEditable(false);
            area.setOpaque(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(UITheme.FONT_BODY);
            area.setForeground(error ? ERROR_FG : (user ? Color.WHITE : UITheme.TEXT_DARK));
            area.setBorder(null);
            area.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
            setBorder(new EmptyBorder(12, 18, 15, 18));
            add(area, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            int viewW = messages.getWidth() > 0 ? messages.getWidth() : 700;
            int maxOuter = Math.max(260, (int) (viewW * 0.70) - 46);
            int padX = 36;
            int padY = 27;
            int maxInner = maxOuter - padX;

            FontMetrics fm = area.getFontMetrics(area.getFont());
            int natural = 40;
            for (String line : area.getText().split("\n", -1)) {
                natural = Math.max(natural, fm.stringWidth(line) + 6);
            }
            int w = Math.min(maxInner, natural);
            area.setSize(w, Short.MAX_VALUE);
            int h = area.getPreferredSize().height;
            return new Dimension(w + padX, h + padY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth();
            int h = getHeight();
            if (error) {
                softCard(g2, w, h, ERROR_BG, new Color(252, 165, 165));
            } else if (user) {
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 3, 22, 22));
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, w, h, UITheme.PRIMARY_DARK));
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 3, 22, 22));
            } else {
                softCard(g2, w, h, Color.WHITE, UITheme.BORDER_COLOR);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}