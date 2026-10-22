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
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadFields();
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Field Information"), BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(1, 4, 15, 12));
        fieldsGrid.setBackground(UITheme.CARD_BG);

        fieldIdField = UITheme.createTextField();
        fieldNameField = UITheme.createTextField();
        locationField = UITheme.createTextField();
        areaSizeField = UITheme.createTextField();

        fieldsGrid.add(UITheme.labeledField("Field ID", fieldIdField));
        fieldsGrid.add(UITheme.labeledField("Field Name", fieldNameField));
        fieldsGrid.add(UITheme.labeledField("Location", locationField));
        fieldsGrid.add(UITheme.labeledField("Area Size (acres)", areaSizeField));

        card.add(fieldsGrid, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(UITheme.CARD_BG);

        JButton addButton = UITheme.createStyledButton("Add Field", UITheme.PRIMARY);
        JButton updateButton = UITheme.createStyledButton("Update Field", UITheme.INFO);
        JButton deleteButton = UITheme.createStyledButton("Delete Field", UITheme.DANGER);
        JButton refreshButton = UITheme.createStyledButton("Refresh", UITheme.NEUTRAL);
        JButton clearButton = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        card.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addField());
        updateButton.addActionListener(e -> updateField());
        deleteButton.addActionListener(e -> deleteField());
        refreshButton.addActionListener(e -> loadFields());
        clearButton.addActionListener(e -> clearForm());

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("All Fields"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Field ID", "Field Name", "Location", "Area Size"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);

        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                fieldIdField.setText(tableModel.getValueAt(row, 0).toString());
                fieldNameField.setText(tableModel.getValueAt(row, 1).toString());
                locationField.setText(tableModel.getValueAt(row, 2).toString());
                areaSizeField.setText(tableModel.getValueAt(row, 3).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
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