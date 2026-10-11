package com.agrimonitor.gui;

import com.agrimonitor.service.ProfitLossService;

import javax.swing.*;
import java.awt.*;

public class ProfitLossPanel extends JPanel {

    private ProfitLossService profitLossService;
    private JLabel totalCostLabel, totalSalesLabel, resultLabel;
    private JTextField cropIdField;
    private JLabel cropResultLabel;

    public ProfitLossPanel() {
        profitLossService = new ProfitLossService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createTitledBorder("Overall Profit / Loss"));

        JButton calculateButton = new JButton("Calculate Overall Profit/Loss");
        calculateButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        totalCostLabel = new JLabel("Total Cost: -");
        totalSalesLabel = new JLabel("Total Sales: -");
        resultLabel = new JLabel("Result: -");

        totalCostLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        totalSalesLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));

        totalCostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        totalSalesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(calculateButton);
        topPanel.add(Box.createVerticalStrut(15));
        topPanel.add(totalCostLabel);
        topPanel.add(totalSalesLabel);
        topPanel.add(resultLabel);
        topPanel.add(Box.createVerticalStrut(10));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createTitledBorder("Profit / Loss by Crop"));

        JPanel searchRow = new JPanel(new FlowLayout());
        searchRow.add(new JLabel("Crop ID:"));
        cropIdField = new JTextField(15);
        searchRow.add(cropIdField);
        JButton searchButton = new JButton("Calculate");
        searchRow.add(searchButton);

        cropResultLabel = new JLabel(" ");
        cropResultLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cropResultLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(searchRow);
        bottomPanel.add(Box.createVerticalStrut(10));
        bottomPanel.add(cropResultLabel);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.CENTER);

        calculateButton.addActionListener(e -> calculateOverall());
        searchButton.addActionListener(e -> calculateByCrop());
    }

    private void calculateOverall() {
        double totalCost = profitLossService.calculateTotalCost();
        double totalSales = profitLossService.calculateTotalSales();
        double result = totalSales - totalCost;

        totalCostLabel.setText("Total Cost: " + totalCost + " BDT");
        totalSalesLabel.setText("Total Sales: " + totalSales + " BDT");

        if (result >= 0) {
            resultLabel.setText("Profit: " + result + " BDT");
            resultLabel.setForeground(new Color(0, 128, 0));
        } else {
            resultLabel.setText("Loss: " + Math.abs(result) + " BDT");
            resultLabel.setForeground(Color.RED);
        }
    }

    private void calculateByCrop() {
        String cropId = cropIdField.getText().trim();
        if (cropId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Crop ID.");
            return;
        }

        double result = profitLossService.calculateProfitOrLossByCrop(cropId);

        if (result >= 0) {
            cropResultLabel.setText("Profit for " + cropId + ": " + result + " BDT");
            cropResultLabel.setForeground(new Color(0, 128, 0));
        } else {
            cropResultLabel.setText("Loss for " + cropId + ": " + Math.abs(result) + " BDT");
            cropResultLabel.setForeground(Color.RED);
        }
    }
}