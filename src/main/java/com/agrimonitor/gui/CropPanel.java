package com.agrimonitor.gui;

import com.agrimonitor.dao.CropDAO;
import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.model.Crop;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CropPanel extends JPanel {

    private CropDAO cropDAO;
    private FieldDAO fieldDAO;
    private JTextField cropIdField, cropNameField, cropTypeField, growthDurationField, fieldIdField;
    private DefaultTableModel tableModel;
    private JTable table;

    public CropPanel() {
        cropDAO = new CropDAO();
        fieldDAO = new FieldDAO();
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadCrops();
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Crop Information"), BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(1, 5, 15, 12));
        fieldsGrid.setBackground(UITheme.CARD_BG);

        cropIdField = UITheme.createTextField();
        cropNameField = UITheme.createTextField();
        cropTypeField = UITheme.createTextField();
        growthDurationField = UITheme.createTextField();
        fieldIdField = UITheme.createTextField();

        fieldsGrid.add(UITheme.labeledField("Crop ID", cropIdField));
        fieldsGrid.add(UITheme.labeledField("Crop Name", cropNameField));
        fieldsGrid.add(UITheme.labeledField("Crop Type", cropTypeField));
        fieldsGrid.add(UITheme.labeledField("Growth Duration (days)", growthDurationField));
        fieldsGrid.add(UITheme.labeledField("Field ID", fieldIdField));

        card.add(fieldsGrid, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(UITheme.CARD_BG);

        JButton addButton = UITheme.createStyledButton("Add Crop", UITheme.PRIMARY);
        JButton updateButton = UITheme.createStyledButton("Update Crop", UITheme.INFO);
        JButton refreshButton = UITheme.createStyledButton("Refresh", UITheme.NEUTRAL);
        JButton clearButton = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        card.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addCrop());
        updateButton.addActionListener(e -> updateCrop());
        refreshButton.addActionListener(e -> loadCrops());
        clearButton.addActionListener(e -> clearForm());

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("All Crops"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Crop ID", "Crop Name", "Crop Type", "Growth Duration", "Field ID"}, 0) {
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
                cropIdField.setText(tableModel.getValueAt(row, 0).toString());
                cropNameField.setText(tableModel.getValueAt(row, 1).toString());
                cropTypeField.setText(tableModel.getValueAt(row, 2).toString());
                growthDurationField.setText(tableModel.getValueAt(row, 3).toString());
                fieldIdField.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    private void addCrop() {
        try {
            String cropId = cropIdField.getText().trim();
            String cropName = cropNameField.getText().trim();
            String cropType = cropTypeField.getText().trim();
            int growthDuration = Integer.parseInt(growthDurationField.getText().trim());
            String fieldId = fieldIdField.getText().trim();

            if (cropId.isEmpty() || cropName.isEmpty() || fieldId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Crop ID, Name, and Field ID are required.");
                return;
            }

            if (cropDAO.cropExists(cropId)) {
                JOptionPane.showMessageDialog(this, "Crop ID already exists.");
                return;
            }

            if (!fieldDAO.fieldExists(fieldId)) {
                JOptionPane.showMessageDialog(this, "Field ID does not exist. Please add field first.");
                return;
            }

            Crop crop = new Crop(cropId, cropName, cropType, growthDuration, fieldId);
            boolean success = cropDAO.addCrop(crop);

            if (success) {
                JOptionPane.showMessageDialog(this, "Crop added successfully.");
                clearForm();
                loadCrops();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add crop.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Growth Duration.");
        }
    }

    private void updateCrop() {
        try {
            String cropId = cropIdField.getText().trim();
            String cropName = cropNameField.getText().trim();
            String cropType = cropTypeField.getText().trim();
            int growthDuration = Integer.parseInt(growthDurationField.getText().trim());

            if (!cropDAO.cropExists(cropId)) {
                JOptionPane.showMessageDialog(this, "Crop ID not found.");
                return;
            }

            boolean success = cropDAO.updateCrop(cropId, cropName, cropType, growthDuration);

            if (success) {
                JOptionPane.showMessageDialog(this, "Crop updated successfully.");
                clearForm();
                loadCrops();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update crop.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Growth Duration.");
        }
    }

    private void loadCrops() {
        tableModel.setRowCount(0);
        List<Crop> crops = cropDAO.getAllCrops();
        for (Crop crop : crops) {
            tableModel.addRow(new Object[]{
                    crop.getCropId(), crop.getCropName(), crop.getCropType(),
                    crop.getGrowthDuration(), crop.getFieldId()
            });
        }
    }

    private void clearForm() {
        cropIdField.setText("");
        cropNameField.setText("");
        cropTypeField.setText("");
        growthDurationField.setText("");
        fieldIdField.setText("");
        table.clearSelection();
    }
}