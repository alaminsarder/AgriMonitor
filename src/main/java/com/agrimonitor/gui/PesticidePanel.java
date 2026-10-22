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
        setLayout(new BorderLayout(0, 15));
        setBackground(UITheme.BACKGROUND);

        add(buildFormCard(), BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        loadAllPesticides();
    }

    private JPanel buildFormCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Pesticide Information"), BorderLayout.NORTH);

        JPanel fieldsGrid = new JPanel(new GridLayout(1, 5, 15, 12));
        fieldsGrid.setBackground(UITheme.CARD_BG);

        fieldIdField = UITheme.createTextField();
        pesticideNameField = UITheme.createTextField();
        quantityField = UITheme.createTextField();
        dateField = UITheme.createTextField();
        searchFieldIdField = UITheme.createTextField();

        fieldsGrid.add(UITheme.labeledField("Field ID", fieldIdField));
        fieldsGrid.add(UITheme.labeledField("Pesticide Name", pesticideNameField));
        fieldsGrid.add(UITheme.labeledField("Quantity", quantityField));
        fieldsGrid.add(UITheme.labeledField("Application Date", dateField));
        fieldsGrid.add(UITheme.labeledField("Search by Field ID", searchFieldIdField));

        card.add(fieldsGrid, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 15));
        buttonPanel.setBackground(UITheme.CARD_BG);

        JButton addButton = UITheme.createStyledButton("Add Pesticide Record", UITheme.PRIMARY);
        JButton refreshButton = UITheme.createStyledButton("View All", UITheme.NEUTRAL);
        JButton searchButton = UITheme.createStyledButton("Search by Field", UITheme.INFO);
        JButton clearButton = UITheme.createStyledButton("Clear", UITheme.TEXT_LIGHT);

        buttonPanel.add(addButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(clearButton);

        card.add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addPesticide());
        refreshButton.addActionListener(e -> loadAllPesticides());
        searchButton.addActionListener(e -> searchByField());
        clearButton.addActionListener(e -> clearForm());

        return card;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.createCardPanel();
        card.setLayout(new BorderLayout());

        card.add(UITheme.createCardTitle("Pesticide Records"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Field ID", "Pesticide Name", "Quantity", "Application Date"}, 0) {
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