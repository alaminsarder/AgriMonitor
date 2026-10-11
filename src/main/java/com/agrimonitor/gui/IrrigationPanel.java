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
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Irrigation Information"));

        formPanel.add(new JLabel("Field ID:"));
        fieldIdField = new JTextField();
        formPanel.add(fieldIdField);

        formPanel.add(new JLabel("Irrigation Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        formPanel.add(dateField);

        formPanel.add(new JLabel("Water Quantity (liters):"));
        waterQuantityField = new JTextField();
        formPanel.add(waterQuantityField);

        formPanel.add(new JLabel("Search by Field ID:"));
        searchFieldIdField = new JTextField();
        formPanel.add(searchFieldIdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Irrigation Record");
        JButton refreshButton = new JButton("View All");
        JButton searchButton = new JButton("Search by Field");
        JButton clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        tableModel = new DefaultTableModel(new Object[]{"Field ID", "Date", "Water Quantity"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addIrrigation());
        refreshButton.addActionListener(e -> loadAllIrrigation());
        searchButton.addActionListener(e -> searchByField());
        clearButton.addActionListener(e -> clearForm());

        loadAllIrrigation();
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