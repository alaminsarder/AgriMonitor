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
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Crop Information"));

        formPanel.add(new JLabel("Crop ID:"));
        cropIdField = new JTextField();
        formPanel.add(cropIdField);

        formPanel.add(new JLabel("Crop Name:"));
        cropNameField = new JTextField();
        formPanel.add(cropNameField);

        formPanel.add(new JLabel("Crop Type:"));
        cropTypeField = new JTextField();
        formPanel.add(cropTypeField);

        formPanel.add(new JLabel("Growth Duration (days):"));
        growthDurationField = new JTextField();
        formPanel.add(growthDurationField);

        formPanel.add(new JLabel("Field ID:"));
        fieldIdField = new JTextField();
        formPanel.add(fieldIdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Crop");
        JButton updateButton = new JButton("Update Crop");
        JButton refreshButton = new JButton("Refresh");
        JButton clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        tableModel = new DefaultTableModel(new Object[]{"Crop ID", "Crop Name", "Crop Type", "Growth Duration", "Field ID"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addCrop());
        updateButton.addActionListener(e -> updateCrop());
        refreshButton.addActionListener(e -> loadCrops());
        clearButton.addActionListener(e -> clearForm());

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

        loadCrops();
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