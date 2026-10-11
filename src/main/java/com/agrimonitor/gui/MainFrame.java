package com.agrimonitor.gui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public MainFrame() {
        setTitle("AgriMonitor - Smart Agricultural Monitoring System");
        setSize(1100, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new FieldPanel(), "Field");
        mainPanel.add(new CropPanel(), "Crop");
        mainPanel.add(new IrrigationPanel(), "Irrigation");
        mainPanel.add(new PesticidePanel(), "Pesticide");
        mainPanel.add(new CostPanel(), "Cost");
        mainPanel.add(new SalesPanel(), "Sales");
        mainPanel.add(new ProfitLossPanel(), "ProfitLoss");
        mainPanel.add(new WeatherPanel(), "Weather");
        mainPanel.add(new ReportPanel(), "Report");

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new GridLayout(10, 1, 5, 5));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        sidePanel.setBackground(new Color(34, 87, 59));

        String[] menuItems = {
                "Field Management", "Crop Management", "Irrigation Management",
                "Pesticide Management", "Cost Management", "Sales Management",
                "Profit/Loss", "Weather Simulation", "Report Generation", "Exit"
        };

        String[] cardNames = {
                "Field", "Crop", "Irrigation", "Pesticide", "Cost",
                "Sales", "ProfitLoss", "Weather", "Report", "Exit"
        };

        for (int i = 0; i < menuItems.length; i++) {
            JButton button = new JButton(menuItems[i]);
            String cardName = cardNames[i];
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                if (cardName.equals("Exit")) {
                    System.exit(0);
                } else {
                    cardLayout.show(mainPanel, cardName);
                }
            });
            sidePanel.add(button);
        }

        JLabel titleLabel = new JLabel("AgriMonitor", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(34, 87, 59));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        setLayout(new BorderLayout());
        add(titleLabel, BorderLayout.NORTH);
        add(sidePanel, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "Field");
    }
}