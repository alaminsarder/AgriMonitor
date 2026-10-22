package com.agrimonitor.gui;

import com.agrimonitor.service.ProfitLossService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;

public class ProfitLossPanel extends JPanel {

    private ProfitLossService profitLossService;
    private DecimalFormat df;

    private JLabel costValueLabel;
    private JLabel salesValueLabel;
    private JLabel resultValueLabel;
    private JLabel resultTitleLabel;
    private JLabel marginValueLabel;

    private DefaultTableModel tableModel;
    private JTable table;

    public ProfitLossPanel() {
        profitLossService = new ProfitLossService();
        df = new DecimalFormat("#,##0.00");

        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        JPanel cardsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        cardsPanel.setBackground(UITheme.BACKGROUND);

        JPanel costCard = createCard("TOTAL COST", "0.00 BDT", UITheme.DANGER);
        costValueLabel = (JLabel) costCard.getClientProperty("valueLabel");

        JPanel salesCard = createCard("TOTAL SALES", "0.00 BDT", UITheme.INFO);
        salesValueLabel = (JLabel) salesCard.getClientProperty("valueLabel");

        JPanel resultCard = createCard("NET RESULT", "0.00 BDT", UITheme.SUCCESS);
        resultValueLabel = (JLabel) resultCard.getClientProperty("valueLabel");
        resultTitleLabel = (JLabel) resultCard.getClientProperty("titleLabel");

        JPanel marginCard = createCard("PROFIT MARGIN", "0.00 %", new Color(124, 58, 237));
        marginValueLabel = (JLabel) marginCard.getClientProperty("valueLabel");

        cardsPanel.add(costCard);
        cardsPanel.add(salesCard);
        cardsPanel.add(resultCard);
        cardsPanel.add(marginCard);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UITheme.BACKGROUND);

        JButton refreshButton = UITheme.createStyledButton("Refresh Dashboard", UITheme.PRIMARY);
        JPanel refreshWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshWrap.setBackground(UITheme.BACKGROUND);
        refreshWrap.add(refreshButton);

        topPanel.add(refreshWrap, BorderLayout.EAST);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setBackground(UITheme.BACKGROUND);
        northContainer.add(topPanel);
        northContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        northContainer.add(cardsPanel);

        JPanel tableCard = UITheme.createCardPanel();
        tableCard.setLayout(new BorderLayout());
        tableCard.add(UITheme.createCardTitle("Crop-wise Profit / Loss Breakdown"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Crop ID", "Crop Name", "Total Cost (BDT)", "Total Sales (BDT)", "Profit / Loss (BDT)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(northContainer, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> refreshDashboard());

        refreshDashboard();
    }

    private JPanel createCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.FONT_SMALL);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);

        card.putClientProperty("valueLabel", valueLabel);
        card.putClientProperty("titleLabel", titleLabel);

        return card;
    }

    private void refreshDashboard() {
        double totalCost = profitLossService.calculateTotalCost();
        double totalSales = profitLossService.calculateTotalSales();
        double result = totalSales - totalCost;
        double margin = profitLossService.calculateProfitMarginPercent();

        costValueLabel.setText(df.format(totalCost) + " BDT");
        salesValueLabel.setText(df.format(totalSales) + " BDT");
        marginValueLabel.setText(df.format(margin) + " %");

        if (result >= 0) {
            resultValueLabel.setText(df.format(result) + " BDT");
            resultTitleLabel.setText("NET PROFIT");
        } else {
            resultValueLabel.setText(df.format(Math.abs(result)) + " BDT");
            resultTitleLabel.setText("NET LOSS");
        }

        tableModel.setRowCount(0);
        for (Object[] row : profitLossService.getCropWiseProfitLoss()) {
            String cropId = (String) row[0];
            String cropName = (String) row[1];
            double cost = (double) row[2];
            double sales = (double) row[3];
            double profit = (double) row[4];

            tableModel.addRow(new Object[]{
                    cropId, cropName, df.format(cost), df.format(sales),
                    (profit >= 0 ? "+" : "") + df.format(profit)
            });
        }
    }
}