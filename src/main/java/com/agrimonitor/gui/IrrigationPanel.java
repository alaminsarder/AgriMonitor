package com.agrimonitor.gui;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.dao.IrrigationDAO;
import com.agrimonitor.model.Irrigation;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class IrrigationPanel extends JPanel {

    private IrrigationDAO irrigationDAO;
    private FieldDAO fieldDAO;
    private JTextField fieldIdField, dateField, waterQuantityField, searchFieldIdField;
    private DefaultTableModel tableModel;
    private JTable table;

    public IrrigationPanel() {
        irrigationDAO = new IrrigationDAO();
        fieldDAO = new FieldDAO();
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadAllIrrigation();
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Irrigation Information"), BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(1, 4, 15, 12));
        fieldsGrid.setBackground(UITheme.CARD_BG);

        fieldIdField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        waterQuantityField = UITheme.createTextField();
        searchFieldIdField = UITheme.createTextField();

        fieldsGrid.add(UITheme.labeledField("Field ID", fieldIdField));
        fieldsGrid.add(UITheme.labeledField("Date (YYYY-MM-DD)", dateField));
        fieldsGrid.add(UITheme.labeledField("Water Quantity (liters)", waterQuantityField));
        fieldsGrid.add(UITheme.labeledField("Search by Field ID", searchFieldIdField));

        card.add(fieldsGrid, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(UITheme.CARD_BG);

        JButton addButton = UITheme.createStyledButton("Add Irrigation Record", UITheme.PRIMARY);
        JButton refreshButton = UITheme.createStyledButton("View All", UITheme.NEUTRAL);
        JButton searchButton = UITheme.createStyledButton("Search by Field", UITheme.INFO);
        JButton clearButton = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        card.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addIrrigation());
        refreshButton.addActionListener(e -> loadAllIrrigation());
        searchButton.addActionListener(e -> searchByField());
        clearButton.addActionListener(e -> clearForm());

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Irrigation Records"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Field ID", "Date", "Water Quantity"}, 0) {
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

    private void addIrrigation() {
        try {
            String fieldId = fieldIdField.getText().trim();
            String date = dateField.getText().trim();
            double waterQuantity = Double.parseDouble(waterQuantityField.getText().trim());

            if (fieldId.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Field ID and Date are required.");
                return;
            }

            if (!fieldDAO.fieldExists(fieldId)) {
                JOptionPane.showMessageDialog(this, "Field ID does not exist.");
                return;
            }

            Irrigation irrigation = new Irrigation(fieldId, date, waterQuantity);
            boolean success = irrigationDAO.addIrrigation(irrigation);

            if (success) {
                JOptionPane.showMessageDialog(this, "Irrigation record added successfully.");
                clearForm();
                loadAllIrrigation();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add irrigation record.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Water Quantity.");
        }
    }

    private void loadAllIrrigation() {
        tableModel.setRowCount(0);
        List<Irrigation> list = irrigationDAO.getAllIrrigations();
        for (Irrigation irrigation : list) {
            tableModel.addRow(new Object[]{
                    irrigation.getFieldId(), irrigation.getIrrigationDate(), irrigation.getWaterQuantity()
            });
        }
    }

    private void searchByField() {
        String fieldId = searchFieldIdField.getText().trim();
        if (fieldId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Field ID to search.");
            return;
        }
        tableModel.setRowCount(0);
        List<Irrigation> list = irrigationDAO.getIrrigationByFieldId(fieldId);
        for (Irrigation irrigation : list) {
            tableModel.addRow(new Object[]{
                    irrigation.getFieldId(), irrigation.getIrrigationDate(), irrigation.getWaterQuantity()
            });
        }
    }

    private void clearForm() {
        fieldIdField.setText("");
        dateField.setText("");
        waterQuantityField.setText("");
        searchFieldIdField.setText("");
    }
}