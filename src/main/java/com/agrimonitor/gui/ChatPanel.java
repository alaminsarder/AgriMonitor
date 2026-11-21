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
    private static final int TYPING_TICK_MS = 260;

    private static final String[] SAMPLE_PROMPTS = {
            "My crop leaves are turning yellow",
            "How often should I irrigate my crop?",
            "Insects are attacking my field",
            "How can I increase my profit?"
    };

    private final AIChatService aiService = new AIChatService();
    private final List<ChatMessage> history = new ArrayList<>();

    private final MessagesPanel messagesPanel = new MessagesPanel();
    private final JScrollPane scrollPane = new JScrollPane(messagesPanel);
    private final JTextArea inputArea = new JTextArea(2, 20);
    private final SendButton sendButton = new SendButton();
    private final JPanel chipsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
    private final InputBox inputBox = new InputBox();

    private ChatHistoryDAO historyDao;
    private Row typingRow;
    private TypingDots typingDots;
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
        card.add(buildMessageSurface(), BorderLayout.CENTER);
        card.add(buildBottom(), BorderLayout.SOUTH);

        add(card, BorderLayout.CENTER);

        registerListeners();
        showWelcome();
        loadSavedHistory();
    }

    private void registerListeners() {
        messagesPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                messagesPanel.revalidate();
            }
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                FarmContextBuilder.invalidate();
            }
        });
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(UITheme.createCardTitle("AI Farming Assistant"), BorderLayout.CENTER);

        boolean configured = aiService.isConfigured();
        String statusText = configured ? "Online - " + aiService.getModel() : "AI key not set";
        StatusPill status = new StatusPill(statusText, configured);

        NewChatButton clearButton = new NewChatButton("New Chat");
        clearButton.addActionListener(e -> clearChat());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(status);
        right.add(clearButton);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildMessageSurface() {
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setBackground(SURFACE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new ThinScrollUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Surface surface = new Surface();
        surface.setLayout(new BorderLayout());
        surface.setBorder(new EmptyBorder(8, 8, 8, 8));
        surface.add(scrollPane, BorderLayout.CENTER);
        return surface;
    }

    private JComponent buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        chipsRow.setOpaque(false);
        for (String sample : SAMPLE_PROMPTS) {
            chipsRow.add(createChipButton(sample));
        }
        bottom.add(chipsRow, BorderLayout.NORTH);

        configureInputArea();

        JScrollPane inputScroll = new JScrollPane(inputArea);
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

    private void configureInputArea() {
        inputArea.setFont(UITheme.FONT_BODY);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setOpaque(false);
        inputArea.setBorder(null);
        inputArea.setCaretColor(UITheme.PRIMARY);

        inputArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
        inputArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.SHIFT_DOWN_MASK), "newline");

        inputArea.getActionMap().put("send", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFromInput();
            }
        });

        inputArea.getActionMap().put("newline", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputArea.append("\n");
            }
        });

        inputArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                inputBox.setFocused(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                inputBox.setFocused(false);
            }
        });
    }

    private JButton createChipButton(String text) {
        ChipButton button = new ChipButton(text);
        button.addActionListener(e -> send(text));
        return button;
    }

    private void showWelcome() {
        String username = Session.getUsername();
        String greetingName = (username == null || username.isBlank()) ? "" : " " + username.trim();

        addBubble("Hello" + greetingName + "! I am your farming assistant.\n"
                + "Tell me about your problem - a crop disease, insects, irrigation, fertilizer, weather or profit - "
                + "and we will discuss it together. You can write in Bangla or English.", false, false);

        if (!aiService.isConfigured()) {
            addBubble("The AI key is not set yet. Add AI_API_KEY to the .env file in the project folder "
                    + "(see .env.example), then restart the app.", false, true);
        }
    }

    private void clearChat() {
        if (busy) return;

        history.clear();
        runAsync(() -> {
            ChatHistoryDAO dao = getDao();
            if (dao != null) dao.clear();
        }, "chat-clear");

        messagesPanel.removeAll();
        chipsRow.setVisible(true);
        showWelcome();
        refreshMessages();
    }

    private void sendFromInput() {
        send(inputArea.getText());
    }

    private void send(String rawText) {
        if (busy) return;

        String text = (rawText == null) ? "" : rawText.trim();
        if (text.isEmpty()) return;

        inputArea.setText("");
        chipsRow.setVisible(false);
        addBubble(text, true, false);

        ChatMessage userMessage = ChatMessage.user(text);
        history.add(userMessage);

        setBusy(true);
        showTyping();

        final List<ChatMessage> payloadHistory = recentHistory();
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return aiService.chat(buildSystemPrompt(), payloadHistory);
            }

            @Override
            protected void done() {
                hideTyping();
                try {
                    String reply = get();
                    history.add(ChatMessage.assistant(reply));
                    addBubble(cleanMarkdown(reply), false, false);
                    saveExchangeAsync(text, reply);
                } catch (Exception ex) {
                    Throwable cause = (ex.getCause() != null) ? ex.getCause() : ex;
                    history.remove(userMessage);
                    inputArea.setText(text);
                    String errorMsg = (cause.getMessage() == null || cause.getMessage().isBlank())
                            ? "Something went wrong. Please try again."
                            : cause.getMessage();
                    addBubble(errorMsg, false, true);
                } finally {
                    setBusy(false);
                    inputArea.requestFocusInWindow();
                }
            }
        }.execute();
    }

    protected String buildSystemPrompt() {
        return FarmAssistantPrompt.build(FarmContextBuilder.build());
    }

    private synchronized ChatHistoryDAO getDao() {
        if (historyDao == null) {
            try {
                historyDao = new ChatHistoryDAO();
            } catch (Throwable ignored) {
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
                ChatHistoryDAO dao = getDao();
                return (dao == null) ? List.of() : dao.loadRecent(MAX_SHOWN_ON_OPEN);
            }

            @Override
            protected void done() {
                try {
                    List<ChatMessage> saved = get();
                    if (saved != null && !saved.isEmpty() && history.isEmpty()) {
                        history.addAll(saved);
                        chipsRow.setVisible(false);
                        for (ChatMessage msg : saved) {
                            String content = msg.isUser() ? msg.getText() : cleanMarkdown(msg.getText());
                            addBubble(content, msg.isUser(), false);
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void saveExchangeAsync(String question, String answer) {
        runAsync(() -> {
            ChatHistoryDAO dao = getDao();
            if (dao != null) dao.saveExchange(question, answer);
        }, "chat-save");
    }

    private void runAsync(Runnable task, String threadName) {
        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    private List<ChatMessage> recentHistory() {
        int startIndex = Math.max(0, history.size() - MAX_HISTORY_MESSAGES);
        List<ChatMessage> window = new ArrayList<>(history.subList(startIndex, history.size()));
        while (!window.isEmpty() && !window.get(0).isUser()) {
            window.remove(0);
        }
        return window;
    }

    private void setBusy(boolean isBusy) {
        this.busy = isBusy;
        sendButton.setEnabled(!isBusy);
        inputArea.setEnabled(!isBusy);
    }

    private void addBubble(String text, boolean isUser, boolean isError) {
        Bubble bubble = new Bubble(text, isUser, isError);
        messagesPanel.add(new Row(bubble, isUser));
        refreshMessages();
        scrollToBottom();
    }

    private void refreshMessages() {
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    private void showTyping() {
        if (typingTimer != null && typingTimer.isRunning()) {
            typingTimer.stop();
        }
        typingDots = new TypingDots();
        typingRow = new Row(typingDots, false);
        messagesPanel.add(typingRow);
        refreshMessages();
        scrollToBottom();

        typingTimer = new Timer(TYPING_TICK_MS, e -> {
            if (typingDots != null) typingDots.tick();
        });
        typingTimer.start();
    }

    private void hideTyping() {
        if (typingTimer != null) {
            typingTimer.stop();
            typingTimer = null;
        }
        if (typingRow != null) {
            messagesPanel.remove(typingRow);
            typingRow = null;
            typingDots = null;
            refreshMessages();
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            verticalBar.setValue(verticalBar.getMaximum());
        });
    }

    static String cleanMarkdown(String raw) {
        if (raw == null) return "";
        String cleaned = raw.replace("\r", "");
        cleaned = cleaned.replaceAll("(?m)^\\s*#{1,6}\\s*", "");
        cleaned = cleaned.replaceAll("\\*\\*(.+?)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("(?m)^\\s*[*\\-]\\s+", "\u2022 ");
        cleaned = cleaned.replaceAll("(?<![\\w*])\\*(\\S[^*\\n]*?)\\*(?![\\w*])", "$1");
        cleaned = cleaned.replaceAll("`([^`\\n]*)`", "$1");
        return cleaned.trim();
    }

    private static void enableAntialiasing(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    private static void paintSoftCard(Graphics2D g, int width, int height, Color fill, Color border) {
        g.setColor(new Color(0, 0, 0, 10));
        g.fill(new RoundRectangle2D.Double(1, 3, width - 2, height - 3, 22, 22));
        g.setColor(fill);
        g.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 3, 22, 22));
        g.setColor(border);
        g.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 2, height - 4, 22, 22));
    }

    private static class Surface extends JPanel {
        Surface() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            enableAntialiasing(g);
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
            return createEmptyButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createEmptyButton();
        }

        private JButton createEmptyButton() {
            JButton button = new JButton();
            Dimension zero = new Dimension(0, 0);
            button.setPreferredSize(zero);
            button.setMinimumSize(zero);
            button.setMaximumSize(zero);
            return button;
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            // Transparent track
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
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
            enableAntialiasing(g);
            int w = getWidth();
            int h = getHeight();
            Color statusColor = ok ? UITheme.SUCCESS : UITheme.DANGER;

            g.setColor(withAlpha(statusColor, 26));
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
            g.setColor(withAlpha(statusColor, 90));
            g.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 2, h - 2, h, h));

            g.setColor(statusColor);
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
            configureFlatButton(new Dimension(126, 38));
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

        private void configureFlatButton(Dimension size) {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(size);
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            enableAntialiasing(g);
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
            enableAntialiasing(g);
            int w = getWidth();
            int h = getHeight();

            g.setColor(hover ? UITheme.PRIMARY : new Color(236, 247, 242));
            g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, h, h));
            g.setColor(withAlpha(UITheme.PRIMARY, hover ? 255 : 90));
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

        void setFocused(boolean focused) {
            this.focused = focused;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            enableAntialiasing(g);
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
            enableAntialiasing(g);
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
            enableAntialiasing(g);
            int w = getWidth();
            int h = getHeight();

            if (user) {
                g.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, 0, h, UITheme.PRIMARY_DARK));
                g.fill(new Ellipse2D.Double(0, 0, w - 1, h - 1));
                String name = Session.getUsername();
                String initial = (name == null || name.isBlank()) ? "U" : name.trim().substring(0, 1).toUpperCase();
                g.setFont(new Font(UIKit.FAMILY, Font.BOLD, 15));
                FontMetrics fm = g.getFontMetrics();
                g.setColor(Color.WHITE);
                g.drawString(initial, (w - fm.stringWidth(initial)) / 2, (h - fm.getHeight()) / 2 + fm.getAscent());
            } else {
                g.setPaint(new GradientPaint(0, 0, new Color(240, 204, 120), 0, h, UITheme.ACCENT));
                g.fill(new Ellipse2D.Double(0, 0, w - 1, h - 1));
                UITheme.Icons.paint(g, "crop", (w - 20) / 2, (h - 20) / 2, 20, UITheme.PRIMARY_DARK);
            }
            g.dispose();
        }
    }

    private static class Row extends JPanel {
        Row(JComponent bubble, boolean isUser) {
            super(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(6, 8, 6, 8));

            JPanel avatarWrap = new JPanel(new BorderLayout());
            avatarWrap.setOpaque(false);
            avatarWrap.add(new AvatarDot(isUser), BorderLayout.NORTH);

            JPanel holder = new JPanel(new BorderLayout(10, 0));
            holder.setOpaque(false);
            holder.add(bubble, BorderLayout.CENTER);

            if (isUser) {
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
            enableAntialiasing(g);
            int w = getWidth();
            int h = getHeight();
            paintSoftCard(g, w, h, Color.WHITE, UITheme.BORDER_COLOR);

            int cy = (h - 3) / 2;
            for (int i = 0; i < 3; i++) {
                boolean active = (i == phase);
                double diameter = active ? 10 : 7;
                int cx = 24 + i * 15;
                g.setColor(active ? UITheme.PRIMARY : new Color(190, 205, 197));
                g.fill(new Ellipse2D.Double(cx - diameter / 2, cy - diameter / 2, diameter, diameter));
            }
            g.dispose();
        }
    }

    private class Bubble extends JPanel {
        private final JTextArea area = new JTextArea();
        private final boolean isUser;
        private final boolean isError;

        Bubble(String text, boolean isUser, boolean isError) {
            super(new BorderLayout());
            this.isUser = isUser;
            this.isError = isError;
            setOpaque(false);

            area.setText(text);
            area.setEditable(false);
            area.setOpaque(false);
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
            area.setFont(UITheme.FONT_BODY);
            area.setForeground(isError ? ERROR_FG : (isUser ? Color.WHITE : UITheme.TEXT_DARK));
            area.setBorder(null);
            area.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

            setBorder(new EmptyBorder(12, 18, 15, 18));
            add(area, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            int viewportWidth = messagesPanel.getWidth() > 0 ? messagesPanel.getWidth() : 700;
            int maxOuter = Math.max(260, (int) (viewportWidth * 0.70) - 46);
            int padX = 36;
            int padY = 27;
            int maxInner = maxOuter - padX;

            FontMetrics fm = area.getFontMetrics(area.getFont());
            int naturalWidth = 40;
            for (String line : area.getText().split("\n", -1)) {
                naturalWidth = Math.max(naturalWidth, fm.stringWidth(line) + 6);
            }

            int width = Math.min(maxInner, naturalWidth);
            area.setSize(width, Short.MAX_VALUE);
            int height = area.getPreferredSize().height;
            return new Dimension(width + padX, height + padY);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            enableAntialiasing(g2);
            int w = getWidth();
            int h = getHeight();

            if (isError) {
                paintSoftCard(g2, w, h, ERROR_BG, new Color(252, 165, 165));
            } else if (isUser) {
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fill(new RoundRectangle2D.Double(1, 3, w - 2, h - 3, 22, 22));
                g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY, w, h, UITheme.PRIMARY_DARK));
                g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 3, 22, 22));
            } else {
                paintSoftCard(g2, w, h, Color.WHITE, UITheme.BORDER_COLOR);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}