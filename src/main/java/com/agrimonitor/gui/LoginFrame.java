package com.agrimonitor.gui;

import com.agrimonitor.dao.UserDAO;
import com.agrimonitor.model.User;
import com.agrimonitor.session.Session;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField registerConfirmField;

    private UserDAO userDAO;

    private static class GradientPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_DARK, getWidth(), getHeight(), UITheme.SIDEBAR_BG);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public LoginFrame() {
        userDAO = new UserDAO();

        setTitle("AgriMonitor - Login");
        setSize(520, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GradientPanel root = new GradientPanel();
        root.setLayout(new GridBagLayout());
        setContentPane(root);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(buildLoginPanel(), "LOGIN");
        cardPanel.add(buildRegisterPanel(), "REGISTER");

        root.add(cardPanel);

        cardLayout.show(cardPanel, "LOGIN");
    }

    private JPanel wrapCard(int height) {
        JPanel wrapper = UITheme.createCardPanel();
        wrapper.setPreferredSize(new Dimension(400, height));
        return wrapper;
    }

    private JLabel buildLogo() {
        JLabel logo = new JLabel("\uD83C\uDF3E", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        return logo;
    }

    private JPanel buildLoginPanel() {
        JPanel wrapper = wrapCard(480);

        JLabel logo = buildLogo();

        JLabel title = new JLabel("AgriMonitor");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Welcome back, please login");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(UITheme.TEXT_LIGHT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginUsernameField = UITheme.createTextField();
        loginPasswordField = UITheme.createPasswordField();

        JPanel usernameField = UITheme.labeledField("Username", loginUsernameField);
        JPanel passwordField = UITheme.labeledField("Password", loginPasswordField);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(340, 70));
        passwordField.setMaximumSize(new Dimension(340, 70));

        JButton loginButton = UITheme.createStyledButton("Login", UITheme.PRIMARY);
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(340, 44));
        loginButton.addActionListener(e -> handleLogin());

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(340, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton goRegister = new JButton("Don't have an account? Register");
        goRegister.setContentAreaFilled(false);
        goRegister.setBorderPainted(false);
        goRegister.setFocusPainted(false);
        goRegister.setForeground(UITheme.INFO);
        goRegister.setFont(new Font("Segoe UI", Font.BOLD, 12));
        goRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        goRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        goRegister.addActionListener(e -> cardLayout.show(cardPanel, "REGISTER"));

        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(logo);
        wrapper.add(Box.createRigidArea(new Dimension(0, 8)));
        wrapper.add(title);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitle);
        wrapper.add(Box.createRigidArea(new Dimension(0, 28)));
        wrapper.add(usernameField);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(passwordField);
        wrapper.add(Box.createRigidArea(new Dimension(0, 26)));
        wrapper.add(loginButton);
        wrapper.add(Box.createRigidArea(new Dimension(0, 20)));
        wrapper.add(sep);
        wrapper.add(Box.createRigidArea(new Dimension(0, 16)));
        wrapper.add(goRegister);

        getRootPane().setDefaultButton(loginButton);

        return wrapper;
    }

    private JPanel buildRegisterPanel() {
        JPanel wrapper = wrapCard(560);

        JLabel logo = buildLogo();

        JLabel title = new JLabel("Create Account");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Register to start using AgriMonitor");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(UITheme.TEXT_LIGHT);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        registerUsernameField = UITheme.createTextField();
        registerPasswordField = UITheme.createPasswordField();
        registerConfirmField = UITheme.createPasswordField();

        JPanel usernameField = UITheme.labeledField("Username", registerUsernameField);
        JPanel passwordField = UITheme.labeledField("Password", registerPasswordField);
        JPanel confirmField = UITheme.labeledField("Confirm Password", registerConfirmField);
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(340, 70));
        passwordField.setMaximumSize(new Dimension(340, 70));
        confirmField.setMaximumSize(new Dimension(340, 70));

        JButton registerButton = UITheme.createStyledButton("Create Account", UITheme.PRIMARY);
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(340, 44));
        registerButton.addActionListener(e -> handleRegister());

        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(340, 1));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton goLogin = new JButton("Already have an account? Login");
        goLogin.setContentAreaFilled(false);
        goLogin.setBorderPainted(false);
        goLogin.setFocusPainted(false);
        goLogin.setForeground(UITheme.INFO);
        goLogin.setFont(new Font("Segoe UI", Font.BOLD, 12));
        goLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        goLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        goLogin.addActionListener(e -> cardLayout.show(cardPanel, "LOGIN"));

        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(logo);
        wrapper.add(Box.createRigidArea(new Dimension(0, 8)));
        wrapper.add(title);
        wrapper.add(Box.createRigidArea(new Dimension(0, 6)));
        wrapper.add(subtitle);
        wrapper.add(Box.createRigidArea(new Dimension(0, 24)));
        wrapper.add(usernameField);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));
        wrapper.add(passwordField);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));
        wrapper.add(confirmField);
        wrapper.add(Box.createRigidArea(new Dimension(0, 24)));
        wrapper.add(registerButton);
        wrapper.add(Box.createRigidArea(new Dimension(0, 18)));
        wrapper.add(sep);
        wrapper.add(Box.createRigidArea(new Dimension(0, 14)));
        wrapper.add(goLogin);

        return wrapper;
    }

    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter username and password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User user = userDAO.loginUser(username, password);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = userDAO.registerUser(username, password);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Username already exists.", "Registration Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = userDAO.loginUser(username, password);
        Session.login(user.getId(), user.getUsername());
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}