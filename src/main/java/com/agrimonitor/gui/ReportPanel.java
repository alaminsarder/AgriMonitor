package com.agrimonitor.gui;

import com.agrimonitor.service.ReportService;

import javax.swing.*;
import java.awt.*;

public class ReportPanel extends JPanel {

    private ReportService reportService;
    private JTextArea textArea;

    public ReportPanel() {
        reportService = new ReportService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel buttonPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Report Generation"));

        JButton fieldReportButton = new JButton("Field Report");
        JButton cropReportButton = new JButton("Crop Report");
        JButton irrigationReportButton = new JButton("Irrigation Report");
        JButton pesticideReportButton = new JButton("Pesticide Report");
        JButton costReportButton = new JButton("Cost Report");
        JButton salesReportButton = new JButton("Sales Report");
        JButton profitLossReportButton = new JButton("Profit/Loss Report");
        JButton overallReportButton = new JButton("Overall Farm Report");

        buttonPanel.add(fieldReportButton);
        buttonPanel.add(cropReportButton);
        buttonPanel.add(irrigationReportButton);
        buttonPanel.add(pesticideReportButton);
        buttonPanel.add(costReportButton);
        buttonPanel.add(salesReportButton);
        buttonPanel.add(profitLossReportButton);
        buttonPanel.add(overallReportButton);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);

        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

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