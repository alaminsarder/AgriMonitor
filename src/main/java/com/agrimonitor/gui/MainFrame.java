package com.agrimonitor.gui;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.session.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFrame extends JFrame {

    private static final String[] MENU_ITEMS = {
        "Home",
        "Field Management",
        "Crop Management",
        "Irrigation Management",
        "Pesticide Log",
        "Cost Management",
        "Sales Management",
        "Profit / Loss",
        "Weather",
        "Season Guide",
        "AI Assistant",
        "Report Generation",
        "Profile"
    };

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel titleLabel;
    private List<UITheme.SidebarButton> sidebarButtons = new ArrayList<>();
    private Map<String, UITheme.SidebarButton> buttonsByName = new HashMap<>();
    private final JPopupMenu notifyPopup = new JPopupMenu();

    public MainFrame() {
        setTitle("AgriMonitor - Smart Agriculture Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BACKGROUND);

        rootPanel.add(createSidebar(), BorderLayout.WEST);
        rootPanel.add(createMainArea(), BorderLayout.CENTER);

        setContentPane(rootPanel);
    }

    private JPanel createSidebar() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(UITheme.SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(230, 0));

        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(UITheme.PRIMARY_DARK);
        logoPanel.setMaximumSize(new Dimension(230, 80));
        logoPanel.setPreferredSize(new Dimension(230, 80));
        logoPanel.setBorder(new EmptyBorder(14, 22, 14, 20));
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoIcon = new JLabel("AgriMonitor");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLabel = new JLabel("Farm Management System");
        subLabel.setFont(UITheme.FONT_SMALL);
        subLabel.setForeground(new Color(190, 220, 220));
        subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        logoPanel.add(logoIcon);
        logoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        logoPanel.add(subLabel);

        sidebarPanel.add(logoPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setOpaque(false);
        userPanel.setPreferredSize(new Dimension(230, 104));
        userPanel.setMaximumSize(new Dimension(230, 104));
        userPanel.setBorder(new EmptyBorder(6, 10, 6, 10));
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userPanel.add(new SidebarProfile(Session.getUsername(), () -> navigateTo("Profile")), BorderLayout.CENTER);

        sidebarPanel.add(userPanel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        for (String item : MENU_ITEMS) {
            UITheme.SidebarButton btn = UITheme.createSidebarButton(item);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setPreferredSize(new Dimension(230, 40));
            btn.setMaximumSize(new Dimension(230, 40));
            btn.addActionListener(e -> switchPanel(item, btn));
            sidebarPanel.add(btn);
            sidebarButtons.add(btn);
            buttonsByName.put(item, btn);
        }

        sidebarButtons.get(0).setSelected(true);

        sidebarPanel.add(Box.createVerticalGlue());

        UITheme.SidebarButton logoutBtn = UITheme.createSidebarButton("Logout");
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setPreferredSize(new Dimension(230, 40));
        logoutBtn.setMaximumSize(new Dimension(230, 40));
        logoutBtn.addActionListener(e -> handleLogout());
        sidebarPanel.add(logoutBtn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        return sidebarPanel;
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UITheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 30, 14, 30));
        header.setPreferredSize(new Dimension(0, 70));

        titleLabel = new JLabel("Home");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        header.add(titleLabel, BorderLayout.WEST);

        UIKit.SearchField searchField = new UIKit.SearchField("Search...");
        searchField.addActionListener(e -> runSearch(searchField.getText()));

        JComponent bell = UITheme.createBellButton();
        bell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bell.setToolTipText("Notifications");
        bell.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                if (notifyPopup.isVisible()) {
                    notifyPopup.setVisible(false);
                } else {
                    showNotifications(bell);
                }
            }
        });

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        tools.setOpaque(false);
        tools.add(searchField);
        tools.add(bell);
        header.add(tools, BorderLayout.EAST);

        JSeparator separator = new JSeparator();
        separator.setForeground(UITheme.BORDER_COLOR);

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(header, BorderLayout.CENTER);
        headerWrapper.add(separator, BorderLayout.SOUTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(new HomePanel(this::navigateTo), "Home");
        contentPanel.add(new FieldPanel(), "Field Management");
        contentPanel.add(new CropPanel(), "Crop Management");
        contentPanel.add(new IrrigationPanel(), "Irrigation Management");
        contentPanel.add(new PesticidePanel(), "Pesticide Log");
        contentPanel.add(new CostPanel(), "Cost Management");
        contentPanel.add(new SalesPanel(), "Sales Management");
        contentPanel.add(new ProfitLossPanel(), "Profit / Loss");
        contentPanel.add(new WeatherPanel(), "Weather");
        contentPanel.add(new SeasonGuidePanel(), "Season Guide");
        contentPanel.add(new ChatPanel(), "AI Assistant");
        contentPanel.add(new ReportPanel(), "Report Generation");
        contentPanel.add(new ProfilePanel(), "Profile");

        mainArea.add(headerWrapper, BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        return mainArea;
    }

    private void showNotifications(final JComponent bell) {
        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() {
                List<String[]> items = new ArrayList<>();
                int fields = new FieldDAO().getAllFields().size();
                int crops = new CropDAO().getAllCrops().size();

                if (fields == 0) {
                    items.add(new String[]{"No fields added yet", "Add your first field to get started", "Field Management"});
                } else {
                    items.add(new String[]{fields + (fields == 1 ? " field registered" : " fields registered"),
                            "Open Field Management", "Field Management"});
                }
                if (crops == 0) {
                    items.add(new String[]{"No crops added yet", "Add a crop to track profit and cost", "Crop Management"});
                } else {
                    items.add(new String[]{crops + (crops == 1 ? " crop being tracked" : " crops being tracked"),
                            "Open Crop Management", "Crop Management"});
                }
                items.add(new String[]{"Check the weather", "Plan irrigation and spraying", "Weather"});
                return items;
            }

            @Override
            protected void done() {
                List<String[]> items;
                try {
                    items = get();
                } catch (Exception ex) {
                    items = new ArrayList<>();
                    items.add(new String[]{"Could not load notifications", "Check your connection and try again", "Home"});
                }
                buildPopup(items);
                notifyPopup.show(bell, bell.getWidth() - 340, bell.getHeight() + 8);
            }
        }.execute();
    }

    private void buildPopup(List<String[]> items) {
        notifyPopup.removeAll();
        notifyPopup.setBorder(new LineBorder(UITheme.BORDER_COLOR));
        notifyPopup.setBackground(Color.WHITE);

        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(Color.WHITE);
        box.setPreferredSize(new Dimension(340, 56 + items.size() * 62));

        JLabel head = new JLabel("Notifications");
        head.setFont(new Font("Segoe UI", Font.BOLD, 15));
        head.setForeground(UITheme.PRIMARY_DARK);
        head.setBorder(new EmptyBorder(14, 18, 12, 18));
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        box.add(head);

        for (String[] it : items) {
            final String target = it[2];
            NotifRow row = new NotifRow(it[0], it[1], () -> {
                notifyPopup.setVisible(false);
                navigateTo(target);
            });
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            box.add(row);
        }

        notifyPopup.add(box);
    }

    private void runSearch(String text) {
        if (text == null) return;
        String q = text.trim().toLowerCase();
        if (q.isEmpty()) return;
        for (String item : MENU_ITEMS) {
            if (item.toLowerCase().contains(q)) {
                navigateTo(item);
                return;
            }
        }
        Toolkit.getDefaultToolkit().beep();
    }

    private void navigateTo(String name) {
        UITheme.SidebarButton btn = buttonsByName.get(name);
        if (btn != null) {
            switchPanel(name, btn);
        }
    }

    private void switchPanel(String name, UITheme.SidebarButton clicked) {
        titleLabel.setText(name);
        cardLayout.show(contentPanel, name);
        for (UITheme.SidebarButton btn : sidebarButtons) {
            btn.setSelected(btn == clicked);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Session.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    private static class NotifRow extends JPanel {
        private boolean hover = false;

        NotifRow(String title, String sub, Runnable onClick) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
            setBorder(new EmptyBorder(11, 38, 11, 16));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
            setPreferredSize(new Dimension(340, 62));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.BOLD, 13));
            t.setForeground(UITheme.TEXT_DARK);

            JLabel s = new JLabel(sub);
            s.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            s.setForeground(UITheme.TEXT_LIGHT);

            add(t);
            add(Box.createVerticalStrut(3));
            add(s);

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
                    onClick.run();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (hover) {
                g.setColor(new Color(236, 247, 242));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
            g.setColor(UITheme.ACCENT);
            g.fill(new Ellipse2D.Double(18, getHeight() / 2.0 - 4, 8, 8));
            g.dispose();
            super.paintComponent(g0);
        }
    }
}