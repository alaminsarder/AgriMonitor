package com.agrimonitor.gui;

import com.agrimonitor.dao.UserDAO;
import com.agrimonitor.model.User;
import com.agrimonitor.session.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

public class LoginFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmField;

    private JButton loginButton;
    private JButton registerButton;

    private UserDAO userDAO;
    private FadePanel fadePanel;
    private boolean switching = false;

    private static final int FORM_WIDTH = 340;

    // Label that fades in and slides up
    private static class FadeLabel extends JLabel {
        float alpha = 0f;

        FadeLabel(String text, int align) {
            super(text, align);
        }

        public void paint(Graphics g) {
            if (alpha >= 1f) {
                super.paint(g);
                return;
            }
            if (alpha <= 0f) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(0, (1f - alpha) * 14);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            super.paint(g2);
            g2.dispose();
        }
    }

    // Panel whose whole content can fade (used for form transitions)
    private static class FadePanel extends JPanel {
        private float alpha = 1f;

        FadePanel(LayoutManager lm) {
            super(lm);
            setOpaque(false);
        }

        void setAlpha(float a) {
            alpha = Math.max(0f, Math.min(1f, a));
            repaint();
        }

        public void paint(Graphics g) {
            if (alpha >= 1f) {
                super.paint(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            super.paint(g2);
            g2.dispose();
        }
    }

    // Left branding side: drifting circles + staggered entrance
    private static class BrandPanel extends JPanel {
        private final java.util.List<FadeLabel> items = new java.util.ArrayList<>();
        private final Timer timer;
        private long startTime = -1;
        private double phase = 0;

        BrandPanel() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBorder(BorderFactory.createEmptyBorder(0, 44, 0, 36));
            setPreferredSize(new Dimension(400, 620));

            FadeLabel badge = new FadeLabel("\uD83C\uDF3E", SwingConstants.CENTER) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                    g2.setColor(new Color(255, 255, 255, 70));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 24, 24);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            badge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
            badge.setForeground(Color.WHITE);
            badge.setPreferredSize(new Dimension(68, 68));
            badge.setMaximumSize(new Dimension(68, 68));
            badge.setAlignmentX(Component.LEFT_ALIGNMENT);

            FadeLabel name = new FadeLabel("AgriMonitor", SwingConstants.LEFT);
            name.setFont(new Font("Segoe UI", Font.BOLD, 34));
            name.setForeground(Color.WHITE);
            name.setAlignmentX(Component.LEFT_ALIGNMENT);

            FadeLabel tagline = new FadeLabel("Smart Agricultural Monitoring System", SwingConstants.LEFT);
            tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            tagline.setForeground(UITheme.PRIMARY_LIGHT);
            tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel accentLine = new JPanel() {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.ACCENT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
            };
            accentLine.setOpaque(false);
            accentLine.setPreferredSize(new Dimension(48, 4));
            accentLine.setMaximumSize(new Dimension(48, 4));
            accentLine.setAlignmentX(Component.LEFT_ALIGNMENT);

            add(Box.createVerticalGlue());
            add(badge);
            add(Box.createRigidArea(new Dimension(0, 22)));
            add(name);
            add(Box.createRigidArea(new Dimension(0, 8)));
            add(tagline);
            add(Box.createRigidArea(new Dimension(0, 22)));
            add(accentLine);
            add(Box.createRigidArea(new Dimension(0, 30)));
            items.add(badge);
            items.add(name);
            items.add(tagline);

            String[] feats = { "Field and crop management", "Irrigation and pesticide tracking",
                    "Cost, sales and profit reports", "Live weather information" };
            for (int i = 0; i < feats.length; i++) {
                FadeLabel f = feature(feats[i]);
                items.add(f);
                add(f);
                if (i < feats.length - 1) {
                    add(Box.createRigidArea(new Dimension(0, 14)));
                }
            }
            add(Box.createVerticalGlue());

            timer = new Timer(33, e -> tick());
        }

        private FadeLabel feature(String text) {
            FadeLabel l = new FadeLabel("\u2713   " + text, SwingConstants.LEFT);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            l.setForeground(new Color(236, 253, 245));
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            return l;
        }

        private void tick() {
            long now = System.currentTimeMillis();
            if (startTime < 0) {
                startTime = now;
            }
            long elapsed = now - startTime;
            int[] delays = { 0, 120, 220, 380, 480, 580, 680 };
            for (int i = 0; i < items.size(); i++) {
                float t = (elapsed - delays[i]) / 450f;
                t = Math.max(0f, Math.min(1f, t));
                items.get(i).alpha = 1f - (1f - t) * (1f - t);
            }
            phase += 0.025;
            repaint();
        }

        public void addNotify() {
            super.addNotify();
            timer.start();
        }

        public void removeNotify() {
            timer.stop();
            super.removeNotify();
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, UITheme.PRIMARY_DARK, getWidth(), getHeight(), UITheme.SIDEBAR_BG));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int dx1 = (int) (Math.sin(phase) * 22);
            int dy1 = (int) (Math.cos(phase * 0.8) * 18);
            g2.setColor(new Color(255, 255, 255, 14));
            g2.fillOval(-120 + dx1, -120 + dy1, 340, 340);

            int dx2 = (int) (Math.cos(phase * 0.7) * 26);
            int dy2 = (int) (Math.sin(phase * 0.9) * 22);
            g2.setColor(new Color(16, 185, 129, 38));
            g2.fillOval(getWidth() - 170 + dx2, getHeight() - 210 + dy2, 320, 320);

            int dx3 = (int) (Math.sin(phase * 1.3) * 14);
            int dy3 = (int) (Math.cos(phase * 1.1) * 26);
            g2.setColor(new Color(245, 158, 11, 30));
            g2.fillOval(getWidth() - 90 + dx3, 60 + dy3, 120, 120);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public LoginFrame() {
        userDAO = new UserDAO();

        setTitle("AgriMonitor - Login");
        setSize(900, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Color.WHITE);

        cardPanel.add(buildLoginPanel(), "LOGIN");
        cardPanel.add(buildRegisterPanel(), "REGISTER");

        fadePanel = new FadePanel(new BorderLayout());
        fadePanel.add(cardPanel, BorderLayout.CENTER);
        fadePanel.setAlpha(0f);

        root.add(new BrandPanel(), BorderLayout.WEST);
        root.add(fadePanel, BorderLayout.CENTER);

        showCard("LOGIN");

        // form fades in shortly after the window opens
        Timer intro = new Timer(15, null);
        final int[] n = { 0 };
        intro.setInitialDelay(250);
        intro.addActionListener(e -> {
            n[0]++;
            float t = Math.min(1f, n[0] / 24f);
            fadePanel.setAlpha(1f - (1f - t) * (1f - t));
            if (t >= 1f) {
                ((Timer) e.getSource()).stop();
            }
        });
        intro.start();
    }

    // fade out -> switch card -> fade in
    private void switchTo(String name) {
        if (switching) {
            return;
        }
        switching = true;
        Timer t = new Timer(14, null);
        final int[] i = { 0 };
        t.addActionListener(e -> {
            i[0]++;
            if (i[0] < 10) {
                fadePanel.setAlpha(1f - i[0] / 10f);
            } else if (i[0] == 10) {
                fadePanel.setAlpha(0f);
                showCard(name);
            } else if (i[0] < 20) {
                fadePanel.setAlpha((i[0] - 10) / 10f);
            } else {
                fadePanel.setAlpha(1f);
                switching = false;
                ((Timer) e.getSource()).stop();
            }
        });
        t.start();
    }

    // window shake, then run the action (used for error messages)
    private void shake(Runnable after) {
        final Point origin = getLocation();
        final int[] offsets = { -12, 12, -9, 9, -6, 6, -3, 3, 0 };
        final int[] i = { 0 };
        Timer t = new Timer(28, null);
        t.addActionListener(e -> {
            if (i[0] >= offsets.length) {
                ((Timer) e.getSource()).stop();
                setLocation(origin);
                if (after != null) {
                    after.run();
                }
                return;
            }
            setLocation(origin.x + offsets[i[0]++], origin.y);
        });
        t.start();
    }

    private void showError(String message, String title, int type) {
        shake(() -> JOptionPane.showMessageDialog(this, message, title, type));
    }

    private void showCard(String name) {
        cardLayout.show(cardPanel, name);
        if ("LOGIN".equals(name)) {
            getRootPane().setDefaultButton(loginButton);
            setTitle("AgriMonitor - Login");
        } else {
            getRootPane().setDefaultButton(registerButton);
            setTitle("AgriMonitor - Register");
        }
    }

    private JPanel formContainer() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(0, 60, 0, 60));
        return p;
    }

    private JPanel centered(JPanel inner) {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);
        outer.add(inner);
        return outer;
    }

    private JLabel heading(String text) {
        JLabel title = new JLabel(text);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        return title;
    }

    private JLabel subheading(String text) {
        JLabel subtitle = new JLabel(text);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_LIGHT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        return subtitle;
    }

    private JPanel field(String label, JComponent input) {
        JPanel p = UITheme.labeledField(label, input);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(FORM_WIDTH, 74));
        p.setPreferredSize(new Dimension(FORM_WIDTH, 74));
        return p;
    }

    private JCheckBox showPasswordBox(JPasswordField... fields) {
        JCheckBox box = new JCheckBox("Show password");
        box.setOpaque(false);
        box.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        box.setForeground(UITheme.TEXT_LIGHT);
        box.setFocusPainted(false);
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
        char defaultEcho = fields[0].getEchoChar();
        box.addActionListener(e -> {
            for (JPasswordField f : fields) {
                f.setEchoChar(box.isSelected() ? (char) 0 : defaultEcho);
            }
        });
        return box;
    }

    private JButton primaryButton(String text) {
        JButton b = UITheme.createStyledButton(text, UITheme.PRIMARY);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setPreferredSize(new Dimension(FORM_WIDTH, 48));
        b.setMaximumSize(new Dimension(FORM_WIDTH, 48));
        return b;
    }

    private JButton linkButton(String text, String targetCard) {
        JButton b = new JButton(text);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setForeground(UITheme.PRIMARY);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> switchTo(targetCard));
        return b;
    }

    private JPanel switchRow(String question, String linkText, String targetCard) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(FORM_WIDTH, 30));
        JLabel q = new JLabel(question + " ");
        q.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        q.setForeground(UITheme.TEXT_LIGHT);
        row.add(q);
        row.add(linkButton(linkText, targetCard));
        return row;
    }

    private JPanel buildLoginPanel() {
        JPanel form = formContainer();

        loginUsernameField = UITheme.createTextField();
        loginPasswordField = UITheme.createPasswordField();

        loginButton = primaryButton("Sign In");
        loginButton.addActionListener(e -> handleLogin());

        form.add(heading("Welcome back"));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(subheading("Sign in to continue to your farm dashboard"));
        form.add(Box.createRigidArea(new Dimension(0, 34)));
        form.add(field("Username", loginUsernameField));
        form.add(Box.createRigidArea(new Dimension(0, 16)));
        form.add(field("Password", loginPasswordField));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(showPasswordBox(loginPasswordField));
        form.add(Box.createRigidArea(new Dimension(0, 26)));
        form.add(loginButton);
        form.add(Box.createRigidArea(new Dimension(0, 24)));
        form.add(switchRow("New to AgriMonitor?", "Create an account", "REGISTER"));

        return centered(form);
    }

    private JPanel buildRegisterPanel() {
        JPanel form = formContainer();

        registerUsernameField = UITheme.createTextField();
        registerPasswordField = UITheme.createPasswordField();
        registerConfirmField = UITheme.createPasswordField();

        registerButton = primaryButton("Create Account");
        registerButton.addActionListener(e -> handleRegister());

        form.add(heading("Create account"));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(subheading("Register to start managing your farm"));
        form.add(Box.createRigidArea(new Dimension(0, 28)));
        form.add(field("Username", registerUsernameField));
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(field("Password", registerPasswordField));
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        form.add(field("Confirm Password", registerConfirmField));
        form.add(Box.createRigidArea(new Dimension(0, 8)));
        form.add(showPasswordBox(registerPasswordField, registerConfirmField));
        form.add(Box.createRigidArea(new Dimension(0, 24)));
        form.add(registerButton);
        form.add(Box.createRigidArea(new Dimension(0, 22)));
        form.add(switchRow("Already have an account?", "Sign in", "LOGIN"));

        return centered(form);
    }

    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.loginUser(username, password);
        if (user == null) {
            showError("Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Session.login(user.getId(), user.getUsername());
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private void handleRegister() {
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirm = new String(registerConfirmField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = userDAO.registerUser(username, password);
        if (!success) {
            showError("Username already exists.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.loginUser(username, password);
        Session.login(user.getId(), user.getUsername());
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
