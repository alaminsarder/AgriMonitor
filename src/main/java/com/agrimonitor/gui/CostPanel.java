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
    private JTextField cropIdField, fieldIdField, amountField, dateField, searchCropIdField;
    private JComboBox<String> descriptionBox;
    private JLabel totalLabel;
    private DefaultTableModel tableModel;
    private JTable table;

    public CostPanel() {
        costDAO = new CostDAO();
        cropDAO = new CropDAO();
        fieldDAO = new FieldDAO();
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadAllCosts();
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Add Cost Record"), BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(2, 3, 15, 12));
        fieldsGrid.setBackground(UITheme.CARD_BG);

        cropIdField = UITheme.createTextField();
        fieldIdField = UITheme.createTextField();

        String[] categories = {"Seeds", "Fertilizer", "Labor", "Irrigation", "Pesticide", "Transportation", "Other"};
        descriptionBox = new JComboBox<>(categories);
        descriptionBox.setFont(UITheme.FONT_BODY);

        amountField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        searchCropIdField = UITheme.createTextField();

        fieldsGrid.add(UITheme.labeledField("Crop ID", cropIdField));
        fieldsGrid.add(UITheme.labeledField("Field ID", fieldIdField));
        fieldsGrid.add(UITheme.labeledField("Cost Category", descriptionBox));
        fieldsGrid.add(UITheme.labeledField("Amount (BDT)", amountField));
        fieldsGrid.add(UITheme.labeledField("Date (YYYY-MM-DD)", dateField));
        fieldsGrid.add(UITheme.labeledField("Check Total Cost for Crop ID", searchCropIdField));

        card.add(fieldsGrid, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBackground(UITheme.CARD_BG);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(UITheme.CARD_BG);

        JButton addButton = UITheme.createStyledButton("Add Cost", UITheme.PRIMARY);
        JButton refreshButton = UITheme.createStyledButton("View All", UITheme.NEUTRAL);
        JButton searchButton = UITheme.createStyledButton("Get Total", UITheme.INFO);
        JButton clearButton = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        totalLabel = new JLabel(" ");
        totalLabel.setFont(UITheme.FONT_SUBHEADER);
        totalLabel.setForeground(UITheme.SUCCESS);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        bottomPanel.add(buttonPanel);
        bottomPanel.add(totalLabel);

        card.add(bottomPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCost());
        refreshButton.addActionListener(e -> loadAllCosts());
        searchButton.addActionListener(e -> getTotalCost());
        clearButton.addActionListener(e -> clearForm());

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("All Cost Records"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Crop ID", "Field ID", "Category", "Amount (BDT)", "Date"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void addCost() {
        try {
            String cropId = cropIdField.getText().trim();
            String fieldId = fieldIdField.getText().trim();
            String description = (String) descriptionBox.getSelectedItem();
            double amount = Double.parseDouble(amountField.getText().trim());
            String date = dateField.getText().trim();

            if (cropId.isEmpty() || fieldId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Crop ID and Field ID are required.");
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
                JOptionPane.showMessageDialog(this, "Cost added successfully.");
                clearForm();
                loadAllCosts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add cost.");
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
        totalLabel.setText("Total Cost for " + cropId + ": " + total + " BDT");
    }

    private void clearForm() {
        cropIdField.setText("");
        fieldIdField.setText("");
        amountField.setText("");
        dateField.setText("");
        searchCropIdField.setText("");
        totalLabel.setText(" ");
    }
}