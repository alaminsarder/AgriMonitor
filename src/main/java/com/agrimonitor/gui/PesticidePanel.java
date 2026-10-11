package com.agrimonitor.gui;

import com.agrimonitor.dao.FieldDAO;
import com.agrimonitor.dao.PesticideDAO;
import com.agrimonitor.model.Pesticide;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PesticidePanel extends JPanel {

    private PesticideDAO pesticideDAO;
    private FieldDAO fieldDAO;
    private JTextField fieldIdField, pesticideNameField, quantityField, dateField, searchFieldIdField;
    private DefaultTableModel tableModel;
    private JTable table;

    public PesticidePanel() {
        pesticideDAO = new PesticideDAO();
        fieldDAO = new FieldDAO();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Pesticide Information"));

        formPanel.add(new JLabel("Field ID:"));
        fieldIdField = new JTextField();
        formPanel.add(fieldIdField);

        formPanel.add(new JLabel("Pesticide Name:"));
        pesticideNameField = new JTextField();
        formPanel.add(pesticideNameField);

        formPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        formPanel.add(quantityField);

        formPanel.add(new JLabel("Application Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        formPanel.add(dateField);

        formPanel.add(new JLabel("Search by Field ID:"));
        searchFieldIdField = new JTextField();
        formPanel.add(searchFieldIdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton addButton = new JButton("Add Pesticide Record");
        JButton refreshButton = new JButton("View All");
        JButton searchButton = new JButton("Search by Field");
        JButton clearButton = new JButton("Clear");

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        formPanel.add(buttonPanel);

        tableModel = new DefaultTableModel(new Object[]{"Field ID", "Pesticide Name", "Quantity", "Application Date"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        add(formPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> addPesticide());
        refreshButton.addActionListener(e -> loadAllPesticides());
        searchButton.addActionListener(e -> searchByField());
        clearButton.addActionListener(e -> clearForm());

        loadAllPesticides();
    }

    private void addPesticide() {
        try {
            String fieldId = fieldIdField.getText().trim();
            String pesticideName = pesticideNameField.getText().trim();
            double quantity = Double.parseDouble(quantityField.getText().trim());
            String date = dateField.getText().trim();

            if (fieldId.isEmpty() || pesticideName.isEmpty() || date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Field ID, Pesticide Name, and Date are required.");
                return;
            }

            if (!fieldDAO.fieldExists(fieldId)) {
                JOptionPane.showMessageDialog(this, "Field ID does not exist.");
                return;
            }

            Pesticide pesticide = new Pesticide(fieldId, pesticideName, quantity, date);
            boolean success = pesticideDAO.addPesticide(pesticide);

            if (success) {
                JOptionPane.showMessageDialog(this, "Pesticide record added successfully.");
                clearForm();
                loadAllPesticides();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add pesticide record.");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Quantity.");
        }
    }

    private void loadAllPesticides() {
        tableModel.setRowCount(0);
        List<Pesticide> list = pesticideDAO.getAllPesticides();
        for (Pesticide pesticide : list) {
            tableModel.addRow(new Object[]{
                    pesticide.getFieldId(), pesticide.getPesticideName(),
                    pesticide.getQuantity(), pesticide.getApplicationDate()
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
        List<Pesticide> list = pesticideDAO.getPesticideByFieldId(fieldId);
        for (Pesticide pesticide : list) {
            tableModel.addRow(new Object[]{
                    pesticide.getFieldId(), pesticide.getPesticideName(),
                    pesticide.getQuantity(), pesticide.getApplicationDate()
            });
        }
    }

    private void clearForm() {
        fieldIdField.setText("");
        pesticideNameField.setText("");
        quantityField.setText("");
        dateField.setText("");
        searchFieldIdField.setText("");
    }
}