package com.agrimonitor.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel titleLabel;
    private List<UITheme.SidebarButton> sidebarButtons = new ArrayList<>();

    public MainFrame() {
        setTitle("AgriMonitor - Smart Agriculture Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

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
        logoPanel.setMaximumSize(new Dimension(230, 90));
        logoPanel.setPreferredSize(new Dimension(230, 90));
        logoPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel logoIcon = new JLabel("\uD83C\uDF3E AgriMonitor");
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
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] menuItems = {
            "Field Management",
            "Crop Management",
            "Irrigation Management",
            "Pesticide Log",
            "Cost Management",
            "Sales Management",
            "Profit / Loss",
            "Weather",
            "Report Generation"
        };

        for (String item : menuItems) {
            UITheme.SidebarButton btn = UITheme.createSidebarButton(item);
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setMaximumSize(new Dimension(230, 48));
            btn.addActionListener(e -> switchPanel(item, btn));
            sidebarPanel.add(btn);
            sidebarButtons.add(btn);
        }

        sidebarButtons.get(0).setSelected(true);

        sidebarPanel.add(Box.createVerticalGlue());

        UITheme.SidebarButton exitBtn = UITheme.createSidebarButton("\u23FB  Exit");
        exitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        exitBtn.setMaximumSize(new Dimension(230, 48));
        exitBtn.addActionListener(e -> System.exit(0));
        sidebarPanel.add(exitBtn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        return sidebarPanel;
    }

    private JPanel createMainArea() {
        JPanel mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(UITheme.BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        header.setPreferredSize(new Dimension(0, 70));

        titleLabel = new JLabel("Field Management");
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        header.add(titleLabel, BorderLayout.WEST);

        JSeparator separator = new JSeparator();
        separator.setForeground(UITheme.BORDER_COLOR);

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.add(header, BorderLayout.CENTER);
        headerWrapper.add(separator, BorderLayout.SOUTH);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        contentPanel.add(new FieldPanel(), "Field Management");
        contentPanel.add(new CropPanel(), "Crop Management");
        contentPanel.add(new IrrigationPanel(), "Irrigation Management");
        contentPanel.add(new PesticidePanel(), "Pesticide Log");
        contentPanel.add(new CostPanel(), "Cost Management");
        contentPanel.add(new SalesPanel(), "Sales Management");
        contentPanel.add(new ProfitLossPanel(), "Profit / Loss");
        contentPanel.add(new WeatherPanel(), "Weather");
        contentPanel.add(new ReportPanel(), "Report Generation");

        mainArea.add(headerWrapper, BorderLayout.NORTH);
        mainArea.add(contentPanel, BorderLayout.CENTER);

        return mainArea;
    }

    private void switchPanel(String name, UITheme.SidebarButton clicked) {
        titleLabel.setText(name);
        cardLayout.show(contentPanel, name);

        for (UITheme.SidebarButton btn : sidebarButtons) {
            btn.setSelected(btn == clicked);
        }
    }
}