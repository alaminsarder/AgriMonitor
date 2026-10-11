package com.agrimonitor.gui;

import com.agrimonitor.dao.CostDAO;
import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Cost;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CostPanel extends JPanel {

    private CostDAO costDAO;
    private CropDAO cropDAO;
    private FieldDAO fieldDAO;
    private JTextField cropIdField, fieldIdField, descriptionField, amountField, dateField, searchCropIdField;
    private JLabel totalLabel;
    private DefaultTableModel tableModel;
    private JTable table;

    public CostPanel() {
        costDAO = new CostDAO();
        cropDAO = new CropDAO();
        fieldDAO = new FieldDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Cost Information"));

        formPanel.add(new JLabel("Crop ID:"));
        cropIdField = new JTextField();
        formPanel.add(cropIdField);

        formPanel.add(new JLabel("Field ID:"));
        fieldIdField = new JTextField();
        formPanel.add(fieldIdField);

        formPanel.add(new JLabel("Description (Seeds/Fertilizer/Labor...):"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        formPanel.add(new JLabel("Amount (BDT):"));
        amountField = new JTextField();
        formPanel.add(amountField);

        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        formPanel.add(dateField);

        formPanel.add(new JLabel("Search Total Cost by Crop ID:"));
        searchCropIdField = new JTextField();
        formPanel.add(searchCropIdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Cost Record");
        JButton refreshButton = new JButton("View All");
        JButton searchButton = new JButton("Get Total Cost");
        JButton clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        totalLabel = new JLabel(" ");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(totalLabel);

        tableModel = new DefaultTableModel(new Object[]{"Crop ID", "Field ID", "Description", "Amount", "Date"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addCost());
        refreshButton.addActionListener(e -> loadAllCosts());
        searchButton.addActionListener(e -> getTotalCost());
        clearButton.addActionListener(e -> clearForm());

        loadAllCosts();
    }

    private void addCost() {
        try {
            String cropId = cropIdField.getText().trim();
            String fieldId = fieldIdField.getText().trim();
            String description = descriptionField.getText().trim();
            double amount = Double.parseDouble(amountField.getText().trim());
            String date = dateField.getText().trim();

            if (cropId.isEmpty() || fieldId.isEmpty() || description.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Crop ID, Field ID, and Description are required.");
                return;
            }

            if (!cropDAO.cropExists(cropId)) {
                JOptionPane.showMessageDialog(this, "Crop ID does not exist.");
                return;
            }

            if (!fieldDAO.fieldExists(fieldId)) {
                JOptionPane.showMessageDialog(this, "Field ID does not exist.");
                return;
            }

            Cost cost = new Cost(cropId, fieldId, description, amount, date);
            boolean success = costDAO.addCost(cost);

            if (success) {
                JOptionPane.showMessageDialog(this, "Cost record added successfully.");
                clearForm();
                loadAllCosts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add cost record.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Amount.");
        }
    }

    private void loadAllCosts() {
        tableModel.setRowCount(0);
        List<Cost> list = costDAO.getAllCosts();
        for (Cost cost : list) {
            tableModel.addRow(new Object[]{
                    cost.getCropId(), cost.getFieldId(), cost.getDescription(),
                    cost.getAmount(), cost.getDate()
            });
        }
    }

    private void getTotalCost() {
        String cropId = searchCropIdField.getText().trim();
        if (cropId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Crop ID.");
            return;
        }
        double total = costDAO.getTotalCostByCropId(cropId);
        totalLabel.setText("Total Cost for Crop " + cropId + ": " + total + " BDT");
    }

    private void clearForm() {
        cropIdField.setText("");
        fieldIdField.setText("");
        descriptionField.setText("");
        amountField.setText("");
        dateField.setText("");
        searchCropIdField.setText("");
        totalLabel.setText(" ");
    }
}