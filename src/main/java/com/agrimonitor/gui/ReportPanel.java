package com.agrimonitor.gui;

import com.agrimonitor.service.ReportService;

import javax.swing.*;
import java.awt.*;

public class ReportPanel extends JPanel {

    private ReportService reportService;
    private JTextArea textArea;

    public ReportPanel() {
        reportService = new ReportService();
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        JPanel buttonCard = UITheme.createCardPanel();
        buttonCard.setLayout(new BorderLayout());
        buttonCard.add(UITheme.createCardTitle("Report Generation"), BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        buttonPanel.setBackground(UITheme.CARD_BG);

        JButton fieldReportButton = UITheme.createStyledButton("Field Report", UITheme.PRIMARY);
        JButton cropReportButton = UITheme.createStyledButton("Crop Report", UITheme.PRIMARY);
        JButton irrigationReportButton = UITheme.createStyledButton("Irrigation Report", UITheme.PRIMARY);
        JButton pesticideReportButton = UITheme.createStyledButton("Pesticide Report", UITheme.PRIMARY);
        JButton costReportButton = UITheme.createStyledButton("Cost Report", UITheme.INFO);
        JButton salesReportButton = UITheme.createStyledButton("Sales Report", UITheme.INFO);
        JButton profitLossReportButton = UITheme.createStyledButton("Profit/Loss Report", UITheme.SUCCESS);
        JButton overallReportButton = UITheme.createStyledButton("Overall Farm Report", UITheme.ACCENT);

        buttonPanel.add(fieldReportButton);
        buttonPanel.add(cropReportButton);
        buttonPanel.add(irrigationReportButton);
        buttonPanel.add(pesticideReportButton);
        buttonPanel.add(costReportButton);
        buttonPanel.add(salesReportButton);
        buttonPanel.add(profitLossReportButton);
        buttonPanel.add(overallReportButton);

        buttonCard.add(buttonPanel, BorderLayout.CENTER);

        JPanel outputCard = UITheme.createCardPanel();
        outputCard.setLayout(new BorderLayout());
        outputCard.add(UITheme.createCardTitle("Report Output"), BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        outputCard.add(scrollPane, BorderLayout.CENTER);

        add(buttonCard, BorderLayout.NORTH);
        add(outputCard, BorderLayout.CENTER);

        fieldReportButton.addActionListener(e -> textArea.setText(reportService.getFieldReportText()));
        cropReportButton.addActionListener(e -> textArea.setText(reportService.getCropReportText()));
        irrigationReportButton.addActionListener(e -> textArea.setText(reportService.getIrrigationReportText()));
        pesticideReportButton.addActionListener(e -> textArea.setText(reportService.getPesticideReportText()));
        costReportButton.addActionListener(e -> textArea.setText(reportService.getCostReportText()));
        salesReportButton.addActionListener(e -> textArea.setText(reportService.getSalesReportText()));
        profitLossReportButton.addActionListener(e -> textArea.setText(reportService.getProfitLossReportText()));
        overallReportButton.addActionListener(e -> textArea.setText(reportService.getOverallFarmReportText()));
    }
}