package com.agrimonitor.gui;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Field;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FieldPanel extends JPanel {

    private FieldDAO fieldDAO;
    private JTextField fieldIdField, fieldNameField, locationField, areaSizeField;
    private DefaultTableModel tableModel;
    private JTable table;

    public FieldPanel() {
        fieldDAO = new FieldDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Field Information"));

        formPanel.add(new JLabel("Field ID:"));
        fieldIdField = new JTextField();
        formPanel.add(fieldIdField);

        formPanel.add(new JLabel("Field Name:"));
        fieldNameField = new JTextField();
        formPanel.add(fieldNameField);

        formPanel.add(new JLabel("Location:"));
        locationField = new JTextField();
        formPanel.add(locationField);

        formPanel.add(new JLabel("Area Size (acres):"));
        areaSizeField = new JTextField();
        formPanel.add(areaSizeField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Field");
        JButton updateButton = new JButton("Update Field");
        JButton deleteButton = new JButton("Delete Field");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        tableModel = new DefaultTableModel(new Object[]{"Field ID", "Field Name", "Location", "Area Size"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addField());
        updateButton.addActionListener(e -> updateField());
        deleteButton.addActionListener(e -> deleteField());
        refreshButton.addActionListener(e -> loadFields());
        clearButton.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                fieldIdField.setText(tableModel.getValueAt(row, 0).toString());
                fieldNameField.setText(tableModel.getValueAt(row, 1).toString());
                locationField.setText(tableModel.getValueAt(row, 2).toString());
                areaSizeField.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        loadFields();
    }

    private void addField() {
        try {
            String fieldId = fieldIdField.getText().trim();
            String fieldName = fieldNameField.getText().trim();
            String location = locationField.getText().trim();
            double areaSize = Double.parseDouble(areaSizeField.getText().trim());

            if (fieldId.isEmpty() || fieldName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Field ID and Name are required.");
                return;
            }

            if (fieldDAO.fieldExists(fieldId)) {
                JOptionPane.showMessageDialog(this, "Field ID already exists.");
                return;
            }

            Field field = new Field(fieldId, fieldName, location, areaSize);
            boolean success = fieldDAO.addField(field);

            if (success) {
                JOptionPane.showMessageDialog(this, "Field added successfully.");
                clearForm();
                loadFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add field.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Area Size.");
        }
    }

    private void updateField() {
        try {
            String fieldId = fieldIdField.getText().trim();
            String fieldName = fieldNameField.getText().trim();
            String location = locationField.getText().trim();
            double areaSize = Double.parseDouble(areaSizeField.getText().trim());

            if (!fieldDAO.fieldExists(fieldId)) {
                JOptionPane.showMessageDialog(this, "Field ID not found.");
                return;
            }

            boolean success = fieldDAO.updateField(fieldId, fieldName, location, areaSize);

            if (success) {
                JOptionPane.showMessageDialog(this, "Field updated successfully.");
                clearForm();
                loadFields();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update field.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Area Size.");
        }
    }

    private void deleteField() {
        String fieldId = fieldIdField.getText().trim();
        if (fieldId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a field to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this field?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = fieldDAO.deleteField(fieldId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Field deleted successfully.");
                clearForm();
                loadFields();
            } else {
                JOptionPane.showMessageDialog(this, "Field ID not found.");
            }
        }
    }

    private void loadFields() {
        tableModel.setRowCount(0);
        List<Field> fields = fieldDAO.getAllFields();
        for (Field field : fields) {
            tableModel.addRow(new Object[]{
                    field.getFieldId(), field.getFieldName(), field.getLocation(), field.getAreaSize()
            });
        }
    }

    private void clearForm() {
        fieldIdField.setText("");
        fieldNameField.setText("");
        locationField.setText("");
        areaSizeField.setText("");
        table.clearSelection();
    }
}